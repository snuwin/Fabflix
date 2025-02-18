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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
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

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT s.id, s.name, s.birthYear,\n" +
            "GROUP_CONCAT(DISTINCT m.id ORDER BY m.title SEPARATOR ', ') AS movieIds,\n" +
            "GROUP_CONCAT(DISTINCT m.title ORDER BY m.title SEPARATOR ', ') AS movies\n" +
            " FROM stars s\n" +
            "JOIN stars_in_movies sim ON s.id = sim.starId\n" +
            "JOIN movies m ON sim.movieId = m.id\n" +
            "WHERE s.id = ?\n" +
            "GROUP BY s.id, s.name, s.birthYear;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String starId = rs.getString("id");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");
                String movies = rs.getString("movies");

                String[] movieIds = rs.getString("movieIds").split(", ");
                String[] movieTitles = movies.split(", ");

                // Create JSON arrays for starIds and starNames
                JsonArray movieIdsJsonArray = new JsonArray();
                JsonArray movieTitlesJsonArray = new JsonArray();
                for (String movieId : movieIds) {
                    movieIdsJsonArray.add(movieId);
                }
                for (String movieTitle : movieTitles) {
                    movieTitlesJsonArray.add(movieTitle);
                }

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", starId);
                jsonObject.addProperty("name", starName);
                jsonObject.addProperty("birthYear", starDob);
                jsonObject.addProperty("movies", movies);
                jsonObject.add("movieIds", movieIdsJsonArray); // Add starIds as JSON array
                jsonObject.add("movies", movieTitlesJsonArray); // Add starNames as JSON array

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}