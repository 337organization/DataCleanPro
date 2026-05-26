package com.datacleanpro.parser;

import com.datacleanpro.exception.FileFormatException;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.FileUtil;
import com.datacleanpro.util.LogUtil;

import java.io.File;
import java.util.List;

/**
 * 文件解析器抽象类
 * 定义文件解析的通用接口
 */
public abstract class FileParser {
    
    /**
     * 解析文件
     * @param file 文件
     * @return 数据行列表
     * @throws FileFormatException 文件格式异常
     */
    public abstract List<DataRow> parse(File file) throws FileFormatException;
    
    /**
     * 获取支持的文件扩展名
     * @return 支持的扩展名数组
     */
    public abstract String[] getSupportedExtensions();
    
    /**
     * 验证文件是否有效
     * @param file 文件
     * @return 是否有效
     */
    public boolean validateFile(File file) {
        if (file == null || !file.exists()) {
            LogUtil.warn("文件不存在: " + (file != null ? file.getAbsolutePath() : "null"));
            return false;
        }
        
        if (!file.canRead()) {
            LogUtil.warn("文件无法读取: " + file.getAbsolutePath());
            return false;
        }
        
        String extension = FileUtil.getFileExtension(file);
        boolean supported = false;
        for (String ext : getSupportedExtensions()) {
            if (ext.equalsIgnoreCase(extension)) {
                supported = true;
                break;
            }
        }
        
        if (!supported) {
            LogUtil.warn("不支持的文件类型: " + extension);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取文件类型描述
     * @return 文件类型描述
     */
    public abstract String getFileTypeDescription();
    
    /**
     * 解析文件并返回结果
     * @param file 文件
     * @return 解析结果
     */
    public ParseResult parseWithResult(File file) {
        ParseResult result = new ParseResult();
        result.setFileName(file.getName());
        result.setFilePath(file.getAbsolutePath());
        
        try {
            if (!validateFile(file)) {
                result.setSuccess(false);
                result.setMessage("文件验证失败");
                return result;
            }
            
            List<DataRow> rows = parse(file);
            result.setSuccess(true);
            result.setRows(rows);
            result.setRowCount(rows.size());
            result.setMessage("解析成功");
            
            LogUtil.info("文件解析成功: " + file.getName() + ", 行数: " + rows.size());
        } catch (FileFormatException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            LogUtil.error("文件解析失败: " + file.getName(), e);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("解析过程中发生未知错误: " + e.getMessage());
            LogUtil.error("文件解析异常: " + file.getName(), e);
        }
        
        return result;
    }
    
    /**
     * 解析结果内部类
     */
    public static class ParseResult {
        private String fileName;
        private String filePath;
        private boolean success;
        private String message;
        private List<DataRow> rows;
        private int rowCount;
        
        public ParseResult() {}
        
        // Getters and Setters
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public List<DataRow> getRows() {
            return rows;
        }
        
        public void setRows(List<DataRow> rows) {
            this.rows = rows;
        }
        
        public int getRowCount() {
            return rowCount;
        }
        
        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }
    }
}
