package dao;

import model.Notification;
import util.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NotificationDAO {
    private static Notification map(ResultSet rs) throws SQLException {
        return new Notification(rs.getInt("id"), rs.getString("message"), String.valueOf(rs.getTimestamp("created_at")));
    }

    public boolean add(String message) throws SQLException {
        return DB.update("INSERT INTO notifications (message) VALUES (?)", message) > 0;
    }

    public List<Notification> getAll() throws SQLException {
        return DB.query("SELECT * FROM notifications ORDER BY id DESC", NotificationDAO::map);
    }

    public Notification latest() throws SQLException {
        return DB.queryOne("SELECT * FROM notifications ORDER BY id DESC LIMIT 1", NotificationDAO::map);
    }

    public boolean delete(int id) throws SQLException {
        return DB.update("DELETE FROM notifications WHERE id = ?", id) > 0;
    }
}
