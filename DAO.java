package dao;

import java.sql.SQLException;
import java.util.List;

// Generic interface: T kisi bhi model class ke liye use ho sakta hai
public interface DAO<T> {
    boolean add(T item) throws SQLException;
    T getById(int id) throws SQLException;
    List<T> getAll() throws SQLException;
    boolean update(T item) throws SQLException;
    boolean delete(int id) throws SQLException;
}
