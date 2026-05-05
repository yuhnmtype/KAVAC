package com.mycompany.KAVAC.resources;

import com.mycompany.kavac.db.DatabaseConnection;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("/orders")
public class OrderResource {

    // POST /api/orders  — Tạo đơn hàng mới
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrder(String body) {
        Connection conn = null;
        try {
            JSONObject data     = new JSONObject(body);
            int userId          = data.getInt("user_id");
            String address      = data.getString("shipping_address");
            JSONArray items     = data.getJSONArray("items");

            // Tính tổng tiền từ DB (không tin frontend gửi lên)
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            double totalAmount = 0;
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int productId   = item.getInt("product_id");
                int qty         = item.getInt("quantity");

                PreparedStatement pricePs = conn.prepareStatement(
                    "SELECT price, stock_quantity FROM Products WHERE product_id = ?");
                pricePs.setInt(1, productId);
                ResultSet priceRs = pricePs.executeQuery();

                if (!priceRs.next()) {
                    conn.rollback();
                    return Response.status(404)
                            .entity("{\"error\":\"Sản phẩm ID " + productId + " không tồn tại\"}")
                            .build();
                }

                int stock = priceRs.getInt("stock_quantity");
                if (stock < qty) {
                    conn.rollback();
                    return Response.status(400)
                            .entity("{\"error\":\"Sản phẩm ID " + productId + " không đủ hàng (còn " + stock + ")\"}")
                            .build();
                }

                totalAmount += priceRs.getDouble("price") * qty;
            }

            // Insert Orders
            PreparedStatement orderPs = conn.prepareStatement(
                "INSERT INTO Orders (user_id, total_amount, status, shipping_address) VALUES (?, ?, 'PENDING', ?)",
                Statement.RETURN_GENERATED_KEYS);
            orderPs.setInt   (1, userId);
            orderPs.setDouble(2, totalAmount);
            orderPs.setString(3, address);
            orderPs.executeUpdate();

            ResultSet keys  = orderPs.getGeneratedKeys();
            int orderId     = keys.next() ? keys.getInt(1) : -1;

            // Insert Order_Items + trừ stock
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int productId   = item.getInt("product_id");
                int qty         = item.getInt("quantity");

                // Lấy price từ DB
                PreparedStatement pPs = conn.prepareStatement(
                    "SELECT price FROM Products WHERE product_id = ?");
                pPs.setInt(1, productId);
                ResultSet pRs = pPs.executeQuery();
                pRs.next();
                double price = pRs.getDouble("price");

                // Insert order item
                PreparedStatement itemPs = conn.prepareStatement(
                    "INSERT INTO Order_Items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)");
                itemPs.setInt   (1, orderId);
                itemPs.setInt   (2, productId);
                itemPs.setInt   (3, qty);
                itemPs.setDouble(4, price);
                itemPs.executeUpdate();

                // Trừ stock
                PreparedStatement stockPs = conn.prepareStatement(
                    "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ?");
                stockPs.setInt(1, qty);
                stockPs.setInt(2, productId);
                stockPs.executeUpdate();
            }

            conn.commit();

            JSONObject res = new JSONObject();
            res.put("order_id",     orderId);
            res.put("total_amount", totalAmount);
            res.put("status",       "PENDING");
            res.put("message",      "Đặt hàng thành công!");
            return Response.status(201).entity(res.toString()).build();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
        }
    }

    // GET /api/orders/user/{userId}  — Lịch sử đơn hàng của user
    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrdersByUser(@PathParam("userId") int userId) {
        JSONArray list = new JSONArray();
        String sql = "SELECT o.order_id, o.total_amount, o.status, o.shipping_address, o.order_date " +
                     "FROM Orders o WHERE o.user_id = ? ORDER BY o.order_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                JSONObject order = new JSONObject();
                order.put("order_id",        orderId);
                order.put("total_amount",    rs.getDouble("total_amount"));
                order.put("status",          rs.getString("status"));
                order.put("shipping_address",rs.getString("shipping_address"));
                order.put("order_date",      rs.getString("order_date"));

                // Lấy items của order này
                PreparedStatement itemPs = conn.prepareStatement(
                    "SELECT oi.quantity, oi.price_at_purchase, p.name, p.sku " +
                    "FROM Order_Items oi LEFT JOIN Products p ON oi.product_id = p.product_id " +
                    "WHERE oi.order_id = ?");
                itemPs.setInt(1, orderId);
                ResultSet itemRs = itemPs.executeQuery();
                JSONArray itemArr = new JSONArray();
                while (itemRs.next()) {
                    JSONObject it = new JSONObject();
                    it.put("name",               itemRs.getString("name"));
                    it.put("sku",                itemRs.getString("sku"));
                    it.put("quantity",           itemRs.getInt("quantity"));
                    it.put("price_at_purchase",  itemRs.getDouble("price_at_purchase"));
                    itemArr.put(it);
                }
                order.put("items", itemArr);
                list.put(order);
            }
            return Response.ok(list.toString()).build();

        } catch (SQLException e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // PUT /api/orders/{id}/status  — Cập nhật trạng thái (ADMIN)
    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("id") int id, String body) {
        try {
            JSONObject data = new JSONObject(body);
            String status   = data.getString("status");

            String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt   (2, id);
                int rows = ps.executeUpdate();
                if (rows == 0) return Response.status(404)
                        .entity("{\"error\":\"Đơn hàng không tồn tại\"}").build();
                return Response.ok("{\"message\":\"Cập nhật trạng thái thành công\"}").build();
            }
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
