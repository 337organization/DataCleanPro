package com.datacleanpro.service;

import com.datacleanpro.cleaner.CleanPipeline;
import com.datacleanpro.cleaner.DataCleaner;
import com.datacleanpro.dao.DataFileDAO;
import com.datacleanpro.dao.DataRowDAO;
import com.datacleanpro.exception.DataCleanException;
import com.datacleanpro.exception.FileFormatException;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.model.TaskHistory;
import com.datacleanpro.parser.FileParser;
import com.datacleanpro.parser.ParserFactory;
import com.datacleanpro.util.DateUtil;
import com.datacleanpro.util.FileUtil;
import com.datacleanpro.util.LogUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入服务
 * 处理文件导入、解析、清洗和存储
 */
public class DataImportService {

    /**
     * 仅解析文件，不写入数据库
     * @param file 文件
     * @return 文件解析结果
     */
    public FileParser.ParseResult parseFileOnly(File file) {
        LogUtil.info("仅解析文件用于预览: " + file.getName());

        if (!FileUtil.isAllowedFileType(file)) {
            throw new FileFormatException("不支持的文件类型", file.getAbsolutePath(),
                                        "xlsx, xls, csv", FileUtil.getFileExtension(file));
        }

        FileParser.ParseResult parseResult = ParserFactory.parseFile(file);
        if (!parseResult.isSuccess()) {
            throw new FileFormatException(parseResult.getMessage());
        }

        return parseResult;
    }
    
    /**
     * 导入文件
     * @param file 文件
     * @return 数据文件对象
     */
    public DataFile importFile(File file) {
        LocalDateTime startTime = DateUtil.now();
        LogUtil.info("开始导入文件: " + file.getName());
        
        try {
            // 1. 验证文件
            if (!FileUtil.isAllowedFileType(file)) {
                throw new FileFormatException("不支持的文件类型", file.getAbsolutePath(), 
                                            "xlsx, xls, csv", FileUtil.getFileExtension(file));
            }
            
            // 2. 解析文件
            FileParser.ParseResult parseResult = parseFileOnly(file);
            
            // 3. 保存文件元数据
            DataFile dataFile = new DataFile();
            dataFile.setFileName(file.getName());
            dataFile.setFilePath(file.getAbsolutePath());
            dataFile.setFileType(FileUtil.getFileExtension(file));
            dataFile.setRowCount(parseResult.getRowCount());
            dataFile.setColumnCount(parseResult.getRows().isEmpty() ? 0 : parseResult.getRows().get(0).getFieldCount());
            dataFile.setStatus("IMPORTED");
            
            Long fileId = DataFileDAO.insert(dataFile);
            dataFile.setId(fileId);
            
            // 4. 保存数据行
            List<DataRow> rows = parseResult.getRows();
            for (DataRow row : rows) {
                row.setFileId(fileId);
            }
            DataRowDAO.batchInsert(rows);
            
            // 5. 记录历史
            TaskHistory history = new TaskHistory("IMPORT", file.getName());
            history.setDetail("导入成功，行数: " + rows.size());
            history.setExecutionTime(DateUtil.betweenMillis(startTime, DateUtil.now()));
            TaskHistoryService.recordHistory(history);
            
            LogUtil.info("文件导入成功: " + file.getName() + ", 行数: " + rows.size());
            return dataFile;
            
        } catch (FileFormatException e) {
            LogUtil.error("文件导入失败: " + file.getName(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error("文件导入异常: " + file.getName(), e);
            throw new DataCleanException("文件导入失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清洗数据
     * @param fileId 文件ID
     * @return 清洗结果
     */
    public DataCleaner.CleanResult cleanData(Long fileId) {
        LocalDateTime startTime = DateUtil.now();
        LogUtil.info("开始清洗数据，文件ID: " + fileId);
        
        try {
            // 1. 获取数据
            List<DataRow> rows = DataRowDAO.findByFileId(fileId);
            if (rows.isEmpty()) {
                return new DataCleaner.CleanResult(0, 0, 0, "没有数据需要清洗");
            }
            
            // 2. 执行清洗管道
            CleanPipeline pipeline = CleanPipeline.createDefault();
            DataCleaner.CleanResult result = pipeline.execute(rows);
            
            // 3. 更新数据
            DataRowDAO.deleteByFileId(fileId);
            for (DataRow row : rows) {
                row.setFileId(fileId);
            }
            DataRowDAO.batchInsert(rows);
            
            // 4. 更新文件状态
            DataFile dataFile = DataFileDAO.findById(fileId);
            if (dataFile != null) {
                dataFile.setStatus("CLEANED");
                dataFile.setRowCount(rows.size());
                DataFileDAO.update(dataFile);
            }
            
            // 5. 记录历史
            TaskHistory history = new TaskHistory("CLEAN", "文件ID: " + fileId);
            history.setDetail(result.toString());
            history.setExecutionTime(DateUtil.betweenMillis(startTime, DateUtil.now()));
            TaskHistoryService.recordHistory(history);
            
            LogUtil.info("数据清洗完成，文件ID: " + fileId + ", 结果: " + result);
            return result;
            
        } catch (Exception e) {
            LogUtil.error("数据清洗失败，文件ID: " + fileId, e);
            throw new DataCleanException("数据清洗失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件数据
     * @param fileId 文件ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 数据行列表
     */
    public List<DataRow> getFileData(Long fileId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return DataRowDAO.findByFileIdWithPagination(fileId, offset, pageSize);
    }
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    public DataFile getFileInfo(Long fileId) {
        return DataFileDAO.findById(fileId);
    }
    
    /**
     * 获取所有文件
     * @return 文件列表
     */
    public List<DataFile> getAllFiles() {
        return DataFileDAO.findAll();
    }
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否成功
     */
    public boolean deleteFile(Long fileId) {
        try {
            DataRowDAO.deleteByFileId(fileId);
            return DataFileDAO.deleteById(fileId);
        } catch (Exception e) {
            LogUtil.error("删除文件失败，文件ID: " + fileId, e);
            return false;
        }
    }
}
