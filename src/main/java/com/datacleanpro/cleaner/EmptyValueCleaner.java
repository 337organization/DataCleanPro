package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;
import com.datacleanpro.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 空值清洗器
 * 处理空值和空白值
 */
public class EmptyValueCleaner implements DataCleaner {
    
    private final boolean removeEmptyRows;
    private final String defaultValue;
    
    /**
     * 构造函数
     * @param removeEmptyRows 是否删除空行
     * @param defaultValue 默认值（如果为null则保留空字符串）
     */
    public EmptyValueCleaner(boolean removeEmptyRows, String defaultValue) {
        this.removeEmptyRows = removeEmptyRows;
        this.defaultValue = defaultValue;
    }
    
    /**
     * 构造函数（默认不删除空行，使用空字符串作为默认值）
     */
    public EmptyValueCleaner() {
        this(false, "");
    }
    
    @Override
    public CleanResult clean(List<DataRow> data) {
        if (data == null || data.isEmpty()) {
            return new CleanResult(0, 0, 0, "数据为空");
        }
        
        int totalRows = data.size();
        int affectedRows = 0;
        int removedRows = 0;
        List<DataRow> result = new ArrayList<>();
        
        for (DataRow row : data) {
            boolean hasEmpty = false;
            List<String> fields = row.getFields();
            
            if (fields != null) {
                for (int i = 0; i < fields.size(); i++) {
                    String field = fields.get(i);
                    if (StringUtil.isBlank(field)) {
                        hasEmpty = true;
                        // 填充默认值
                        fields.set(i, defaultValue != null ? defaultValue : "");
                    }
                }
            }
            
            if (hasEmpty) {
                affectedRows++;
            }
            
            if (removeEmptyRows && isCompletelyEmpty(row)) {
                removedRows++;
            } else {
                result.add(row);
            }
        }
        
        // 清空原列表并添加处理后的数据
        data.clear();
        data.addAll(result);
        
        LogUtil.info("空值清洗完成，总行数: " + totalRows + ", 受影响行数: " + affectedRows + ", 删除行数: " + removedRows);
        
        return new CleanResult(totalRows, affectedRows, removedRows, 
                              "处理空值: " + affectedRows + " 行, 删除空行: " + removedRows + " 行");
    }
    
    /**
     * 检查行是否完全为空
     * @param row 数据行
     * @return 是否完全为空
     */
    private boolean isCompletelyEmpty(DataRow row) {
        if (row.getFields() == null || row.getFields().isEmpty()) {
            return true;
        }
        
        for (String field : row.getFields()) {
            if (StringUtil.isNotBlank(field)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getName() {
        return "空值清洗器";
    }
    
    @Override
    public String getDescription() {
        return "处理空值和空白值，可选择删除空行或填充默认值";
    }
}
