package lk.ijse.dep9.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.api.util.HttpServlet2;
import lk.ijse.dep9.dto.StudentDTO;
import org.apache.commons.dbcp2.BasicDataSource;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            loadAllStudents(response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private void loadAllStudents(HttpServletResponse response) throws IOException {
        try {
            BasicDataSource pool = (BasicDataSource) getServletContext().getAttribute("pool");  /* Remove at Line 28 */

            Connection connection = pool.getConnection();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM student");

            ArrayList<StudentDTO> students = new ArrayList<>();
            while (rst.next()) {
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                StudentDTO dto = new StudentDTO(id, name, address, contact);
                students.add(dto);
            }
            connection.close();

            Jsonb jsonb = JsonbBuilder.create();
            String json = jsonb.toJson(students);
            jsonb.toJson(students, response.getWriter());
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to load members");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Must include member id");
            return;
        }

        Matcher matcher = Pattern.compile("^/([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})/?$").matcher(request.getPathInfo());
        if (matcher.matches()) {
            deleteStudent(matcher.group(1), response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid member id");
        }

    }

    private void deleteStudent(String memberId, HttpServletResponse response) throws IOException {
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
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }

        Matcher matcher = Pattern.
                compile("^/([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})/?$")
                .matcher(request.getPathInfo());
        if (matcher.matches()) {
            updateStudent(matcher.group(1), request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }

    private void updateStudent(String memberId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (request.getContentType() == null || !request.getContentType().startsWith("application/json")) {
                throw new JsonbException("Invalid JSON");
            }
            StudentDTO student = JsonbBuilder.create().fromJson(request.getReader(), StudentDTO.class);

            if (student.getId() == null || !memberId.equalsIgnoreCase(student.getId())) {
                throw new JsonbException("Id is empty or invalid");
            } else if (student.getName() == null || !student.getName().matches("[A-Za-z ]+")) {
                throw new JsonbException("Name is empty or invalid");
            } else if (student.getContact() == null || !student.getContact().matches("\\d{3}-\\d{7}")) {
                throw new JsonbException("Contact is empty or invalid");
            } else if (student.getAddress() == null || !student.getAddress().matches("^[A-Za-z0-9|,.:;#\\/\\\\ -]+$")) {
                throw new JsonbException("Address is empty or invalid");
            }

            try (Connection connection = pool.getConnection()) {
                PreparedStatement stm = connection.prepareStatement("UPDATE student SET name=?, address=?, contact=? WHERE id=?");
                stm.setString(1, student.getName());
                stm.setString(2, student.getAddress());
                stm.setString(3, student.getContact());
                stm.setString(4, student.getId());

                if (stm.executeUpdate() == 1) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student does not exists");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (JsonbException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
