package com.datacleanpro.view;

import com.datacleanpro.service.DataImportService;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.parser.FileParser;
import com.datacleanpro.util.FileUtil;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据导入面板
 * 处理文件导入和数据预览
 */
public class ImportPanel extends JPanel {
    
    private JButton selectFileButton;
    private JButton importButton;
    private JButton cleanButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JLabel fileInfoLabel;
    private JFileChooser fileChooser;
    private File selectedFile;
    private DataImportService importService;
    
    public ImportPanel() {
        importService = new DataImportService();
        initComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        selectFileButton = new JButton("选择文件");
        importButton = new JButton("导入数据");
        cleanButton = new JButton("清洗数据");
        fileInfoLabel = new JLabel("未选择文件");
        
        importButton.setEnabled(false);
        cleanButton.setEnabled(false);
        
        // 创建表格模型
        String[] columns = {"列1", "列2", "列3", "列4", "列5"};
        tableModel = new DefaultTableModel(columns, 0);
        dataTable = new JTable(tableModel);
        
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "支持的文件 (*.xlsx, *.xls, *.csv)", "xlsx", "xls", "csv"));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(selectFileButton);
        topPanel.add(importButton);
        topPanel.add(cleanButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(fileInfoLabel);
        
        // 表格面板
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 400));
        
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        selectFileButton.addActionListener(e -> selectFile());
        importButton.addActionListener(e -> importFile());
        cleanButton.addActionListener(e -> cleanData());
    }
    
    /**
     * 选择文件
     */
    private void selectFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileInfoLabel.setText("已选择: " + selectedFile.getName());
            importButton.setEnabled(true);
            LogUtil.info("用户选择文件: " + selectedFile.getAbsolutePath());
        }
    }
    
    /**
     * 导入文件
     */
    private void importFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 使用SwingWorker在后台执行导入
        SwingWorker<ImportResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ImportResult doInBackground() throws Exception {
                try {
                    DataFile dataFile = importService.importFile(selectedFile);
                    List<DataRow> rows = importService.getFileData(dataFile.getId(), 1, 100);
                    return new ImportResult(dataFile, rows, true, null);
                } catch (Exception dbEx) {
                    // 数据库不可用时，降级为仅解析预览，避免基础导入流程被 MySQL 配置卡住。
                    LogUtil.warn("数据库导入失败，尝试仅解析文件用于预览", dbEx);
                    FileParser.ParseResult parseResult = importService.parseFileOnly(selectedFile);
                    DataFile previewFile = createPreviewDataFile(selectedFile, parseResult);
                    return new ImportResult(previewFile, parseResult.getRows(), false, dbEx.getMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImportResult result = get();
                    if (result != null && result.dataFile != null) {
                        DataFile dataFile = result.dataFile;
                        String modeText = result.savedToDatabase ? "导入成功" : "预览成功";
                        fileInfoLabel.setText(modeText + ": " + dataFile.getFileName() +
                                            " (" + dataFile.getRowCount() + " 行)");
                        cleanButton.setEnabled(true);
                        updateTable(result.rows);

                        if (result.savedToDatabase) {
                            JOptionPane.showMessageDialog(ImportPanel.this,
                                "文件导入成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(ImportPanel.this,
                                "文件已解析并显示预览，但未保存到数据库。\n" +
                                "如需使用历史记录、查询等功能，请先配置并启动 MySQL。\n\n" +
                                "数据库错误: " + result.warningMessage,
                                "预览模式", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("文件导入失败", e);
                    JOptionPane.showMessageDialog(ImportPanel.this, 
                        "文件导入失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 清洗数据
     */
    private void cleanData() {
        // TODO: 实现数据清洗
        JOptionPane.showMessageDialog(this, "数据清洗功能即将实现", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 加载数据预览
     * @param fileId 文件ID
     */
    private void loadDataPreview(Long fileId) {
        SwingWorker<List<DataRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DataRow> doInBackground() throws Exception {
                return importService.getFileData(fileId, 1, 100);
            }
            
            @Override
            protected void done() {
                try {
                    List<DataRow> rows = get();
                    updateTable(rows);
                } catch (Exception e) {
                    LogUtil.error("加载数据预览失败", e);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * 更新表格数据
     * @param rows 数据行
     */
    private void updateTable(List<DataRow> rows) {
        // 清空表格
        tableModel.setRowCount(0);
        
        if (rows == null || rows.isEmpty()) {
            return;
        }
        
        // 根据预览数据中最长的一行生成列名
        int maxColumnCount = 0;
        for (DataRow row : rows) {
            if (row.getFields() != null) {
                maxColumnCount = Math.max(maxColumnCount, row.getFields().size());
            }
        }

        String[] columns = new String[maxColumnCount];
        for (int i = 0; i < maxColumnCount; i++) {
            columns[i] = "列 " + (i + 1);
        }
        tableModel.setColumnIdentifiers(columns);
        
        // 添加数据
        for (DataRow row : rows) {
            if (row.getFields() != null) {
                tableModel.addRow(row.getFields().toArray());
            }
        }
    }

    /**
     * 创建仅用于界面预览的文件信息
     * @param file 源文件
     * @param parseResult 解析结果
     * @return 文件信息
     */
    private DataFile createPreviewDataFile(File file, FileParser.ParseResult parseResult) {
        DataFile dataFile = new DataFile();
        dataFile.setFileName(file.getName());
        dataFile.setFilePath(file.getAbsolutePath());
        dataFile.setFileType(FileUtil.getFileExtension(file));
        dataFile.setRowCount(parseResult.getRowCount());
        dataFile.setColumnCount(getMaxColumnCount(parseResult.getRows()));
        dataFile.setStatus("PREVIEW_ONLY");
        dataFile.setDescription("数据库不可用，仅解析预览，未保存");
        return dataFile;
    }

    /**
     * 获取预览数据最大列数
     * @param rows 数据行
     * @return 最大列数
     */
    private int getMaxColumnCount(List<DataRow> rows) {
        int maxColumnCount = 0;
        if (rows == null) {
            return maxColumnCount;
        }

        for (DataRow row : rows) {
            if (row.getFields() != null) {
                maxColumnCount = Math.max(maxColumnCount, row.getFields().size());
            }
        }
        return maxColumnCount;
    }

    /**
     * 导入结果，兼容数据库保存模式和纯预览模式
     */
    private static class ImportResult {
        private final DataFile dataFile;
        private final List<DataRow> rows;
        private final boolean savedToDatabase;
        private final String warningMessage;

        private ImportResult(DataFile dataFile, List<DataRow> rows, boolean savedToDatabase, String warningMessage) {
            this.dataFile = dataFile;
            this.rows = rows != null ? rows : new ArrayList<>();
            this.savedToDatabase = savedToDatabase;
            this.warningMessage = warningMessage;
        }
    }
}
