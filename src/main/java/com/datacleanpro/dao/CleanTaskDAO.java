package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.CleanTask;
import com.datacleanpro.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 清洗任务数据访问对象
 * 提供清洗任务的CRUD操作
 */
public class CleanTaskDAO {

    /**
     * 插入清洗任务
     * @param cleanTask 清洗任务
     * @return 插入后的ID
     */
    public static Long insert(CleanTask cleanTask) {
        String sql = "INSERT INTO clean_task (file_id, task_type, status, rows_affected, detail, start_time, end_time, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, cleanTask.getFileId());
            stmt.setString(2, cleanTask.getTaskType());
            stmt.setString(3, cleanTask.getStatus());
            stmt.setInt(4, cleanTask.getRowsAffected());
            stmt.setString(5, cleanTask.getDetail());
            stmt.setTimestamp(6, cleanTask.getStartTime() != null ? Timestamp.valueOf(cleanTask.getStartTime()) : null);
            stmt.setTimestamp(7, cleanTask.getEndTime() != null ? Timestamp.valueOf(cleanTask.getEndTime()) : null);
            stmt.setTimestamp(8, Timestamp.valueOf(cleanTask.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    cleanTask.setId(id);
                    LogUtil.info("清洗任务插入成功，ID: " + id);
                    return id;
                }
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("插入清洗任务失败", e);
            throw new DatabaseImportException("插入清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 更新清洗任务
     * @param cleanTask 清洗任务
     * @return 是否更新成功
     */
    public static boolean update(CleanTask cleanTask) {
        String sql = "UPDATE clean_task SET status = ?, rows_affected = ?, detail = ?, start_time = ?, end_time = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cleanTask.getStatus());
            stmt.setInt(2, cleanTask.getRowsAffected());
            stmt.setString(3, cleanTask.getDetail());
            stmt.setTimestamp(4, cleanTask.getStartTime() != null ? Timestamp.valueOf(cleanTask.getStartTime()) : null);
            stmt.setTimestamp(5, cleanTask.getEndTime() != null ? Timestamp.valueOf(cleanTask.getEndTime()) : null);
            stmt.setLong(6, cleanTask.getId());
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("清洗任务更新成功，ID: " + cleanTask.getId());
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("更新清洗任务失败", e);
            throw new DatabaseImportException("更新清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID删除清洗任务
     * @param id 任务ID
     * @return 是否删除成功
     */
    public static boolean deleteById(Long id) {
        String sql = "DELETE FROM clean_task WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            LogUtil.info("清洗任务删除成功，ID: " + id);
            return affectedRows > 0;
        } catch (SQLException e) {
            LogUtil.error("删除清洗任务失败", e);
            throw new DatabaseImportException("删除清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * 根据ID查询清洗任务
     * @param id 任务ID
     * @return 清洗任务
     */
    public static CleanTask findById(Long id) {
        String sql = "SELECT * FROM clean_task WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToCleanTask(rs);
            }
            return null;
        } catch (SQLException e) {
            LogUtil.error("查询清洗任务失败", e);
            throw new DatabaseImportException("查询清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据文件ID查询所有清洗任务
     * @param fileId 文件ID
     * @return 清洗任务列表
     */
    public static List<CleanTask> findByFileId(Long fileId) {
        String sql = "SELECT * FROM clean_task WHERE file_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            
            rs = stmt.executeQuery();
            List<CleanTask> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToCleanTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            LogUtil.error("根据文件ID查询清洗任务失败", e);
            throw new DatabaseImportException("根据文件ID查询清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 查询所有清洗任务
     * @return 清洗任务列表
     */
    public static List<CleanTask> findAll() {
        String sql = "SELECT * FROM clean_task ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            
            rs = stmt.executeQuery();
            List<CleanTask> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToCleanTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            LogUtil.error("查询所有清洗任务失败", e);
            throw new DatabaseImportException("查询所有清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据状态查询清洗任务
     * @param status 状态
     * @return 清洗任务列表
     */
    public static List<CleanTask> findByStatus(String status) {
        String sql = "SELECT * FROM clean_task WHERE status = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            
            rs = stmt.executeQuery();
            List<CleanTask> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapResultSetToCleanTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            LogUtil.error("根据状态查询清洗任务失败", e);
            throw new DatabaseImportException("根据状态查询清洗任务失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 统计清洗任务数量
     * @return 任务数量
     */
    public static long count() {
        String sql = "SELECT COUNT(*) FROM clean_task";
        
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
            LogUtil.error("统计清洗任务数量失败", e);
            throw new DatabaseImportException("统计清洗任务数量失败", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    /**
     * 将ResultSet映射为CleanTask对象
     * @param rs ResultSet
     * @return CleanTask对象
     * @throws SQLException SQL异常
     */
    private static CleanTask mapResultSetToCleanTask(ResultSet rs) throws SQLException {
        CleanTask cleanTask = new CleanTask();
        cleanTask.setId(rs.getLong("id"));
        cleanTask.setFileId(rs.getLong("file_id"));
        cleanTask.setTaskType(rs.getString("task_type"));
        cleanTask.setStatus(rs.getString("status"));
        cleanTask.setRowsAffected(rs.getInt("rows_affected"));
        cleanTask.setDetail(rs.getString("detail"));
        
        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            cleanTask.setStartTime(startTime.toLocalDateTime());
        }
        
        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            cleanTask.setEndTime(endTime.toLocalDateTime());
        }
        
        cleanTask.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return cleanTask;
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
