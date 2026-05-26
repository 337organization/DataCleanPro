package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.ValidationResult;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果数据访问对象
 * 提供验证结果的CRUD操作
 */
public class ValidationResultDAO {

    /**
     * 插入验证结果
     * @param result 验证结果
     * @return 插入后的ID
     */
    public static Long insert(ValidationResult result) {
        String sql = "INSERT INTO validation_result (file_id, rule_id, row_index, column_name, cell_value, is_passed, error_message, validated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, result.getFileId());
            stmt.setLong(2, result.getRuleId());
            stmt.setInt(3, result.getRowIndex());
            stmt.setString(4, result.getColumnName());
            stmt.setString(5, result.getCellValue());
            stmt.setBoolean(6, result.isPassed());
            stmt.setString(7, result.getErrorMessage());
            stmt.setTimestamp(8, Timestamp.valueOf(result.getValidatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    result.setId(id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入验证结果失败", e);
            throw new DatabaseImportException("插入验证结果失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 批量插入验证结果
     * @param results 验证结果列表
     * @return 插入的行数
     */
    public static int batchInsert(List<ValidationResult> results) {
        String sql = "INSERT INTO validation_result (file_id, rule_id, row_index, column_name, cell_value, is_passed, error_message, validated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            DBConnection.beginTransaction(conn);
            stmt = conn.prepareStatement(sql);
            
            for (ValidationResult result : results) {
                stmt.setLong(1, result.getFileId());
                stmt.setLong(2, result.getRuleId());
                stmt.setInt(3, result.getRowIndex());
                stmt.setString(4, result.getColumnName());
                stmt.setString(5, result.getCellValue());
                stmt.setBoolean(6, result.isPassed());
                stmt.setString(7, result.getErrorMessage());
                stmt.setTimestamp(8, Timestamp.valueOf(result.getValidatedAt()));
                stmt.addBatch();
            }
            
            int[] resultsArray = stmt.executeBatch();
            DBConnection.commitTransaction(conn);
            
            int totalInserted = 0;
            for (int result : resultsArray) {
                totalInserted += result;
            }
            LogUtil.info("批量插入验证结果成功，数量: " + totalInserted);
            return totalInserted;
        } catch (SQLException e) {
            LogUtil.error("批量插入验证结果失败", e);
            try {
                if (conn != null) {
                    DBConnection.rollbackTransaction(conn);
                }
            } catch (SQLException ex) {
                LogUtil.error("回滚事务失败", ex);
            }
            throw new DatabaseImportException("批量插入验证结果失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据文件ID查询验证结果
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> findByFileId(Long fileId) {
        String sql = "SELECT * FROM validation_result WHERE file_id = ? ORDER BY row_index";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            rs = stmt.executeQuery();
            List<ValidationResult> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToValidationResult(rs));
            }
            return results;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID查询验证结果失败", e);
            throw new DatabaseImportException("根据文件ID查询验证结果失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID和规则ID查询验证结果
     * @param fileId 文件ID
     * @param ruleId 规则ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> findByFileIdAndRuleId(Long fileId, Long ruleId) {
        String sql = "SELECT * FROM validation_result WHERE file_id = ? AND rule_id = ? ORDER BY row_index";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            stmt.setLong(2, ruleId);
            
            rs = stmt.executeQuery();
            List<ValidationResult> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToValidationResult(rs));
            }
            return results;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID和规则ID查询验证结果失败", e);
            throw new DatabaseImportException("根据文件ID和规则ID查询验证结果失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID查询失败的验证结果
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> findFailedByFileId(Long fileId) {
        String sql = "SELECT * FROM validation_result WHERE file_id = ? AND is_passed = FALSE ORDER BY row_index";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            rs = stmt.executeQuery();
            List<ValidationResult> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToValidationResult(rs));
            }
            return results;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID查询失败的验证结果失败", e);
            throw new DatabaseImportException("根据文件ID查询失败的验证结果失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计文件验证结果数量
     * @param fileId 文件ID
     * @return 验证结果数量
     */
    public static long countByFileId(Long fileId) {
        String sql = "SELECT COUNT(*) FROM validation_result WHERE file_id = ?";
        
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
            LogUtil.error("统计文件验证结果数量失败", e);
            throw new DatabaseImportException("统计文件验证结果数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计文件失败的验证结果数量
     * @param fileId 文件ID
     * @return 失败的验证结果数量
     */
    public static long countFailedByFileId(Long fileId) {
        String sql = "SELECT COUNT(*) FROM validation_result WHERE file_id = ? AND is_passed = FALSE";
        
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
            LogUtil.error("统计文件失败的验证结果数量失败", e);
            throw new DatabaseImportException("统计文件失败的验证结果数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID删除验证结果
     * @param fileId 文件ID
     * @return 删除的行数
     */
    public static int deleteByFileId(Long fileId) {
        String sql = "DELETE FROM validation_result WHERE file_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("根据文件ID删除验证结果成功，文件ID: " + fileId + ", 删除行数: " + affectedRows);
            return affectedRows;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID删除验证结果失败", e);
            throw new DatabaseImportException("根据文件ID删除验证结果失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 将ResultSet映射为ValidationResult对象
     * @param rs ResultSet
     * @return ValidationResult对象
     * @throws SQLException SQL异常
     */
    private static ValidationResult mapResultSetToValidationResult(ResultSet rs) throws SQLException {
        ValidationResult result = new ValidationResult();
        result.setId(rs.getLong("id"));
        result.setFileId(rs.getLong("file_id"));
        result.setRuleId(rs.getLong("rule_id"));
        result.setRowIndex(rs.getInt("row_index"));
        result.setColumnName(rs.getString("column_name"));
        result.setCellValue(rs.getString("cell_value"));
        result.setPassed(rs.getBoolean("is_passed"));
        result.setErrorMessage(rs.getString("error_message"));
        result.setValidatedAt(rs.getTimestamp("validated_at").toLocalDateTime());
        return result;
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
