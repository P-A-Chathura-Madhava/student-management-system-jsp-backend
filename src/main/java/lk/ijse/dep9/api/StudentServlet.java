package lk.ijse.dep9.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.api.util.HttpServlet2;

import java.io.IOException;

@WebServlet(name = "StudentServlet", value = "/students/*")
public class StudentServlet extends HttpServlet2 {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /* To Check if methods are working (10) */
//        response.getWriter().println("StudentServlet : doGet()");
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) { /* /students || /students/ */
            response.getWriter().println("<h1>All Members</h1>");
        }else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /* To Check if methods are working (10) */
        response.getWriter().println("StudentServlet : doPost()");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* To Check if methods are working (10) */
        resp.getWriter().println("StudentServlet : doDelete()");
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* To Check if methods are working (10) */
        resp.getWriter().println("StudentServlet : doPatch()");
    }
}
