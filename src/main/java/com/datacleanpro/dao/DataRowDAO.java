package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据行数据访问对象
 * 提供数据行的CRUD操作
 */
public class DataRowDAO {

    /**
     * 插入数据行
     * @param dataRow 数据行
     * @return 插入后的ID
     */
    public static Long insert(DataRow dataRow) {
        String sql = "INSERT INTO data_row (file_id, row_index, row_data, is_deleted, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, dataRow.getFileId());
            stmt.setInt(2, dataRow.getRowIndex());
            stmt.setString(3, convertFieldsToJson(dataRow.getFields()));
            stmt.setBoolean(4, dataRow.isDeleted());
            stmt.setTimestamp(5, Timestamp.valueOf(dataRow.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    dataRow.setId(id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入数据行失败", e);
            throw new DatabaseImportException("插入数据行失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 批量插入数据行
     * @param dataRows 数据行列表
     * @return 插入的行数
     */
    public static int batchInsert(List<DataRow> dataRows) {
        String sql = "INSERT INTO data_row (file_id, row_index, row_data, is_deleted, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            DBConnection.beginTransaction(conn);
            stmt = conn.prepareStatement(sql);
            
            for (DataRow dataRow : dataRows) {
                stmt.setLong(1, dataRow.getFileId());
                stmt.setInt(2, dataRow.getRowIndex());
                stmt.setString(3, convertFieldsToJson(dataRow.getFields()));
                stmt.setBoolean(4, dataRow.isDeleted());
                stmt.setTimestamp(5, Timestamp.valueOf(dataRow.getCreatedAt()));
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            DBConnection.commitTransaction(conn);
            
            int totalInserted = 0;
            for (int result : results) {
                totalInserted += result;
            }
            LogUtil.info("批量插入数据行成功，数量: " + totalInserted);
            return totalInserted;
        } catch (SQLException e) {
            LogUtil.error("批量插入数据行失败", e);
            try {
                if (conn != null) {
                    DBConnection.rollbackTransaction(conn);
                }
            } catch (SQLException ex) {
                LogUtil.error("回滚事务失败", ex);
            }
            throw new DatabaseImportException("批量插入数据行失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 更新数据行
     * @param dataRow 数据行
     * @return 是否更新成功
     */
    public static boolean update(DataRow dataRow) {
        String sql = "UPDATE data_row SET row_data = ?, is_deleted = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, convertFieldsToJson(dataRow.getFields()));
            stmt.setBoolean(2, dataRow.isDeleted());
            stmt.setLong(3, dataRow.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("更新数据行失败", e);
            throw new DatabaseImportException("更新数据行失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID删除数据行
     * @param id 数据行ID
     * @return 是否删除成功
     */
    public static boolean deleteById(Long id) {
        String sql = "DELETE FROM data_row WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("删除数据行失败", e);
            throw new DatabaseImportException("删除数据行失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据文件ID删除所有数据行
     * @param fileId 文件ID
     * @return 删除的行数
     */
    public static int deleteByFileId(Long fileId) {
        String sql = "DELETE FROM data_row WHERE file_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("根据文件ID删除数据行成功，文件ID: " + fileId + ", 删除行数: " + affectedRows);
            return affectedRows;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID删除数据行失败", e);
            throw new DatabaseImportException("根据文件ID删除数据行失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID查询数据行
     * @param id 数据行ID
     * @return 数据行
     */
    public static DataRow findById(Long id) {
        String sql = "SELECT * FROM data_row WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDataRow(rs);
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("查询数据行失败", e);
            throw new DatabaseImportException("查询数据行失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID查询所有数据行
     * @param fileId 文件ID
     * @return 数据行列表
     */
    public static List<DataRow> findByFileId(Long fileId) {
        String sql = "SELECT * FROM data_row WHERE file_id = ? ORDER BY row_index";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            rs = stmt.executeQuery();
            List<DataRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(mapResultSetToDataRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID查询数据行失败", e);
            throw new DatabaseImportException("根据文件ID查询数据行失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID分页查询数据行
     * @param fileId 文件ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 数据行列表
     */
    public static List<DataRow> findByFileIdWithPagination(Long fileId, int offset, int limit) {
        String sql = "SELECT * FROM data_row WHERE file_id = ? ORDER BY row_index LIMIT ? OFFSET ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            
            rs = stmt.executeQuery();
            List<DataRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(mapResultSetToDataRow(rs));
            }
            return rows;
        } catch (SQLException e) {
            LogUtil.error("分页查询数据行失败", e);
            throw new DatabaseImportException("分页查询数据行失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计文件的数据行数量
     * @param fileId 文件ID
     * @return 数据行数量
     */
    public static long countByFileId(Long fileId) {
        String sql = "SELECT COUNT(*) FROM data_row WHERE file_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            LogUtil.error("统计数据行数量失败", e);
            throw new DatabaseImportException("统计数据行数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 软删除数据行
     * @param id 数据行ID
     * @return 是否成功
     */
    public static boolean softDelete(Long id) {
        String sql = "UPDATE data_row SET is_deleted = TRUE WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("软删除数据行失败", e);
            throw new DatabaseImportException("软删除数据行失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 将字段列表转换为JSON字符串
     * @param fields 字段列表
     * @return JSON字符串
     */
    private static String convertFieldsToJson(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(fields.get(i) != null ? fields.get(i).replace("\"", "\\\"") : "").append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将JSON字符串转换为字段列表
     * @param json JSON字符串
     * @return 字段列表
     */
    private static List<String> convertJsonToFields(String json) {
        List<String> fields = new ArrayList<>();
        if (json == null || json.isEmpty() || "[]".equals(json)) {
            return fields;
        }
        
        // 简单的JSON数组解析
        json = json.trim();
        if (json.startsWith("[") && json.endsWith("]")) {
            json = json.substring(1, json.length() - 1);
            if (!json.isEmpty()) {
                String[] items = json.split(",");
                for (String item : items) {
                    item = item.trim();
                    if (item.startsWith("\"") && item.endsWith("\"")) {
                        item = item.substring(1, item.length() - 1);
                    }
                    fields.add(item);
                }
            }
        }
        return fields;
    }

    /**
     * 将ResultSet映射为DataRow对象
     * @param rs ResultSet
     * @return DataRow对象
     * @throws SQLException SQL异常
     */
    private static DataRow mapResultSetToDataRow(ResultSet rs) throws SQLException {
        DataRow dataRow = new DataRow();
        dataRow.setId(rs.getLong("id"));
        dataRow.setFileId(rs.getLong("file_id"));
        dataRow.setRowIndex(rs.getInt("row_index"));
        dataRow.setFields(convertJsonToFields(rs.getString("row_data")));
        dataRow.setDeleted(rs.getBoolean("is_deleted"));
        dataRow.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return dataRow;
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
