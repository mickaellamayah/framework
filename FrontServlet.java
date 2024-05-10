package mg.itu.prom16;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.PrintWriter;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

public class FrontServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
    {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException
    {
        response.setContentType("text/html");
        try(PrintWriter out=response.getWriter())
        {
            String requestUrl = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            String fullUrl = (queryString == null) ? requestUrl : requestUrl + "?" + queryString;

            response.getWriter().println(fullUrl);
        }
        catch(Exception e)
        {

        }
    }
}
