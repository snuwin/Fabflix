import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonArray jsonArray = new JsonArray();

        String query = request.getParameter("query");
        if (query != null) {
            query = query.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
        }

        // If the query is null or empty, return an empty array
        if (query == null || query.isEmpty()) {
            response.getWriter().write(jsonArray.toString());
            return;
        }

        // Updated query to fetch both movies and stars
        String autoCompleteQuery =
                "(SELECT m.id AS id, m.title AS value, 'movie' AS type, NULL AS starId FROM movies m " +
                        "WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) LIMIT 5) " +
                        "UNION " +
                        "(SELECT NULL AS id, s.name AS value, 'star' AS type, s.id AS starId FROM stars s " +
                        "WHERE s.name LIKE ? LIMIT 5)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(autoCompleteQuery)) {

            ps.setString(1, query + "*"); // Prefix matching for movies
            ps.setString(2, "%" + query + "%"); // Partial matching for stars
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", rs.getString("value")); // Common name (movie title or star name)
                JsonObject additionalData = new JsonObject();

                if ("movie".equals(rs.getString("type"))) {
                    additionalData.addProperty("movieID", rs.getString("id"));
                    additionalData.addProperty("starID", (String) null); // Explicitly cast to String
                } else if ("star".equals(rs.getString("type"))) {
                    additionalData.addProperty("movieID", (String) null); // Explicitly cast to String
                    additionalData.addProperty("starID", rs.getString("starId"));
                }
                jsonObject.add("data", additionalData);
                jsonArray.add(jsonObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("errorMessage", "An error occurred while processing your request.");
            jsonArray.add(error);
        }
        // Send the JSON response
        response.getWriter().write(jsonArray.toString());
    }
}
