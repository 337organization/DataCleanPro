package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.TaskHistory;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务历史数据访问对象
 * 提供任务历史的CRUD操作
 */
public class TaskHistoryDAO {

    /**
     * 插入任务历史
     * @param history 任务历史
     * @return 插入后的ID
     */
    public static Long insert(TaskHistory history) {
        String sql = "INSERT INTO task_history (task_id, action, target, detail, status, error_message, execution_time, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, history.getTaskId() != null ? history.getTaskId() : 0);
            stmt.setString(2, history.getAction());
            stmt.setString(3, history.getTarget());
            stmt.setString(4, history.getDetail());
            stmt.setString(5, history.getStatus());
            stmt.setString(6, history.getErrorMessage());
            stmt.setLong(7, history.getExecutionTime() != null ? history.getExecutionTime() : 0);
            stmt.setTimestamp(8, Timestamp.valueOf(history.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    history.setId(id);
                    LogUtil.audit(history.getAction(), history.getTarget(), history.getStatus(), history.getDetail());
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入任务历史失败", e);
            throw new DatabaseImportException("插入任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 查询所有任务历史
     * @return 任务历史列表
     */
    public static List<TaskHistory> findAll() {
        String sql = "SELECT * FROM task_history ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            List<TaskHistory> histories = new ArrayList<>();
            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
            return histories;
        } catch (SQLException e) {
            LogUtil.error("查询所有任务历史失败", e);
            throw new DatabaseImportException("查询所有任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据ID查询任务历史
     * @param id 历史ID
     * @return 任务历史
     */
    public static TaskHistory findById(Long id) {
        String sql = "SELECT * FROM task_history WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTaskHistory(rs);
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("查询任务历史失败", e);
            throw new DatabaseImportException("查询任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据任务ID查询任务历史
     * @param taskId 任务ID
     * @return 任务历史列表
     */
    public static List<TaskHistory> findByTaskId(Long taskId) {
        String sql = "SELECT * FROM task_history WHERE task_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, taskId);
            
            rs = stmt.executeQuery();
            List<TaskHistory> histories = new ArrayList<>();
            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
            return histories;
        } catch (SQLException e) {
            LogUtil.error("根据任务ID查询任务历史失败", e);
            throw new DatabaseImportException("根据任务ID查询任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据操作类型查询任务历史
     * @param action 操作类型
     * @return 任务历史列表
     */
    public static List<TaskHistory> findByAction(String action) {
        String sql = "SELECT * FROM task_history WHERE action = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, action);
            
            rs = stmt.executeQuery();
            List<TaskHistory> histories = new ArrayList<>();
            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
            return histories;
        } catch (SQLException e) {
            LogUtil.error("根据操作类型查询任务历史失败", e);
            throw new DatabaseImportException("根据操作类型查询任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据状态查询任务历史
     * @param status 状态
     * @return 任务历史列表
     */
    public static List<TaskHistory> findByStatus(String status) {
        String sql = "SELECT * FROM task_history WHERE status = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            
            rs = stmt.executeQuery();
            List<TaskHistory> histories = new ArrayList<>();
            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
            return histories;
        } catch (SQLException e) {
            LogUtil.error("根据状态查询任务历史失败", e);
            throw new DatabaseImportException("根据状态查询任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 分页查询任务历史
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 任务历史列表
     */
    public static List<TaskHistory> findWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM task_history ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            rs = stmt.executeQuery();
            List<TaskHistory> histories = new ArrayList<>();
            while (rs.next()) {
                histories.add(mapResultSetToTaskHistory(rs));
            }
            return histories;
        } catch (SQLException e) {
            LogUtil.error("分页查询任务历史失败", e);
            throw new DatabaseImportException("分页查询任务历史失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计任务历史数量
     * @return 任务历史数量
     */
    public static long count() {
        String sql = "SELECT COUNT(*) FROM task_history";
        
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
            LogUtil.error("统计任务历史数量失败", e);
            throw new DatabaseImportException("统计任务历史数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据ID删除任务历史
     * @param id 历史ID
     * @return 是否删除成功
     */
    public static boolean deleteById(Long id) {
        String sql = "DELETE FROM task_history WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("任务历史删除成功，ID: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("删除任务历史失败", e);
            throw new DatabaseImportException("删除任务历史失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 将ResultSet映射为TaskHistory对象
     * @param rs ResultSet
     * @return TaskHistory对象
     * @throws SQLException SQL异常
     */
    private static TaskHistory mapResultSetToTaskHistory(ResultSet rs) throws SQLException {
        TaskHistory history = new TaskHistory();
        history.setId(rs.getLong("id"));
        history.setTaskId(rs.getLong("task_id"));
        history.setAction(rs.getString("action"));
        history.setTarget(rs.getString("target"));
        history.setDetail(rs.getString("detail"));
        history.setStatus(rs.getString("status"));
        history.setErrorMessage(rs.getString("error_message"));
        history.setExecutionTime(rs.getLong("execution_time"));
        history.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return history;
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
