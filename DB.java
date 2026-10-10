package util;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Generic JDBC helper: sab queries PreparedStatement se chalti hain
public class DB {

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper.map(rs));
            }
        }
        return list;
    }

    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public static int count(String sql, Object... params) throws SQLException {
        Integer v = queryOne(sql, rs -> rs.getInt(1), params);
        return v == null ? 0 : v;
    }

    // "label, number" wali query ko Map mein badalta hai (charts ke liye)
    public static Map<String, Number> pairs(String sql, Object... params) throws SQLException {
        Map<String, Number> map = new LinkedHashMap<>();
        for (Object[] row : query(sql, rs -> new Object[]{rs.getString(1), rs.getDouble(2)}, params))
            map.put((String) row[0], (Number) row[1]);
        return map;
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
    }
}
