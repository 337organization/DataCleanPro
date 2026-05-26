package com.datacleanpro.view;

import com.datacleanpro.service.DataImportService;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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
        SwingWorker<DataFile, Void> worker = new SwingWorker<>() {
            @Override
            protected DataFile doInBackground() throws Exception {
                return importService.importFile(selectedFile);
            }
            
            @Override
            protected void done() {
                try {
                    DataFile dataFile = get();
                    if (dataFile != null) {
                        fileInfoLabel.setText("导入成功: " + dataFile.getFileName() + 
                                            " (" + dataFile.getRowCount() + " 行)");
                        cleanButton.setEnabled(true);
                        loadDataPreview(dataFile.getId());
                        JOptionPane.showMessageDialog(ImportPanel.this, 
                            "文件导入成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
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
        
        if (rows.isEmpty()) {
            return;
        }
        
        // 更新列名
        DataRow firstRow = rows.get(0);
        if (firstRow.getFields() != null) {
            String[] columns = new String[firstRow.getFields().size()];
            for (int i = 0; i < firstRow.getFields().size(); i++) {
                columns[i] = "列 " + (i + 1);
            }
            tableModel.setColumnIdentifiers(columns);
        }
        
        // 添加数据
        for (DataRow row : rows) {
            if (row.getFields() != null) {
                tableModel.addRow(row.getFields().toArray());
            }
        }
    }
}
