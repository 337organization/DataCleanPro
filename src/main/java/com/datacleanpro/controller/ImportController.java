package com.datacleanpro.controller;

import com.datacleanpro.service.DataImportService;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;

import java.io.File;
import java.util.List;

/**
 * 导入控制器
 * 处理数据导入相关的业务逻辑
 */
public class ImportController {
    
    private DataImportService importService;
    
    public ImportController() {
        this.importService = new DataImportService();
    }
    
    /**
     * 导入文件
     * @param file 文件
     * @return 数据文件对象
     */
    public DataFile importFile(File file) {
        LogUtil.info("控制器: 导入文件 " + file.getName());
        return importService.importFile(file);
    }
    
    /**
     * 获取文件数据
     * @param fileId 文件ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 数据行列表
     */
    public List<DataRow> getFileData(Long fileId, int page, int pageSize) {
        return importService.getFileData(fileId, page, pageSize);
    }
    
    /**
     * 获取所有文件
     * @return 文件列表
     */
    public List<DataFile> getAllFiles() {
        return importService.getAllFiles();
    }
    
    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否成功
     */
    public boolean deleteFile(Long fileId) {
        return importService.deleteFile(fileId);
    }
}
