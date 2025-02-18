import Models.Customer;

import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletConfig;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String data = request.getReader().lines().collect(Collectors.joining());
        JsonParser parser = new JsonParser();
        JsonObject js = parser.parse(data).getAsJsonObject();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null && password == null) {
            email = js.get("email").getAsString();
            password = js.get("password").getAsString();
        }

        JsonObject responseJsonObject = new JsonObject();

        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Connection connection = dataSource.getConnection();
            String query = "SELECT * FROM customers WHERE email=?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);

            System.out.println("Executing query: " + preparedStatement.toString()); // Log the query being executed
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("User found: " + resultSet.getString("email"));
                String storedPassword = resultSet.getString("password");

                // Directly compare the passwords without encryption
                if (password.equals(storedPassword)) {
                    Customer customer = new Customer(
                            resultSet.getInt("id"),
                            resultSet.getString("firstName"),
                            resultSet.getString("lastName"),
                            resultSet.getString("ccId"),
                            resultSet.getString("address"),
                            resultSet.getString("email"),
                            resultSet.getString("password"));

                    // Set session attributes
                    HttpSession session = request.getSession();
                    session.setAttribute("loggedIn", true);
                    session.setAttribute("customer", customer);

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Successfully logged in!");
                } else {
                    System.out.println("No user found with the provided credentials.");
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect email or password!");
                }
            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Incorrect email or password!");
            }

            // Write the JSON response
            out.write(responseJsonObject.toString());

            // Clean up resources
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred on the server.");
            out.write(responseJsonObject.toString());
        } finally {
            out.close();  // Ensure the writer is always closed
        }
    }
}

