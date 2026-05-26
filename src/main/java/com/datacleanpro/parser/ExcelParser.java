package com.datacleanpro.parser;

import com.datacleanpro.exception.FileFormatException;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel文件解析器
 * 支持解析.xlsx和.xls格式的Excel文件
 */
public class ExcelParser extends FileParser {
    
    private static final String[] SUPPORTED_EXTENSIONS = {"xlsx", "xls"};
    
    @Override
    public List<DataRow> parse(File file) throws FileFormatException {
        List<DataRow> rows = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = createWorkbook(fis, file.getName())) {
            
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new FileFormatException("Excel文件中没有工作表", file.getAbsolutePath(), "至少一个工作表", "无工作表");
            }
            
            int lastRowNum = sheet.getLastRowNum();
            LogUtil.info("开始解析Excel文件: " + file.getName() + ", 总行数: " + (lastRowNum + 1));
            
            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                DataRow dataRow = new DataRow();
                dataRow.setRowIndex(i);
                
                List<String> fields = new ArrayList<>();
                int lastCellNum = row.getLastCellNum();
                
                for (int j = 0; j < lastCellNum; j++) {
                    Cell cell = row.getCell(j);
                    String value = getCellValueAsString(cell);
                    fields.add(value);
                }
                
                dataRow.setFields(fields);
                rows.add(dataRow);
            }
            
            LogUtil.info("Excel文件解析完成: " + file.getName() + ", 解析行数: " + rows.size());
            
        } catch (IOException e) {
            throw new FileFormatException("读取Excel文件失败: " + e.getMessage(), 
                                        file.getAbsolutePath(), "可读取的Excel文件", "读取失败");
        } catch (Exception e) {
            throw new FileFormatException("解析Excel文件失败: " + e.getMessage(), 
                                        file.getAbsolutePath(), "有效的Excel文件", "解析失败");
        }
        
        return rows;
    }
    
    /**
     * 根据文件名创建相应的工作簿
     * @param fis 文件输入流
     * @param fileName 文件名
     * @return 工作簿
     * @throws IOException IO异常
     */
    private Workbook createWorkbook(FileInputStream fis, String fileName) throws IOException {
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IOException("不支持的Excel文件格式: " + fileName);
        }
    }
    
    /**
     * 获取单元格的值并转换为字符串
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            case BLANK:
                return "";
            case ERROR:
                return "ERROR";
            default:
                return "";
        }
    }
    
    @Override
    public String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
    
    @Override
    public String getFileTypeDescription() {
        return "Excel文件 (*.xlsx, *.xls)";
    }
}
