package dao;

import model.Feedback;
import util.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FeedbackDAO {
    private static final String SELECT =
            "SELECT f.*, s.name AS sender_name, a.title AS artwork_title FROM feedback f " +
            "JOIN users s ON s.id = f.sender_id LEFT JOIN artworks a ON a.id = f.artwork_id ";

    private static Feedback map(ResultSet rs) throws SQLException {
        return new Feedback(rs.getInt("id"), rs.getInt("sender_id"), rs.getString("sender_name"),
                rs.getInt("artwork_id"), rs.getString("artwork_title"), rs.getString("message"),
                String.valueOf(rs.getTimestamp("sent_at")));
    }

    public boolean add(int senderId, Integer receiverId, Integer artworkId, String message) throws SQLException {
        return DB.update("INSERT INTO feedback (sender_id, receiver_id, artwork_id, message) VALUES (?, ?, ?, ?)",
                senderId, receiverId, artworkId, message) > 0;
    }

    public List<Feedback> getReceived(int userId) throws SQLException {
        return DB.query(SELECT + "WHERE f.receiver_id = ? ORDER BY f.id DESC", FeedbackDAO::map, userId);
    }

    public List<Feedback> getSent(int userId) throws SQLException {
        return DB.query(SELECT + "WHERE f.sender_id = ? ORDER BY f.id DESC", FeedbackDAO::map, userId);
    }

    public Map<String, Number> interactionsByReceiver() throws SQLException {
        return DB.pairs("SELECT u.name, COUNT(*) FROM feedback f JOIN users u ON u.id = f.receiver_id " +
                "GROUP BY u.id, u.name");
    }
}
