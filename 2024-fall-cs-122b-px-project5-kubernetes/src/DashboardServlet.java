import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard")
public class DashboardServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Check if the user is logged in by verifying the session attribute
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInEmployee") == null) {
            // If not logged in, redirect to the login page
            response.sendRedirect("employeeLogin.html");
            return;
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String operation = request.getParameter("operation");
        System.out.println("Operation received: " + operation);
        if ("add_star".equals(operation)) {
            addStar(request, response);
        } else if ("get_metadata".equals(operation)) {
            getMetadata(response);
        } else if ("add_movie".equals(operation)) {
            addMovie(request, response);
        }
    }

    private void addStar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String birthYearStr = request.getParameter("birthYear");
        Integer birthYear = (birthYearStr == null || birthYearStr.isEmpty()) ? null : Integer.parseInt(birthYearStr);

        try (Connection conn = dataSource.getConnection()) {
            String countQuery = "SELECT COUNT(*) + 1 AS nextId FROM stars";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet rs = countStmt.executeQuery();
            String newId = null;
            if (rs.next()) {
                int nextId = rs.getInt("nextId");
                newId = "nm" + String.format("%08d", nextId);
            }
            rs.close();
            countStmt.close();

            String insertStarSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertStarSQL);
            stmt.setString(1, newId);
            stmt.setString(2, name);
            if (birthYear != null) {
                stmt.setInt(3, birthYear);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();
            response.getWriter().write("Star added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error adding star.");
        }
    }


    private void getMetadata(HttpServletResponse response) throws IOException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet tables = metadata.getTables(null, null, "%", null);

            StringBuilder result = new StringBuilder();
            while (tables.next()) {
                String tableName = tables.getString(3);
                result.append("<h3>Table: ").append(tableName).append("</h3>");
                ResultSet columns = metadata.getColumns(null, null, tableName, "%");

                result.append("<ul>");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    result.append("<li>").append(columnName).append(" - ").append(columnType).append("</li>");
                }
                result.append("</ul>");
            }
            response.setContentType("text/html");
            response.getWriter().write(result.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error fetching metadata.");
        }
    }

    private void addMovie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");

        try (Connection conn = dataSource.getConnection()) {
            CallableStatement stmt = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?)}");
            stmt.setString(1, title);
            stmt.setInt(2, year);
            stmt.setString(3, director);
            stmt.setString(4, starName);
            stmt.setString(5, genreName);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                response.getWriter().write(rs.getString("message"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("Error adding movie.");
        }
    }
}
