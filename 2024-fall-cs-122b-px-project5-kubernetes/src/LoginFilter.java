
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

//        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        if(httpRequest.getSession().getAttribute("loggedIn") != null) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect("login.html");
        }
    }
    // could be optimized
    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith) ||
                requestURI.equals("/project1_war/");

    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("api/processPayments");
        allowedURIs.add("ConfirmationPage.html");
        allowedURIs.add("PaymentPage.html");
        allowedURIs.add("PaymentPage.js");
        allowedURIs.add("_dashboard.js");
        allowedURIs.add("_dashboard.html");
        allowedURIs.add("/_dashboard");
        allowedURIs.add("employeeLogin.html");
        allowedURIs.add("employeeLogin.js");
        allowedURIs.add("/api/employeeLogin");
                // Add CSS file URI
        allowedURIs.add("res/css/main.css");
        allowedURIs.add("/res/assets/");
    }

    public void destroy() {
        // ignored.
    }

}