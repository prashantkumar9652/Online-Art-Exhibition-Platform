package dao;

import exception.ArtworkAlreadySoldException;
import model.Artwork;
import model.Order;
import util.DB;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private static final String SELECT =
            "SELECT o.*, a.title AS artwork_title, u.name AS buyer_name FROM orders o " +
            "JOIN artworks a ON a.id = o.artwork_id JOIN users u ON u.id = o.user_id ";

    private static Order map(ResultSet rs) throws SQLException {
        return new Order(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("artwork_id"), rs.getDouble("amount"),
                rs.getString("payment_method"), String.valueOf(rs.getTimestamp("order_date")),
                rs.getString("artwork_title"), rs.getString("buyer_name"));
    }

    /**
     * Purchase = ek TRANSACTION:
     *  - synchronized: ek time par sirf ek thread purchase kar sakta hai
     *  - SELECT ... FOR UPDATE: row lock, taaki artwork do logon ko na bik jaye
     *  - saare orders + status update + notification ek saath commit ho, ya sab rollback
     */
    public List<Order> purchase(int userId, Collection<Artwork> items, String paymentMethod)
            throws SQLException, ArtworkAlreadySoldException {
        double tax = Double.parseDouble(new SettingsDAO().get("tax_percent", "0"));

        synchronized (OrderDAO.class) {
            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);
                try {
                    List<Order> receipts = new ArrayList<>();
                    for (Artwork a : items) {
                        double price;
                        try (PreparedStatement chk = con.prepareStatement(
                                "SELECT status, price FROM artworks WHERE id = ? FOR UPDATE")) {
                            chk.setInt(1, a.getId());
                            try (ResultSet rs = chk.executeQuery()) {
                                if (!rs.next() || !"APPROVED".equals(rs.getString("status")))
                                    throw new ArtworkAlreadySoldException(
                                            "'" + a.getTitle() + "' ab available nahi hai (sold ho chuka hai).");
                                price = rs.getDouble("price");
                            }
                        }
                        double amount = Math.round(price * (1 + tax / 100) * 100) / 100.0;

                        try (PreparedStatement ins = con.prepareStatement(
                                "INSERT INTO orders (user_id, artwork_id, amount, payment_method) VALUES (?, ?, ?, ?)")) {
                            ins.setInt(1, userId);
                            ins.setInt(2, a.getId());
                            ins.setDouble(3, amount);
                            ins.setString(4, paymentMethod);
                            ins.executeUpdate();
                        }
                        try (PreparedStatement upd = con.prepareStatement(
                                "UPDATE artworks SET status = 'SOLD' WHERE id = ?")) {
                            upd.setInt(1, a.getId());
                            upd.executeUpdate();
                        }
                        receipts.add(new Order(0, userId, a.getId(), amount, paymentMethod,
                                new Timestamp(System.currentTimeMillis()).toString(), a.getTitle(), ""));
                    }
                    try (PreparedStatement n = con.prepareStatement("INSERT INTO notifications (message) VALUES (?)")) {
                        n.setString(1, receipts.size() + " artwork(s) sold just now!");
                        n.executeUpdate();
                    }
                    con.commit();
                    return receipts;
                } catch (SQLException | ArtworkAlreadySoldException e) {
                    con.rollback();
                    throw e;
                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }

    public List<Order> getByUser(int userId) throws SQLException {
        return DB.query(SELECT + "WHERE o.user_id = ? ORDER BY o.id DESC", OrderDAO::map, userId);
    }

    public List<Order> getByArtist(int artistId) throws SQLException {
        return DB.query(SELECT + "WHERE a.artist_id = ? ORDER BY o.id DESC", OrderDAO::map, artistId);
    }

    public Map<String, Number> revenueByArtist() throws SQLException {
        return DB.pairs("SELECT u.name, SUM(o.amount) FROM orders o JOIN artworks a ON a.id = o.artwork_id " +
                "JOIN users u ON u.id = a.artist_id GROUP BY u.id, u.name");
    }

    public double totalRevenue() throws SQLException {
        Double v = DB.queryOne("SELECT COALESCE(SUM(amount), 0) FROM orders", rs -> rs.getDouble(1));
        return v == null ? 0 : v;
    }
}
