package com.mycompany.KAVAC.resources;

import com.mycompany.kavac.db.DatabaseConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/categories")
public class CategoryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        JSONArray list = new JSONArray();
        String sql = "SELECT category_id, name, description, created_at FROM Categories";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("category_id",  rs.getInt("category_id"));
                obj.put("name",         rs.getString("name"));
                obj.put("description",  rs.getString("description"));
                obj.put("created_at",   rs.getString("created_at"));
                list.put(obj);
            }
            return Response.ok(list.toString()).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
