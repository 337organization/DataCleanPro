package com.datacleanpro.service;

import com.datacleanpro.dao.DataFileDAO;
import com.datacleanpro.dao.DataRowDAO;
import com.datacleanpro.dao.TaskHistoryDAO;
import com.datacleanpro.exception.DataCleanException;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.model.TaskHistory;
import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.DateUtil;
import com.datacleanpro.util.LogUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表服务
 * 生成和导出Excel报表
 */
public class ReportService {
    
    /**
     * 导出数据到Excel
     * @param fileId 文件ID
     * @return 导出文件路径
     */
    public static String exportToExcel(Long fileId) {
        LocalDateTime startTime = DateUtil.now();
        LogUtil.info("开始导出Excel，文件ID: " + fileId);
        
        try {
            // 1. 获取文件信息
            DataFile dataFile = DataFileDAO.findById(fileId);
            if (dataFile == null) {
                throw new DataCleanException("文件不存在");
            }
            
            // 2. 获取数据
            List<DataRow> rows = DataRowDAO.findByFileId(fileId);
            
            // 3. 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("数据导出");
            
            // 4. 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 5. 写入表头
            Row headerRow = sheet.createRow(0);
            if (!rows.isEmpty() && rows.get(0).getFields() != null) {
                for (int i = 0; i < rows.get(0).getFields().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue("列 " + (i + 1));
                    cell.setCellStyle(headerStyle);
                }
            }
            
            // 6. 写入数据
            for (int i = 0; i < rows.size(); i++) {
                DataRow row = rows.get(i);
                Row dataRow = sheet.createRow(i + 1);
                
                if (row.getFields() != null) {
                    for (int j = 0; j < row.getFields().size(); j++) {
                        Cell cell = dataRow.createCell(j);
                        cell.setCellValue(row.getFields().get(j) != null ? row.getFields().get(j) : "");
                    }
                }
            }
            
            // 7. 自动调整列宽
            if (!rows.isEmpty() && rows.get(0).getFields() != null) {
                for (int i = 0; i < rows.get(0).getFields().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            
            // 8. 保存文件
            String reportPath = ConfigUtil.getReportPath();
            String fileName = "export_" + dataFile.getFileName().replace(".", "_") + "_" + 
                            System.currentTimeMillis() + ".xlsx";
            String filePath = reportPath + fileName;
            
            // 确保目录存在
            File dir = new File(reportPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            
            workbook.close();
            
            // 9. 记录历史
            TaskHistory history = new TaskHistory("EXPORT", dataFile.getFileName());
            history.setDetail("导出成功，行数: " + rows.size());
            history.setExecutionTime(DateUtil.betweenMillis(startTime, DateUtil.now()));
            TaskHistoryService.recordHistory(history);
            
            LogUtil.info("Excel导出成功: " + filePath);
            return filePath;
            
        } catch (IOException e) {
            LogUtil.error("Excel导出失败", e);
            throw new DataCleanException("Excel导出失败: " + e.getMessage(), e);
        } catch (Exception e) {
            LogUtil.error("Excel导出异常", e);
            throw new DataCleanException("Excel导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建表头样式
     * @param workbook 工作簿
     * @return 单元格样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 导出历史记录到Excel
     * @return 导出文件路径
     */
    public static String exportHistoryToExcel() {
        LocalDateTime startTime = DateUtil.now();
        LogUtil.info("开始导出历史记录");
        
        try {
            // 1. 获取历史记录
            List<TaskHistory> histories = TaskHistoryDAO.findAll();
            
            // 2. 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("历史记录");
            
            // 3. 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "操作", "目标", "状态", "执行时间(ms)", "创建时间"};
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 4. 写入数据
            for (int i = 0; i < histories.size(); i++) {
                TaskHistory history = histories.get(i);
                Row dataRow = sheet.createRow(i + 1);
                
                dataRow.createCell(0).setCellValue(history.getId() != null ? history.getId() : 0);
                dataRow.createCell(1).setCellValue(history.getAction() != null ? history.getAction() : "");
                dataRow.createCell(2).setCellValue(history.getTarget() != null ? history.getTarget() : "");
                dataRow.createCell(3).setCellValue(history.getStatus() != null ? history.getStatus() : "");
                dataRow.createCell(4).setCellValue(history.getExecutionTime() != null ? history.getExecutionTime() : 0);
                dataRow.createCell(5).setCellValue(history.getCreatedAt() != null ? DateUtil.format(history.getCreatedAt()) : "");
            }
            
            // 5. 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 6. 保存文件
            String reportPath = ConfigUtil.getReportPath();
            String fileName = "history_" + System.currentTimeMillis() + ".xlsx";
            String filePath = reportPath + fileName;
            
            File dir = new File(reportPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            
            workbook.close();
            
            // 7. 记录历史
            TaskHistory history = new TaskHistory("EXPORT", "历史记录");
            history.setDetail("导出历史记录成功");
            history.setExecutionTime(DateUtil.betweenMillis(startTime, DateUtil.now()));
            TaskHistoryService.recordHistory(history);
            
            LogUtil.info("历史记录导出成功: " + filePath);
            return filePath;
            
        } catch (IOException e) {
            LogUtil.error("历史记录导出失败", e);
            throw new DataCleanException("历史记录导出失败: " + e.getMessage(), e);
        } catch (Exception e) {
            LogUtil.error("历史记录导出异常", e);
            throw new DataCleanException("历史记录导出失败: " + e.getMessage(), e);
        }
    }
}
