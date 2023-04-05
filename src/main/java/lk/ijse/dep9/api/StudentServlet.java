package lk.ijse.dep9.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.api.util.HttpServlet2;
import lk.ijse.dep9.dto.StudentDTO;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "StudentServlet", value = "/students/*")
public class StudentServlet extends HttpServlet2 {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /* To Check if methods are working (10) */ /* Step 01 */
//        response.getWriter().println("StudentServlet : doGet()");
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) { /* /students || /students/ */
            /* response.getWriter().println("<h1>All Members</h1>"); */ /* Step 02 */
            loadAllStudents(response);
        }else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private void loadAllStudents(HttpServletResponse response) throws IOException {
        /*response.getWriter().println("<h1>All Members</h1>");*/ /* Step 03 */
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sms", "root", "123@Ctech")) {

                Statement stm = connection.createStatement();
                ResultSet rst = stm.executeQuery("SELECT * FROM student");

                ArrayList<StudentDTO> students = new ArrayList<>();
                while (rst.next()){
                    String id = rst.getString("id");
                    String name = rst.getString("name");
                    String address = rst.getString("address");
                    String contact = rst.getString("contact");
                    StudentDTO dto = new StudentDTO(id, name, address, contact);
                    students.add(dto);
                }
                Jsonb jsonb = JsonbBuilder.create();
                String json = jsonb.toJson(students);
                response.setContentType("application/json");
                response.getWriter().println(json);


                /* Step 04 */
                /*StringBuilder sb = new StringBuilder();
                sb.append("[");
                while (rst.next()){
                    String id = rst.getString("id");
                    String name = rst.getString("name");
                    String address = rst.getString("address");
                    String contact = rst.getString("contact");
                    *//*response.getWriter().printf("<h1>ID : %s, NAME : %s, ADDRESS : %s, CONTACT : %s</h1>", id, name, address, contact);*//*
                    String jsonobj = "{\n" +
                            "  \"id\": \"" + id + "\",\n" +
                            "  \"name\": \"" + name + "\",\n" +
                            "  \"address\": \"" + address + "\",\n" +
                            "  \"contact\": \"" + contact + "\"\n" +
                            "}";
                    sb.append(jsonobj).append(",");
                }
                sb.deleteCharAt(sb.length()-1);
                sb.append("]");
                response.setContentType("application/json");
                response.getWriter().println(sb);*/

            /*} catch (SQLException e) {
                throw new RuntimeException(e);*/
            }
        } catch (ClassNotFoundException | SQLException e) {
            /*throw new RuntimeException(e);*/
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load members");
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
