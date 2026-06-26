package com.datacleanpro.parser;

import com.datacleanpro.exception.FileFormatException;
import com.datacleanpro.util.FileUtil;
import com.datacleanpro.util.LogUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析器工厂类
 * 根据文件类型创建相应的解析器
 */
public class ParserFactory {
    
    private static final Map<String, FileParser> PARSERS = new HashMap<>();
    
    static {
        // 注册Excel解析器
        ExcelParser excelParser = new ExcelParser();
        PARSERS.put("xlsx", excelParser);
        PARSERS.put("xls", excelParser);
        
        // 注册CSV解析器
        PARSERS.put("csv", new CsvParser());
        
        LogUtil.info("解析器工厂初始化完成，支持的文件类型: " + PARSERS.keySet());
    }
    
    /**
     * 根据文件扩展名获取解析器
     * @param fileExtension 文件扩展名
     * @return 文件解析器
     * @throws FileFormatException 文件格式异常
     */
    public static FileParser getParser(String fileExtension) throws FileFormatException {
        if (fileExtension == null || fileExtension.isEmpty()) {
            throw new FileFormatException("文件扩展名不能为空");
        }
        
        FileParser parser = PARSERS.get(fileExtension.toLowerCase());
        if (parser == null) {
            throw new FileFormatException("不支持的文件类型: " + fileExtension);
        }
        
        return parser;
    }
    
    /**
     * 根据文件获取解析器
     * @param file 文件
     * @return 文件解析器
     * @throws FileFormatException 文件格式异常
     */
    public static FileParser getParser(File file) throws FileFormatException {
        if (file == null) {
            throw new FileFormatException("文件不能为空");
        }
        
        String extension = FileUtil.getFileExtension(file);
        return getParser(extension);
    }
    
    /**
     * 根据文件名获取解析器
     * @param fileName 文件名
     * @return 文件解析器
     * @throws FileFormatException 文件格式异常
     */
    public static FileParser getParserByName(String fileName) throws FileFormatException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileFormatException("文件名不能为空");
        }
        
        String extension = FileUtil.getFileExtension(fileName);
        return getParser(extension);
    }
    
    /**
     * 检查是否支持指定文件类型
     * @param fileExtension 文件扩展名
     * @return 是否支持
     */
    public static boolean isSupported(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) {
            return false;
        }
        return PARSERS.containsKey(fileExtension.toLowerCase());
    }
    
    /**
     * 检查文件是否支持
     * @param file 文件
     * @return 是否支持
     */
    public static boolean isSupported(File file) {
        if (file == null) {
            return false;
        }
        String extension = FileUtil.getFileExtension(file);
        return isSupported(extension);
    }
    
    /**
     * 获取所有支持的文件类型
     * @return 支持的文件类型集合
     */
    public static java.util.Set<String> getSupportedTypes() {
        return PARSERS.keySet();
    }
    
    /**
     * 解析文件
     * @param file 文件
     * @return 解析结果
     * @throws FileFormatException 文件格式异常
     */
    public static FileParser.ParseResult parseFile(File file) throws FileFormatException {
        FileParser parser = getParser(file);
        return parser.parseWithResult(file);
    }
}
