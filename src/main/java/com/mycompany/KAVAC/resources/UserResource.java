package com.mycompany.KAVAC.resources;

import com.mycompany.kavac.db.DatabaseConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;
import org.json.JSONObject;

@Path("/users")
public class UserResource {

    // POST /api/users/register
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String body) {
        try {
            JSONObject data = new JSONObject(body);
            String email    = data.getString("email");
            String fullName = data.getString("full_name");
            String password = data.getString("password_hash");
            String phone    = data.optString("phone", "");
            String address  = data.optString("address_default", "");

            // Check email đã tồn tại chưa
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement check = conn.prepareStatement(
                         "SELECT user_id FROM Users WHERE email = ?")) {
                check.setString(1, email);
                if (check.executeQuery().next()) {
                    return Response.status(409)
                            .entity("{\"error\":\"Email đã được sử dụng\"}")
                            .build();
                }
            }

            // Insert user mới
            String sql = "INSERT INTO Users (full_name, email, password_hash, phone, address_default, role) " +
                         "VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, password); // TODO: hash bằng BCrypt sau
                ps.setString(4, phone);
                ps.setString(5, address);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                int newId = keys.next() ? keys.getInt(1) : -1;

                JSONObject res = new JSONObject();
                res.put("user_id",   newId);
                res.put("full_name", fullName);
                res.put("email",     email);
                res.put("role",      "CUSTOMER");
                res.put("message",   "Đăng ký thành công");
                return Response.status(201).entity(res.toString()).build();
            }

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // POST /api/users/login
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String body) {
        try {
            JSONObject data = new JSONObject(body);
            String email    = data.getString("email");
            String password = data.getString("password_hash");

            String sql = "SELECT user_id, full_name, email, role, phone, address_default " +
                         "FROM Users WHERE email = ? AND password_hash = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JSONObject res = new JSONObject();
                    res.put("user_id",         rs.getInt("user_id"));
                    res.put("full_name",        rs.getString("full_name"));
                    res.put("email",            rs.getString("email"));
                    res.put("role",             rs.getString("role"));
                    res.put("phone",            rs.getString("phone"));
                    res.put("address_default",  rs.getString("address_default"));
                    res.put("message",          "Đăng nhập thành công");
                    return Response.ok(res.toString()).build();
                } else {
                    return Response.status(401)
                            .entity("{\"error\":\"Email hoặc mật khẩu không đúng\"}")
                            .build();
                }
            }

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // GET /api/users/{id}
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("id") int id) {
        String sql = "SELECT user_id, full_name, email, phone, role, address_default, created_at " +
                     "FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("user_id",         rs.getInt("user_id"));
                obj.put("full_name",        rs.getString("full_name"));
                obj.put("email",            rs.getString("email"));
                obj.put("phone",            rs.getString("phone"));
                obj.put("role",             rs.getString("role"));
                obj.put("address_default",  rs.getString("address_default"));
                obj.put("created_at",       rs.getString("created_at"));
                return Response.ok(obj.toString()).build();
            } else {
                return Response.status(404)
                        .entity("{\"error\":\"Người dùng không tồn tại\"}")
                        .build();
            }

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
