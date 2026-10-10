package dao;

import model.Artwork;
import model.Exhibition;
import util.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ExhibitionDAO implements DAO<Exhibition> {

    private static Exhibition map(ResultSet rs) throws SQLException {
        return new Exhibition(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getTimestamp("start_time").toLocalDateTime(), rs.getTimestamp("end_time").toLocalDateTime());
    }

    @Override
    public boolean add(Exhibition e) throws SQLException {
        return DB.update("INSERT INTO exhibitions (title, description, start_time, end_time) VALUES (?, ?, ?, ?)",
                e.getTitle(), e.getDescription(), e.getStartTime(), e.getEndTime()) > 0;
    }

    @Override
    public Exhibition getById(int id) throws SQLException {
        return DB.queryOne("SELECT * FROM exhibitions WHERE id = ?", ExhibitionDAO::map, id);
    }

    @Override
    public List<Exhibition> getAll() throws SQLException {
        return DB.query("SELECT * FROM exhibitions ORDER BY start_time DESC", ExhibitionDAO::map);
    }

    @Override
    public boolean update(Exhibition e) throws SQLException {
        return DB.update("UPDATE exhibitions SET title = ?, description = ?, start_time = ?, end_time = ? WHERE id = ?",
                e.getTitle(), e.getDescription(), e.getStartTime(), e.getEndTime(), e.getId()) > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return DB.update("DELETE FROM exhibitions WHERE id = ?", id) > 0;
    }

    // ---- Artist submissions (approval flow) ----
    public boolean submitArtwork(int exhibitionId, int artworkId) throws SQLException {
        return DB.update("INSERT IGNORE INTO exhibition_artworks (exhibition_id, artwork_id) VALUES (?, ?)",
                exhibitionId, artworkId) > 0;
    }

    private static final String SUB_SELECT =
            "SELECT ea.exhibition_id, ea.artwork_id, e.title, a.title, u.name, ea.approval_status " +
            "FROM exhibition_artworks ea JOIN exhibitions e ON e.id = ea.exhibition_id " +
            "JOIN artworks a ON a.id = ea.artwork_id JOIN users u ON u.id = a.artist_id ";

    private static Object[] subRow(ResultSet rs) throws SQLException {
        return new Object[]{rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)};
    }

    public List<Object[]> getSubmissions(String status) throws SQLException {
        return DB.query(SUB_SELECT + "WHERE ea.approval_status = ? ORDER BY e.id", ExhibitionDAO::subRow, status);
    }

    public List<Object[]> getSubmissionsByArtist(int artistId) throws SQLException {
        return DB.query(SUB_SELECT + "WHERE a.artist_id = ? ORDER BY e.id", ExhibitionDAO::subRow, artistId);
    }

    public boolean setSubmissionStatus(int exhibitionId, int artworkId, String status) throws SQLException {
        return DB.update("UPDATE exhibition_artworks SET approval_status = ? WHERE exhibition_id = ? AND artwork_id = ?",
                status, exhibitionId, artworkId) > 0;
    }

    public List<Artwork> getApprovedArtworks(int exhibitionId) throws SQLException {
        return DB.query(ArtworkDAO.SELECT + "JOIN exhibition_artworks ea ON ea.artwork_id = a.id " +
                "WHERE ea.exhibition_id = ? AND ea.approval_status = 'APPROVED'", ArtworkDAO::map, exhibitionId);
    }

    // ---- Participation ----
    public boolean join(int exhibitionId, int userId) throws SQLException {
        return DB.update("INSERT IGNORE INTO participants (exhibition_id, user_id) VALUES (?, ?)", exhibitionId, userId) > 0;
    }

    public boolean isJoined(int exhibitionId, int userId) throws SQLException {
        return DB.count("SELECT COUNT(*) FROM participants WHERE exhibition_id = ? AND user_id = ?", exhibitionId, userId) > 0;
    }

    public int participantCount(int exhibitionId) throws SQLException {
        return DB.count("SELECT COUNT(*) FROM participants WHERE exhibition_id = ?", exhibitionId);
    }

    public Map<String, Number> participantStats() throws SQLException {
        return DB.pairs("SELECT e.title, COUNT(p.user_id) FROM exhibitions e " +
                "LEFT JOIN participants p ON p.exhibition_id = e.id GROUP BY e.id, e.title");
    }
}
