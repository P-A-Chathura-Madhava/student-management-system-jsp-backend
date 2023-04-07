package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.api.util.HttpServlet2;
import lk.ijse.dep9.db.ConnectionPool;
import lk.ijse.dep9.dto.StudentDTO;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "StudentServlet", value = "/students/*", loadOnStartup = 0)
public class StudentServlet extends HttpServlet2 {

    @Resource(lookup = "java:/comp/env/jdbc/sms")
    private DataSource pool;

    /* Only use with Glassfish server */
    /*@Override
    public void init() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            pool = (DataSource) ctx.lookup("jdbc/lms");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }*/

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
//            ConnectionPool pool = (ConnectionPool) getServletContext().getAttribute("pool");  /* Remove at Line 26 */
            BasicDataSource pool = (BasicDataSource) getServletContext().getAttribute("pool");  /* Remove at Line 28 */

            Connection connection = pool.getConnection();
            /* Line 23 */
            /*Class.forName("com.mysql.cj.jdbc.Driver");
            try(Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sms", "root", "123@Ctech")) {*/

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

//                pool.releaseConnection(connection);   /* Remove at Line 26 */
            connection.close();

                Jsonb jsonb = JsonbBuilder.create();
                String json = jsonb.toJson(students);
                jsonb.toJson(students, response.getWriter());


                /*Step 05*/
                /*response.setContentType("application/json");
                response.getWriter().println(json);*/


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
//            }
        } catch (SQLException e) {
            /*throw new RuntimeException(e);*/
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load members");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /* To Check if methods are working (10) */
//        response.getWriter().println("StudentServlet : doPost()");
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            try {
                if (request.getContentType() == null || !request.getContentType().startsWith("application/json")) {
                    throw new JsonbException("Invalid JSON");
                }

                StudentDTO student = JsonbBuilder.create().
                        fromJson(request.getReader(), StudentDTO.class);

                if (student.getName() == null ||
                        !student.getName().matches("[A-Za-z ]+")) {
                    throw new JsonbException("Name is empty or invalid");
                } else if (student.getContact() == null ||
                        !student.getContact().matches("\\d{3}-\\d{7}")) {
                    throw new JsonbException("Contact is empty or invalid");
                } else if (student.getAddress() == null ||
                        !student.getAddress().matches("^[A-Za-z0-9|,.:;#\\/\\\\ -]+$")) {
                    throw new JsonbException("Address is empty or invalid");
                }

                try (Connection connection = pool.getConnection()) {
                    student.setId(UUID.randomUUID().toString());
                    PreparedStatement stm = connection.
                            prepareStatement("INSERT INTO student (id, name, address, contact) VALUES (?, ?, ?, ?)");
                    stm.setString(1, student.getId());
                    stm.setString(2, student.getName());
                    stm.setString(3, student.getAddress());
                    stm.setString(4, student.getContact());

                    int affectedRows = stm.executeUpdate();
                    if (affectedRows == 1) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                        response.setContentType("application/json");
                        JsonbBuilder.create().toJson(student, response.getWriter());
                    } else {
                        throw new SQLException("Something went wrong");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } catch (JsonbException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /* To Check if methods are working (10) */
//        response.getWriter().println("StudentServlet : doDelete()");
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
//            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Must include member id");
            return;
        }

        Matcher matcher = Pattern.compile("^/([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})/?$").matcher(request.getPathInfo());
        if (matcher.matches()) {
            deleteMember(matcher.group(1), response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid member id");
        }

    }

    private void deleteMember(String memberId, HttpServletResponse response) throws IOException {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM student WHERE id=?");
            stm.setString(1, memberId);
            int affectedRows = stm.executeUpdate();
            if (affectedRows == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid member id");
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /* To Check if methods are working (10) */
        resp.getWriter().println("StudentServlet : doPatch()");
    }
}
