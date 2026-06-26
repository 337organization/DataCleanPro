package com.datacleanpro.parser;

import com.datacleanpro.exception.FileFormatException;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV文件解析器
 * 支持解析.csv格式的文件
 */
public class CsvParser extends FileParser {
    
    private static final String[] SUPPORTED_EXTENSIONS = {"csv"};
    
    @Override
    public List<DataRow> parse(File file) throws FileFormatException {
        List<DataRow> rows = new ArrayList<>();
        
        try (FileReader fileReader = new FileReader(file);
             CSVReader csvReader = new CSVReaderBuilder(fileReader).build()) {
            
            LogUtil.info("开始解析CSV文件: " + file.getName());
            
            List<String[]> allRows = csvReader.readAll();
            int rowIndex = 0;
            
            for (String[] csvRow : allRows) {
                DataRow dataRow = new DataRow();
                dataRow.setRowIndex(rowIndex);
                
                List<String> fields = new ArrayList<>();
                for (String field : csvRow) {
                    fields.add(field != null ? field.trim() : "");
                }
                
                dataRow.setFields(fields);
                rows.add(dataRow);
                rowIndex++;
            }
            
            LogUtil.info("CSV文件解析完成: " + file.getName() + ", 解析行数: " + rows.size());
            
        } catch (IOException e) {
            throw new FileFormatException("读取CSV文件失败: " + e.getMessage(), 
                                        file.getAbsolutePath(), "可读取的CSV文件", "读取失败");
        } catch (CsvException e) {
            throw new FileFormatException("解析CSV文件失败: " + e.getMessage(), 
                                        file.getAbsolutePath(), "有效的CSV文件", "解析失败");
        } catch (Exception e) {
            throw new FileFormatException("解析CSV文件失败: " + e.getMessage(), 
                                        file.getAbsolutePath(), "有效的CSV文件", "解析失败");
        }
        
        return rows;
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
    
    @Override
    public String getFileTypeDescription() {
        return "CSV文件 (*.csv)";
    }
}
