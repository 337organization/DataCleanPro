package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 重复数据清洗器
 * 去除重复的数据行
 */
public class DuplicateCleaner implements DataCleaner {
    
    @Override
    public CleanResult clean(List<DataRow> data) {
        if (data == null || data.isEmpty()) {
            return new CleanResult(0, 0, 0, "数据为空");
        }
        
        int totalRows = data.size();
        Set<String> seen = new LinkedHashSet<>();
        List<DataRow> uniqueRows = new ArrayList<>();
        int duplicateCount = 0;
        
        for (DataRow row : data) {
            String key = generateRowKey(row);
            if (seen.add(key)) {
                uniqueRows.add(row);
            } else {
                duplicateCount++;
            }
        }
        
        // 清空原列表并添加去重后的数据
        data.clear();
        data.addAll(uniqueRows);
        
        LogUtil.info("重复数据清洗完成，总行数: " + totalRows + ", 重复行数: " + duplicateCount + ", 剩余行数: " + uniqueRows.size());
        
        return new CleanResult(totalRows, duplicateCount, duplicateCount, 
                              "去除重复行: " + duplicateCount + " 行");
    }
    
    /**
     * 生成行的唯一标识
     * @param row 数据行
     * @return 唯一标识
     */
    private String generateRowKey(DataRow row) {
        if (row.getFields() == null || row.getFields().isEmpty()) {
            return String.valueOf(row.getRowIndex());
        }
        
        StringBuilder sb = new StringBuilder();
        for (String field : row.getFields()) {
            sb.append(field != null ? field.trim() : "").append("|");
        }
        return sb.toString();
    }
    
    @Override
    public String getName() {
        return "重复数据清洗器";
    }
    
    @Override
    public String getDescription() {
        return "去除重复的数据行，保留唯一记录";
    }
}
