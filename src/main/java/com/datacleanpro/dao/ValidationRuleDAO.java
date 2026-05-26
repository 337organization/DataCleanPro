package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.ValidationRule;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证规则数据访问对象
 * 提供验证规则的CRUD操作
 */
public class ValidationRuleDAO {

    /**
     * 插入验证规则
     * @param rule 验证规则
     * @return 插入后的ID
     */
    public static Long insert(ValidationRule rule) {
        String sql = "INSERT INTO validation_rule (rule_name, rule_type, target_column, expression, error_message, is_active, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, rule.getRuleName());
            stmt.setString(2, rule.getRuleType());
            stmt.setString(3, rule.getTargetColumn());
            stmt.setString(4, rule.getExpression());
            stmt.setString(5, rule.getErrorMessage());
            stmt.setBoolean(6, rule.isActive());
            stmt.setTimestamp(7, Timestamp.valueOf(rule.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    rule.setId(id);
                    LogUtil.info("验证规则插入成功，ID: " + id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入验证规则失败", e);
            throw new DatabaseImportException("插入验证规则失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 更新验证规则
     * @param rule 验证规则
     * @return 是否更新成功
     */
    public static boolean update(ValidationRule rule) {
        String sql = "UPDATE validation_rule SET rule_name = ?, rule_type = ?, target_column = ?, expression = ?, " +
                    "error_message = ?, is_active = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rule.getRuleName());
            stmt.setString(2, rule.getRuleType());
            stmt.setString(3, rule.getTargetColumn());
            stmt.setString(4, rule.getExpression());
            stmt.setString(5, rule.getErrorMessage());
            stmt.setBoolean(6, rule.isActive());
            stmt.setLong(7, rule.getId());
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("验证规则更新成功，ID: " + rule.getId());
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("更新验证规则失败", e);
            throw new DatabaseImportException("更新验证规则失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID删除验证规则
     * @param id 规则ID
     * @return 是否删除成功
     */
    public static boolean deleteById(Long id) {
        String sql = "DELETE FROM validation_rule WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("验证规则删除成功，ID: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("删除验证规则失败", e);
            throw new DatabaseImportException("删除验证规则失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID查询验证规则
     * @param id 规则ID
     * @return 验证规则
     */
    public static ValidationRule findById(Long id) {
        String sql = "SELECT * FROM validation_rule WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToValidationRule(rs);
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("查询验证规则失败", e);
            throw new DatabaseImportException("查询验证规则失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 查询所有验证规则
     * @return 验证规则列表
     */
    public static List<ValidationRule> findAll() {
        String sql = "SELECT * FROM validation_rule ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            List<ValidationRule> rules = new ArrayList<>();
            while (rs.next()) {
                rules.add(mapResultSetToValidationRule(rs));
            }
            return rules;
        } catch (SQLException e) {
            LogUtil.error("查询所有验证规则失败", e);
            throw new DatabaseImportException("查询所有验证规则失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据规则类型查询验证规则
     * @param ruleType 规则类型
     * @return 验证规则列表
     */
    public static List<ValidationRule> findByRuleType(String ruleType) {
        String sql = "SELECT * FROM validation_rule WHERE rule_type = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, ruleType);
            
            rs = stmt.executeQuery();
            List<ValidationRule> rules = new ArrayList<>();
            while (rs.next()) {
                rules.add(mapResultSetToValidationRule(rs));
            }
            return rules;
        } catch (SQLException e) {
            LogUtil.error("根据规则类型查询验证规则失败", e);
            throw new DatabaseImportException("根据规则类型查询验证规则失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 查询所有激活的验证规则
     * @return 验证规则列表
     */
    public static List<ValidationRule> findActiveRules() {
        String sql = "SELECT * FROM validation_rule WHERE is_active = TRUE ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            List<ValidationRule> rules = new ArrayList<>();
            while (rs.next()) {
                rules.add(mapResultSetToValidationRule(rs));
            }
            return rules;
        } catch (SQLException e) {
            LogUtil.error("查询激活的验证规则失败", e);
            throw new DatabaseImportException("查询激活的验证规则失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计验证规则数量
     * @return 规则数量
     */
    public static long count() {
        String sql = "SELECT COUNT(*) FROM validation_rule";
        
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
            LogUtil.error("统计验证规则数量失败", e);
            throw new DatabaseImportException("统计验证规则数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 将ResultSet映射为ValidationRule对象
     * @param rs ResultSet
     * @return ValidationRule对象
     * @throws SQLException SQL异常
     */
    private static ValidationRule mapResultSetToValidationRule(ResultSet rs) throws SQLException {
        ValidationRule rule = new ValidationRule();
        rule.setId(rs.getLong("id"));
        rule.setRuleName(rs.getString("rule_name"));
        rule.setRuleType(rs.getString("rule_type"));
        rule.setTargetColumn(rs.getString("target_column"));
        rule.setExpression(rs.getString("expression"));
        rule.setErrorMessage(rs.getString("error_message"));
        rule.setActive(rs.getBoolean("is_active"));
        rule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return rule;
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
