package com.datacleanpro.service;

import com.datacleanpro.dao.TaskHistoryDAO;
import com.datacleanpro.model.TaskHistory;
import com.datacleanpro.util.LogUtil;

import java.util.List;

/**
 * 任务历史服务
 * 管理任务历史记录
 */
public class TaskHistoryService {
    
    /**
     * 记录任务历史
     * @param history 任务历史
     * @return 历史ID
     */
    public static Long recordHistory(TaskHistory history) {
        try {
            Long id = TaskHistoryDAO.insert(history);
            LogUtil.info("任务历史记录成功: " + history.getAction() + " - " + history.getTarget());
            return id;
        } catch (Exception e) {
            LogUtil.error("记录任务历史失败", e);
            return null;
        }
    }
    
    /**
     * 获取所有任务历史
     * @return 任务历史列表
     */
    public static List<TaskHistory> getAllHistory() {
        return TaskHistoryDAO.findAll();
    }
    
    /**
     * 根据ID获取任务历史
     * @param id 历史ID
     * @return 任务历史
     */
    public static TaskHistory getHistoryById(Long id) {
        return TaskHistoryDAO.findById(id);
    }
    
    /**
     * 根据操作类型获取任务历史
     * @param action 操作类型
     * @return 任务历史列表
     */
    public static List<TaskHistory> getHistoryByAction(String action) {
        return TaskHistoryDAO.findByAction(action);
    }
    
    /**
     * 根据状态获取任务历史
     * @param status 状态
     * @return 任务历史列表
     */
    public static List<TaskHistory> getHistoryByStatus(String status) {
        return TaskHistoryDAO.findByStatus(status);
    }
    
    /**
     * 分页获取任务历史
     * @param page 页码
     * @param pageSize 每页大小
     * @return 任务历史列表
     */
    public static List<TaskHistory> getHistoryWithPagination(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return TaskHistoryDAO.findWithPagination(offset, pageSize);
    }
    
    /**
     * 获取任务历史总数
     * @return 总数
     */
    public static long getHistoryCount() {
        return TaskHistoryDAO.count();
    }
    
    /**
     * 删除任务历史
     * @param id 历史ID
     * @return 是否成功
     */
    public static boolean deleteHistory(Long id) {
        return TaskHistoryDAO.deleteById(id);
    }
}
