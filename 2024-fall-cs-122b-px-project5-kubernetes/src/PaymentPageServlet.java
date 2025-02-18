import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PaymentPageServlet", urlPatterns = "/api/processPayment")
public class PaymentPageServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("PaymentPageServlet doPost reached");

        JsonObject jsonInput = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        String firstName = jsonInput.get("first_name").getAsString();
        String lastName = jsonInput.get("last_name").getAsString();
        String cardNumber = jsonInput.get("card_number").getAsString();
        String expirationDate = jsonInput.get("expiration_date").getAsString();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            // Validate credit card
            if (isValidCreditCard(conn, firstName, lastName, cardNumber, expirationDate)) {
                // Attempt to record the sale if card details are valid
                request.getSession().setAttribute("id", cardNumber); // This acts as our "customerId"
                request.getSession().setAttribute("firstName", firstName);
                request.getSession().setAttribute("lastName", lastName);
                request.getSession().setAttribute("expirationDate", expirationDate);

                int saleId = recordSale(conn, request.getSession());
                if (saleId > 0) {
                    jsonResponse.addProperty("success", true);
                } else {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("errorMessage", "Failed to record sale. Please try again.");
                }
            } else {
                // Invalid card information provided
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("errorMessage", "Invalid payment information. Please check your details and try again.");
            }
        } catch (IllegalArgumentException e) {
            // Handle missing fields or empty values
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("errorMessage", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("errorMessage", "A server error occurred. Please try again later.");
        }

        out.write(jsonResponse.toString());
    }


    private boolean isValidCreditCard(Connection conn, String firstName, String lastName, String cardNumber, String expirationDate) throws SQLException {
        String formattedCardNumber = cardNumber.replaceAll("\\s+", "");

        String query = "SELECT * FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, cardNumber);
            statement.setString(4, expirationDate);


            ResultSet rs = statement.executeQuery();
            return rs.next(); // returns true if a matching record is found
        }
    }

    private int recordSale(Connection conn, HttpSession session) throws SQLException {
        String cardNumber = (String) session.getAttribute("id"); // Get cardNumber as customerId
        String movieId = (String) session.getAttribute("movieId"); // Assuming movieId is stored in session
        String insertSale = "INSERT INTO sales (cardNumber, movieId, saleDate) VALUES (?, ?, NOW())";

        try (PreparedStatement ps = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cardNumber); // customerId is being set as cardNumber
            ps.setString(2, movieId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated sale ID
            }
        }
        return -1; // Return -1 if insert fails
    }

    private void recordSaleItems(Connection conn, HttpSession session, int saleId) throws SQLException {
        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) session.getAttribute("cartItems");
        String insertSaleItem = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, NOW())";

        try (PreparedStatement ps = conn.prepareStatement(insertSaleItem)) {
            String customerId = (String) session.getAttribute("customerId"); // cardNumber or customer ID

            for (Map<String, Object> item : cartItems) {
                ps.setString(1, customerId);
                ps.setString(2, (String) item.get("movieId"));
                ps.addBatch();
            }
            ps.executeBatch(); // Insert each movie as a separate sale item
        }
    }
}