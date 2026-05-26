package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.util.DateUtil;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据文件数据访问对象
 * 提供数据文件的CRUD操作
 */
public class DataFileDAO {

    /**
     * 插入数据文件
     * @param dataFile 数据文件
     * @return 插入后的ID
     */
    public static Long insert(DataFile dataFile) {
        String sql = "INSERT INTO data_file (file_name, file_path, file_type, row_count, column_count, import_time, status, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, dataFile.getFileName());
            stmt.setString(2, dataFile.getFilePath());
            stmt.setString(3, dataFile.getFileType());
            stmt.setInt(4, dataFile.getRowCount());
            stmt.setInt(5, dataFile.getColumnCount());
            stmt.setTimestamp(6, Timestamp.valueOf(dataFile.getImportTime()));
            stmt.setString(7, dataFile.getStatus());
            stmt.setString(8, dataFile.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    dataFile.setId(id);
                    LogUtil.info("数据文件插入成功，ID: " + id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入数据文件失败", e);
            throw new DatabaseImportException("插入数据文件失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 更新数据文件
     * @param dataFile 数据文件
     * @return 是否更新成功
     */
    public static boolean update(DataFile dataFile) {
        String sql = "UPDATE data_file SET file_name = ?, file_path = ?, file_type = ?, row_count = ?, " +
                    "column_count = ?, status = ?, description = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, dataFile.getFileName());
            stmt.setString(2, dataFile.getFilePath());
            stmt.setString(3, dataFile.getFileType());
            stmt.setInt(4, dataFile.getRowCount());
            stmt.setInt(5, dataFile.getColumnCount());
            stmt.setString(6, dataFile.getStatus());
            stmt.setString(7, dataFile.getDescription());
            stmt.setLong(8, dataFile.getId());
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("数据文件更新成功，ID: " + dataFile.getId());
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("更新数据文件失败", e);
            throw new DatabaseImportException("更新数据文件失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID删除数据文件
     * @param id 文件ID
     * @return 是否删除成功
     */
    public static boolean deleteById(Long id) {
        String sql = "DELETE FROM data_file WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("数据文件删除成功，ID: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("删除数据文件失败", e);
            throw new DatabaseImportException("删除数据文件失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID查询数据文件
     * @param id 文件ID
     * @return 数据文件
     */
    public static DataFile findById(Long id) {
        String sql = "SELECT * FROM data_file WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDataFile(rs);
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("查询数据文件失败", e);
            throw new DatabaseImportException("查询数据文件失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 查询所有数据文件
     * @return 数据文件列表
     */
    public static List<DataFile> findAll() {
        String sql = "SELECT * FROM data_file ORDER BY import_time DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            List<DataFile> files = new ArrayList<>();
            while (rs.next()) {
                files.add(mapResultSetToDataFile(rs));
            }
            return files;
        } catch (SQLException e) {
            LogUtil.error("查询所有数据文件失败", e);
            throw new DatabaseImportException("查询所有数据文件失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据状态查询数据文件
     * @param status 状态
     * @return 数据文件列表
     */
    public static List<DataFile> findByStatus(String status) {
        String sql = "SELECT * FROM data_file WHERE status = ? ORDER BY import_time DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            
            rs = stmt.executeQuery();
            List<DataFile> files = new ArrayList<>();
            while (rs.next()) {
                files.add(mapResultSetToDataFile(rs));
            }
            return files;
        } catch (SQLException e) {
            LogUtil.error("根据状态查询数据文件失败", e);
            throw new DatabaseImportException("根据状态查询数据文件失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件类型查询数据文件
     * @param fileType 文件类型
     * @return 数据文件列表
     */
    public static List<DataFile> findByFileType(String fileType) {
        String sql = "SELECT * FROM data_file WHERE file_type = ? ORDER BY import_time DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fileType);
            
            rs = stmt.executeQuery();
            List<DataFile> files = new ArrayList<>();
            while (rs.next()) {
                files.add(mapResultSetToDataFile(rs));
            }
            return files;
        } catch (SQLException e) {
            LogUtil.error("根据文件类型查询数据文件失败", e);
            throw new DatabaseImportException("根据文件类型查询数据文件失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计数据文件数量
     * @return 文件数量
     */
    public static long count() {
        String sql = "SELECT COUNT(*) FROM data_file";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            LogUtil.error("统计数据文件数量失败", e);
            throw new DatabaseImportException("统计数据文件数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 将ResultSet映射为DataFile对象
     * @param rs ResultSet
     * @return DataFile对象
     * @throws SQLException SQL异常
     */
    private static DataFile mapResultSetToDataFile(ResultSet rs) throws SQLException {
        DataFile dataFile = new DataFile();
        dataFile.setId(rs.getLong("id"));
        dataFile.setFileName(rs.getString("file_name"));
        dataFile.setFilePath(rs.getString("file_path"));
        dataFile.setFileType(rs.getString("file_type"));
        dataFile.setRowCount(rs.getInt("row_count"));
        dataFile.setColumnCount(rs.getInt("column_count"));
        dataFile.setImportTime(rs.getTimestamp("import_time").toLocalDateTime());
        dataFile.setStatus(rs.getString("status"));
        dataFile.setDescription(rs.getString("description"));
        return dataFile;
    }

    /**
     * 关闭资源
     * @param conn 连接
     * @param stmt 语句
     * @param rs 结果集
     */
    private static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LogUtil.warn("关闭ResultSet失败", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LogUtil.warn("关闭PreparedStatement失败", e);
            }
        }
        DBConnection.release(conn);
    }
}
