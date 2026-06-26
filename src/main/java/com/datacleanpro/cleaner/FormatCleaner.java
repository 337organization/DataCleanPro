package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;
import com.datacleanpro.util.StringUtil;

import java.util.List;

/**
 * 格式清洗器
 * 统一数据格式，去除多余空格，统一大小写等
 */
public class FormatCleaner implements DataCleaner {
    
    private final boolean trimWhitespace;
    private final boolean normalizeCase;
    private final boolean removeSpecialChars;
    
    /**
     * 构造函数
     * @param trimWhitespace 是否去除多余空格
     * @param normalizeCase 是否统一大小写（转为小写）
     * @param removeSpecialChars 是否去除特殊字符
     */
    public FormatCleaner(boolean trimWhitespace, boolean normalizeCase, boolean removeSpecialChars) {
        this.trimWhitespace = trimWhitespace;
        this.normalizeCase = normalizeCase;
        this.removeSpecialChars = removeSpecialChars;
    }
    
    /**
     * 构造函数（默认去除空格，不统一大小写，不去除特殊字符）
     */
    public FormatCleaner() {
        this(true, false, false);
    }
    
    @Override
    public CleanResult clean(List<DataRow> data) {
        if (data == null || data.isEmpty()) {
            return new CleanResult(0, 0, 0, "数据为空");
        }
        
        int totalRows = data.size();
        int affectedRows = 0;
        
        for (DataRow row : data) {
            boolean rowAffected = false;
            List<String> fields = row.getFields();
            
            if (fields != null) {
                for (int i = 0; i < fields.size(); i++) {
                    String original = fields.get(i);
                    String processed = processField(original);
                    
                    if (!original.equals(processed)) {
                        fields.set(i, processed);
                        rowAffected = true;
                    }
                }
            }
            
            if (rowAffected) {
                affectedRows++;
            }
        }
        
        LogUtil.info("格式清洗完成，总行数: " + totalRows + ", 受影响行数: " + affectedRows);
        
        return new CleanResult(totalRows, affectedRows, 0, 
                              "格式处理: " + affectedRows + " 行");
    }
    
    /**
     * 处理单个字段
     * @param field 原始字段值
     * @return 处理后的字段值
     */
    private String processField(String field) {
        if (field == null) {
            return null;
        }
        
        String result = field;
        
        // 去除多余空格
        if (trimWhitespace) {
            result = result.trim();
            // 将多个连续空格替换为单个空格
            result = result.replaceAll("\\s+", " ");
        }
        
        // 统一大小写
        if (normalizeCase) {
            result = result.toLowerCase();
        }
        
        // 去除特殊字符（保留字母、数字、中文、常用标点）
        if (removeSpecialChars) {
            result = result.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s.,;:!?()\\[\\]{}\"'-+*/=@#$%^&]", "");
        }
        
        return result;
    }
    
    @Override
    public String getName() {
        return "格式清洗器";
    }
    
    @Override
    public String getDescription() {
        return "统一数据格式，去除多余空格，统一大小写等";
    }
}
