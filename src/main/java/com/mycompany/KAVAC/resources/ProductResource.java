package com.mycompany.KAVAC.resources;

import com.mycompany.kavac.db.DatabaseConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/products")
public class ProductResource {

    // GET /api/products
    // GET /api/products?category_id=1
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts(@QueryParam("category_id") Integer categoryId) {
        JSONArray list = new JSONArray();

        String sql = "SELECT p.product_id, p.sku, p.name, p.price, " +
                     "p.stock_quantity, p.unlock_methods, p.description, " +
                     "p.created_at, c.name AS category_name, c.category_id " +
                     "FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id";

        if (categoryId != null) {
            sql += " WHERE p.category_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (categoryId != null) ps.setInt(1, categoryId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("product_id",     rs.getInt("product_id"));
                obj.put("sku",            rs.getString("sku"));
                obj.put("name",           rs.getString("name"));
                obj.put("price",          rs.getDouble("price"));
                obj.put("stock_quantity", rs.getInt("stock_quantity"));
                obj.put("description",    rs.getString("description"));
                obj.put("category_id",    rs.getInt("category_id"));
                obj.put("category_name",  rs.getString("category_name"));
                obj.put("created_at",     rs.getString("created_at"));

                // unlock_methods đã là JSON string trong DB, parse lại
                String methods = rs.getString("unlock_methods");
                obj.put("unlock_methods", methods != null ? new JSONArray(methods) : new JSONArray());

                list.put(obj);
            }
            return Response.ok(list.toString()).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // GET /api/products/{id}
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") int id) {
        String sql = "SELECT p.*, c.name AS category_name FROM Products p " +
                     "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                     "WHERE p.product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("product_id",     rs.getInt("product_id"));
                obj.put("sku",            rs.getString("sku"));
                obj.put("name",           rs.getString("name"));
                obj.put("price",          rs.getDouble("price"));
                obj.put("stock_quantity", rs.getInt("stock_quantity"));
                obj.put("description",    rs.getString("description"));
                obj.put("category_id",    rs.getInt("category_id"));
                obj.put("category_name",  rs.getString("category_name"));
                String methods = rs.getString("unlock_methods");
                obj.put("unlock_methods", methods != null ? new JSONArray(methods) : new JSONArray());
                return Response.ok(obj.toString()).build();
            } else {
                return Response.status(404)
                        .entity("{\"error\":\"Sản phẩm không tồn tại\"}")
                        .build();
            }

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // POST /api/products  (ADMIN only — thêm sản phẩm mới)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(String body) {
        try {
            JSONObject data = new JSONObject(body);

            String sql = "INSERT INTO Products (category_id, sku, name, price, stock_quantity, unlock_methods, description) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt   (1, data.getInt("category_id"));
                ps.setString(2, data.getString("sku"));
                ps.setString(3, data.getString("name"));
                ps.setDouble(4, data.getDouble("price"));
                ps.setInt   (5, data.optInt("stock_quantity", 0));
                ps.setString(6, data.optJSONArray("unlock_methods") != null
                                ? data.getJSONArray("unlock_methods").toString() : "[]");
                ps.setString(7, data.optString("description", ""));

                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                int newId = keys.next() ? keys.getInt(1) : -1;

                return Response.status(201)
                        .entity("{\"product_id\":" + newId + ",\"message\":\"Thêm sản phẩm thành công\"}")
                        .build();
            }

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // PUT /api/products/{id}  (ADMIN — cập nhật sản phẩm)
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("id") int id, String body) {
        try {
            JSONObject data = new JSONObject(body);
            String sql = "UPDATE Products SET name=?, price=?, stock_quantity=?, " +
                         "unlock_methods=?, description=? WHERE product_id=?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, data.getString("name"));
                ps.setDouble(2, data.getDouble("price"));
                ps.setInt   (3, data.optInt("stock_quantity", 0));
                ps.setString(4, data.optJSONArray("unlock_methods") != null
                                ? data.getJSONArray("unlock_methods").toString() : "[]");
                ps.setString(5, data.optString("description", ""));
                ps.setInt   (6, id);

                int rows = ps.executeUpdate();
                if (rows == 0) return Response.status(404)
                        .entity("{\"error\":\"Không tìm thấy sản phẩm\"}").build();

                return Response.ok("{\"message\":\"Cập nhật thành công\"}").build();
            }

        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // DELETE /api/products/{id}  (ADMIN)
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("id") int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) return Response.status(404)
                    .entity("{\"error\":\"Không tìm thấy sản phẩm\"}").build();

            return Response.ok("{\"message\":\"Xóa sản phẩm thành công\"}").build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
