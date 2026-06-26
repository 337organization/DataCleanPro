package com.datacleanpro.service;

import com.datacleanpro.dao.DataFileDAO;
import com.datacleanpro.dao.DataRowDAO;
import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.model.StatResult;
import com.datacleanpro.util.LogUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库服务
 * 处理数据库相关的业务逻辑
 */
public class DatabaseService {
    
    /**
     * 获取数据统计信息
     * @param fileId 文件ID
     * @return 统计结果
     */
    public static Map<String, Object> getStatistics(Long fileId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            DataFile dataFile = DataFileDAO.findById(fileId);
            if (dataFile == null) {
                return stats;
            }
            
            stats.put("fileName", dataFile.getFileName());
            stats.put("fileType", dataFile.getFileType());
            stats.put("totalRows", dataFile.getRowCount());
            stats.put("totalColumns", dataFile.getColumnCount());
            stats.put("status", dataFile.getStatus());
            stats.put("importTime", dataFile.getImportTime());
            
            // 获取实际数据行数
            long actualRows = DataRowDAO.countByFileId(fileId);
            stats.put("actualRows", actualRows);
            
            LogUtil.info("获取统计信息成功，文件ID: " + fileId);
        } catch (Exception e) {
            LogUtil.error("获取统计信息失败，文件ID: " + fileId, e);
            throw new DatabaseImportException("获取统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * 获取全局统计信息
     * @return 统计结果
     */
    public static Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long totalFiles = DataFileDAO.count();
            stats.put("totalFiles", totalFiles);
            
            // 可以添加更多统计信息
            stats.put("timestamp", LocalDateTime.now());
            
            LogUtil.info("获取全局统计信息成功");
        } catch (Exception e) {
            LogUtil.error("获取全局统计信息失败", e);
            throw new DatabaseImportException("获取全局统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * 测试数据库连接
     * @return 是否连接成功
     */
    public static boolean testConnection() {
        return com.datacleanpro.dao.DBConnection.testConnection();
    }
}
