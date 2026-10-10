package dao;

import exception.InvalidLoginException;
import model.*;
import util.DB;
import util.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserDAO implements DAO<User> {

    // Role ke hisaab se sahi subclass banata hai (polymorphism)
    public static User create(String role, int id, String name, String email, String pass, String status) {
        switch (role) {
            case "ADMIN":  return new Admin(id, name, email, pass, status);
            case "ARTIST": return new Artist(id, name, email, pass, status);
            default:       return new Enthusiast(id, name, email, pass, status);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return create(rs.getString("role"), rs.getInt("id"), rs.getString("name"),
                rs.getString("email"), rs.getString("password"), rs.getString("status"));
    }

    // u.getPassword() mein plain password aana chahiye, yahan hash hoga
    @Override
    public boolean add(User u) throws SQLException {
        return DB.update("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)",
                u.getName(), u.getEmail(), DBConnection.hash(u.getPassword()), u.getRole()) > 0;
    }

    @Override
    public User getById(int id) throws SQLException {
        return DB.queryOne("SELECT * FROM users WHERE id = ?", UserDAO::map, id);
    }

    @Override
    public List<User> getAll() throws SQLException {
        return DB.query("SELECT * FROM users ORDER BY id", UserDAO::map);
    }

    @Override
    public boolean update(User u) throws SQLException {
        return DB.update("UPDATE users SET name = ?, email = ?, role = ?, status = ? WHERE id = ?",
                u.getName(), u.getEmail(), u.getRole(), u.getStatus(), u.getId()) > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return DB.update("DELETE FROM users WHERE id = ?", id) > 0;
    }

    public boolean resetPassword(int id, String plainPassword) throws SQLException {
        return DB.update("UPDATE users SET password = ? WHERE id = ?", DBConnection.hash(plainPassword), id) > 0;
    }

    public Map<String, Number> countByRole() throws SQLException {
        return DB.pairs("SELECT role, COUNT(*) FROM users GROUP BY role");
    }

    // Login: sahi email/password par User object, warna custom exception
    public User login(String email, String password) throws SQLException, InvalidLoginException {
        User u = DB.queryOne("SELECT * FROM users WHERE email = ? AND password = ?",
                UserDAO::map, email, DBConnection.hash(password));
        if (u == null) throw new InvalidLoginException("Email ya password galat hai!");
        if ("BLOCKED".equals(u.getStatus()))
            throw new InvalidLoginException("Ye account block hai. Admin se contact karo.");
        return u;
    }
}
