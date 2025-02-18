import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    // TMDb API Key
    private static final String TMDB_API_KEY = "8da50c7e31c2b4b3d8b9e9f720d11123";

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String name = request.getParameter("star");
        String genre = request.getParameter("genre");
        String letter = request.getParameter("letter");

        // Pagination parameters: page and N (movies per page)
        int page = Integer.parseInt(request.getParameter("page") == null ? "1" : request.getParameter("page"));
        int N = Integer.parseInt(request.getParameter("N") == null ? "10" : request.getParameter("N"));
        int offset = (page - 1) * N;

        // Sorting parameters
        String primarySortBy = request.getParameter("primarySortBy");
        String primarySortOrder = request.getParameter("primarySortOrder");
        String secondarySortBy = request.getParameter("secondarySortBy");
        String secondarySortOrder = request.getParameter("secondarySortOrder");

        // Default sorting if not provided
        if (primarySortBy == null || primarySortBy.isEmpty()) primarySortBy = "title";
        if (primarySortOrder == null || primarySortOrder.isEmpty()) primarySortOrder = "ASC";
        if (secondarySortBy == null || secondarySortBy.isEmpty())
            secondarySortBy = primarySortBy.equals("title") ? "rating" : "title";
        if (secondarySortOrder == null || secondarySortOrder.isEmpty()) secondarySortOrder = "ASC";

        // Build the query
        String query = "SELECT m.id, m.title, m.year, m.director, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', '), ', ', 3) AS genres, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name SEPARATOR ', '), ', ', 3) AS starIds, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', '), ', ', 3) AS stars, " +
                "r.rating, m.posterPath " +
                "FROM movies m " +
                "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                "LEFT JOIN genres g ON gim.genreId = g.id " +
                "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                "LEFT JOIN stars s ON sim.starId = s.id " +
                "LEFT JOIN ratings r ON m.id = r.movieId " +
                "WHERE 1=1 ";

        if (title != null && !title.isEmpty()) query += "AND MATCH (m.title) AGAINST (? IN BOOLEAN MODE) ";
        if (year != null && !year.isEmpty()) query += "AND m.year = ? ";
        if (director != null && !director.isEmpty()) query += "AND m.director LIKE ? ";
        if (name != null && !name.isEmpty())
            query += "AND m.id IN (SELECT sim.movieId FROM stars_in_movies sim JOIN stars s ON sim.starId = s.id WHERE s.name LIKE ?) ";
        if (genre != null && !genre.isEmpty())
            query += "AND m.id IN (SELECT gim.movieId FROM genres_in_movies gim JOIN genres g ON gim.genreId = g.id WHERE g.name = ?) ";
        if (letter != null && !letter.isEmpty())
            query += letter.equals("*") ? "AND m.title REGEXP '^[^a-zA-Z0-9]' " : "AND m.title LIKE ? ";

        query += "GROUP BY m.id, m.title, m.year, m.director ORDER BY " +
                primarySortBy + " " + primarySortOrder + ", " + secondarySortBy + " " + secondarySortOrder +
                " LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);

            int parameterIndex = 1;
            if (title != null && !title.isEmpty()) statement.setString(parameterIndex++, "+" + title + "*");
            if (year != null && !year.isEmpty()) statement.setString(parameterIndex++, year);
            if (director != null && !director.isEmpty()) statement.setString(parameterIndex++, "%" + director + "%");
            if (name != null && !name.isEmpty()) statement.setString(parameterIndex++, "%" + name + "%");
            if (genre != null && !genre.isEmpty()) statement.setString(parameterIndex++, genre);
            if (letter != null && !letter.equals("*")) statement.setString(parameterIndex++, letter + "%");

            statement.setInt(parameterIndex++, N);
            statement.setInt(parameterIndex++, offset);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("year", rs.getString("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("genres", rs.getString("genres"));
                jsonObject.addProperty("rating", rs.getDouble("rating"));
                jsonObject.addProperty("id", rs.getString("id"));

                jsonObject.addProperty("stars", rs.getString("stars"));
                jsonObject.addProperty("starIds", rs.getString("starIds"));

                String posterPath = rs.getString("posterPath");
                if (posterPath == null || posterPath.isEmpty()) {
                    try {
                        posterPath = fetchPosterPathFromTMDb(rs.getString("title"));
                        if (posterPath != null) {
                            PreparedStatement updateStmt = conn.prepareStatement("UPDATE movies SET posterPath = ? WHERE id = ?");
                            updateStmt.setString(1, posterPath);
                            updateStmt.setString(2, rs.getString("id"));
                            updateStmt.executeUpdate();
                            updateStmt.close();
                        } else {
                            System.err.println("Poster not found for movie: " + rs.getString("title"));
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching poster for movie: " + rs.getString("title"));
                        e.printStackTrace();
                    }
                }
                String posterUrl = posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : "res/images/default-poster.jpg";
                jsonObject.addProperty("posterUrl", posterUrl);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);

        } catch (SQLException e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private String fetchPosterPathFromTMDb(String movieTitle) {
        String apiKey = "8da50c7e31c2b4b3d8b9e9f720d11123"; // OUR actual TMDb API key
        String apiUrl = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + URLEncoder.encode(movieTitle, StandardCharsets.UTF_8);

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonResponse.getAsJsonArray("results");

            if (results.size() > 0) {
                JsonObject firstResult = results.get(0).getAsJsonObject();
                // Check if poster_path exists and is not null
                if (firstResult.has("poster_path") && !firstResult.get("poster_path").isJsonNull()) {
                    return firstResult.get("poster_path").getAsString();
                } else {
                    System.err.println("Poster path is missing or null for movie: " + movieTitle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if no poster is found
    }

}
