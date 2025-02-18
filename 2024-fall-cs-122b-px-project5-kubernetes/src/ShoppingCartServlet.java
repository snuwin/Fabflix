import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;

// Declaring a WebServlet called ShoppingCartServlet, which maps to url "/api/ShoppingCart"
@WebServlet(name = "ShoppingCart", urlPatterns = "/api/ShoppingCart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        System.out.println("Entered Get Request");
        double totalPrice = 0;

        PrintWriter out = response.getWriter();
        Map<String, Integer> uniqueItems = new HashMap<>();
        String jsonArrayString = (String) request.getParameter("data");

        if (jsonArrayString == null || jsonArrayString.isEmpty()) {
            // Parse the JSON array string into a JsonArray object
            System.out.println("EmptyCart");
            return;
        } else {
            JsonArray jsonArray = JsonParser.parseString(jsonArrayString).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                // Assuming each element is a JsonObject with an "id" field
                String movieId = element.getAsString();
                // Update the count of the ID in the map
                uniqueItems.put(movieId, uniqueItems.getOrDefault(movieId, 0) + 1);
            }
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Declare our statement
            PreparedStatement statement = null;
            System.out.println("Entered into ShoppingCartServlet");
            String query = "";

            JsonArray jsonArray = new JsonArray();

            for (String items: uniqueItems.keySet()){
                query = "SELECT * " +
                        "FROM movies " +
                        "WHERE movies.id = ?";

                statement = conn.prepareStatement(query);
                statement.setString(1, items);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String movieID = resultSet.getString("id");
                    double moviePrice = resultSet.getDouble("price");
                    String movieTitle = resultSet.getString("title");
                    totalPrice += (moviePrice * uniqueItems.get(movieID));

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movieId", movieID);
                    jsonObject.addProperty("title", movieTitle);
                    jsonObject.addProperty("price", moviePrice);
                    jsonObject.addProperty("quantity", uniqueItems.get(movieID));
                    jsonObject.addProperty("itemTotal", moviePrice * uniqueItems.get(movieID));

                    jsonArray.add(jsonObject);
                    resultSet.close();
                }
                else {
                    // No results found, so raise an error
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("errorMessage", "Invalid movie ID: " + items);

                    // Add the error object to your JSON response array (or however you handle error responses)
                    jsonArray.add(jsonObject);
                    resultSet.close();
                    break;
                }

            }

            jsonArray.add(totalPrice);
            if (statement != null) {
                statement.close();
            }

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        }
        catch(Exception e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}

