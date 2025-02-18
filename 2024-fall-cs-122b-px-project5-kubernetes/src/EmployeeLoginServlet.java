import java.io.*;
import java.sql.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;
import com.google.gson.JsonParser;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employeeLogin")
public class EmployeeLoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonObject = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();

        JsonObject responseJsonObject = new JsonObject();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        System.out.println("Email: " + email);
        try {
            Connection connection = dataSource.getConnection();
            String query = "SELECT * FROM employees WHERE email=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");
                System.out.println("Stored Password: " + storedPassword);
                System.out.println("Provided Password: " + password);

                // Direct comparison of plain text passwords
                if (password.equals(storedPassword)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("loggedInEmployee", true);

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Successfully logged in!");
                } else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect email or password!");
                }
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Email not found!");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "An error occurred on the server.");
        } finally {
            out.write(responseJsonObject.toString());
            out.close();
        }
    }
}

