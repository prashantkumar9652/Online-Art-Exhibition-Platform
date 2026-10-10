package dao;

import util.DB;

import java.sql.SQLException;
import java.util.Map;

public class SettingsDAO {

    public Map<String, String> getAll() throws SQLException {
        Map<String, String> map = new java.util.LinkedHashMap<>();
        for (String[] row : DB.query("SELECT setting_key, setting_value FROM settings ORDER BY setting_key",
                rs -> new String[]{rs.getString(1), rs.getString(2)}))
            map.put(row[0], row[1]);
        return map;
    }

    public String get(String key, String defaultValue) throws SQLException {
        String v = DB.queryOne("SELECT setting_value FROM settings WHERE setting_key = ?", rs -> rs.getString(1), key);
        return v == null ? defaultValue : v;
    }

    public void set(String key, String value) throws SQLException {
        DB.update("INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)", key, value);
    }

    public boolean delete(String key) throws SQLException {
        return DB.update("DELETE FROM settings WHERE setting_key = ?", key) > 0;
    }
}
