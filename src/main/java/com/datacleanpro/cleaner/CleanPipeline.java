package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 清洗管道类
 * 将多个清洗器组合成一个管道，按顺序执行
 */
public class CleanPipeline {
    
    private final List<DataCleaner> cleaners;
    
    public CleanPipeline() {
        this.cleaners = new ArrayList<>();
    }
    
    /**
     * 添加清洗器到管道
     * @param cleaner 清洗器
     * @return 管道本身（支持链式调用）
     */
    public CleanPipeline addCleaner(DataCleaner cleaner) {
        if (cleaner != null) {
            this.cleaners.add(cleaner);
        }
        return this;
    }
    
    /**
     * 执行所有清洗器
     * @param data 数据行列表
     * @return 清洗结果
     */
    public DataCleaner.CleanResult execute(List<DataRow> data) {
        if (data == null || data.isEmpty()) {
            return new DataCleaner.CleanResult(0, 0, 0, "数据为空");
        }
        
        DataCleaner.CleanResult totalResult = new DataCleaner.CleanResult();
        totalResult.setTotalRows(data.size());
        
        LogUtil.info("开始执行清洗管道，清洗器数量: " + cleaners.size());
        
        for (DataCleaner cleaner : cleaners) {
            LogUtil.info("执行清洗器: " + cleaner.getName());
            
            DataCleaner.CleanResult stepResult = cleaner.clean(data);
            totalResult.merge(stepResult);
            
            LogUtil.info("清洗器 " + cleaner.getName() + " 执行完成: " + stepResult);
        }
        
        LogUtil.info("清洗管道执行完成: " + totalResult);
        return totalResult;
    }
    
    /**
     * 获取管道中的清洗器数量
     * @return 清洗器数量
     */
    public int size() {
        return cleaners.size();
    }
    
    /**
     * 清空管道
     */
    public void clear() {
        cleaners.clear();
    }
    
    /**
     * 获取所有清洗器
     * @return 清洗器列表
     */
    public List<DataCleaner> getCleaners() {
        return new ArrayList<>(cleaners);
    }
    
    /**
     * 创建默认清洗管道
     * @return 默认清洗管道
     */
    public static CleanPipeline createDefault() {
        CleanPipeline pipeline = new CleanPipeline();
        pipeline.addCleaner(new DuplicateCleaner());
        pipeline.addCleaner(new EmptyValueCleaner());
        pipeline.addCleaner(new FormatCleaner());
        return pipeline;
    }
}
