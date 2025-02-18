import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movieInfo")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;


    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");


        String id = request.getParameter("id");


        request.getServletContext().log("getting id: " + id);


        PrintWriter out = response.getWriter();


        try (Connection conn = dataSource.getConnection()) {



            String query = "SELECT m.id, m.title, m.year, m.director,\n" +
            "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres,\n" +
            "GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ') AS stars,\n" +
            "GROUP_CONCAT(DISTINCT s.id ORDER BY s.name SEPARATOR ', ') AS starIds,\n" +
            "r.rating\n" +
            "FROM movies m\n" +
            "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
            "LEFT JOIN genres g ON gim.genreId = g.id\n" +
            "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
            "LEFT JOIN stars s ON sim.starId = s.id\n" +
            "LEFT JOIN ratings r ON m.id = r.movieId\n" +
            "WHERE m.id = ?\n" +
            "GROUP BY m.id, m.title, m.year, m.director;";


            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();


            while (rs.next()) {

                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String genres = getString(rs,"genres");
                String stars = getString(rs, "stars");
                double rating = rs.getDouble("rating");

                String[] starIds = rs.getString("starIds").split(", ");
                String[] starNames = stars.split(", ");


                JsonArray starIdsJsonArray = new JsonArray();
                JsonArray starNamesJsonArray = new JsonArray();
                for (String starId : starIds) {
                    starIdsJsonArray.add(starId);
                }
                for (String starName : starNames) {
                    starNamesJsonArray.add(starName);
                }


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", movieTitle);
                jsonObject.addProperty("year", movieYear);
                jsonObject.addProperty("director", movieDirector);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("stars", stars);
                jsonObject.add("starIds", starIdsJsonArray);
                jsonObject.add("stars", starNamesJsonArray);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();


            out.write(jsonArray.toString());

            response.setStatus(200);

        } catch (Exception e) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());


            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }



    }
    protected String getString(ResultSet resultSet, String field) throws SQLException {
        String value = resultSet.getString(field);
        if (value == null) {
            value = "";
        }
        return value;
    }
}

