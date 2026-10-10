package dao;

import model.Artwork;
import util.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ArtworkDAO implements DAO<Artwork> {
    public static final String SELECT =
            "SELECT a.*, u.name AS artist_name FROM artworks a JOIN users u ON a.artist_id = u.id ";

    public static Artwork map(ResultSet rs) throws SQLException {
        Artwork a = new Artwork(rs.getInt("id"), rs.getInt("artist_id"), rs.getString("title"),
                rs.getString("description"), rs.getString("image_path"),
                rs.getDouble("price"), rs.getString("status"));
        a.setArtistName(rs.getString("artist_name"));
        return a;
    }

    @Override
    public boolean add(Artwork a) throws SQLException {
        return DB.update("INSERT INTO artworks (artist_id, title, description, image_path, price) VALUES (?, ?, ?, ?, ?)",
                a.getArtistId(), a.getTitle(), a.getDescription(), a.getImagePath(), a.getPrice()) > 0;
    }

    @Override
    public Artwork getById(int id) throws SQLException {
        return DB.queryOne(SELECT + "WHERE a.id = ?", ArtworkDAO::map, id);
    }

    @Override
    public List<Artwork> getAll() throws SQLException {
        return DB.query(SELECT + "ORDER BY a.id DESC", ArtworkDAO::map);
    }

    @Override
    public boolean update(Artwork a) throws SQLException {
        return DB.update("UPDATE artworks SET title = ?, description = ?, image_path = ?, price = ? WHERE id = ?",
                a.getTitle(), a.getDescription(), a.getImagePath(), a.getPrice(), a.getId()) > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return DB.update("DELETE FROM artworks WHERE id = ?", id) > 0;
    }

    public List<Artwork> getByStatus(String status) throws SQLException {
        return DB.query(SELECT + "WHERE a.status = ? ORDER BY a.id DESC", ArtworkDAO::map, status);
    }

    public List<Artwork> getByArtist(int artistId) throws SQLException {
        return DB.query(SELECT + "WHERE a.artist_id = ? ORDER BY a.id DESC", ArtworkDAO::map, artistId);
    }

    // Gallery search: sirf APPROVED artworks, title ya artist ke naam se
    public List<Artwork> searchApproved(String keyword) throws SQLException {
        String k = "%" + keyword + "%";
        return DB.query(SELECT + "WHERE a.status = 'APPROVED' AND (a.title LIKE ? OR u.name LIKE ?) ORDER BY a.id DESC",
                ArtworkDAO::map, k, k);
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        return DB.update("UPDATE artworks SET status = ? WHERE id = ?", status, id) > 0;
    }

    // ---- Wishlist ----
    public boolean addWishlist(int userId, int artworkId) throws SQLException {
        return DB.update("INSERT IGNORE INTO wishlist (user_id, artwork_id) VALUES (?, ?)", userId, artworkId) > 0;
    }

    public boolean removeWishlist(int userId, int artworkId) throws SQLException {
        return DB.update("DELETE FROM wishlist WHERE user_id = ? AND artwork_id = ?", userId, artworkId) > 0;
    }

    public List<Artwork> getWishlist(int userId) throws SQLException {
        return DB.query(SELECT + "JOIN wishlist w ON w.artwork_id = a.id WHERE w.user_id = ?", ArtworkDAO::map, userId);
    }

    public List<Artwork> getPurchased(int userId) throws SQLException {
        return DB.query(SELECT + "JOIN orders o ON o.artwork_id = a.id WHERE o.user_id = ?", ArtworkDAO::map, userId);
    }
}
