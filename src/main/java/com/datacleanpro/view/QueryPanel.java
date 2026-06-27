package com.datacleanpro.view;

import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.service.DataImportService;
import com.datacleanpro.util.DateUtil;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据查询面板
 * 处理数据查询和筛选
 */
public class QueryPanel extends JPanel {

    private JTextField searchField;
    private JButton searchButton;
    private JButton refreshButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JComboBox<FileItem> fileComboBox;
    private JLabel statusLabel;
    private DataImportService importService;
    private List<DataFile> cachedFiles;
    private List<DataRow> cachedRows;

    public QueryPanel() {
        importService = new DataImportService();
        cachedFiles = new ArrayList<>();
        cachedRows = new ArrayList<>();
        initComponents();
        setupLayout();
        setupEventHandlers();
        loadFiles();
    }

    private void initComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("搜索");
        refreshButton = new JButton("刷新");

        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);

        fileComboBox = new JComboBox<>();
        statusLabel = new JLabel("就绪");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryPanel.add(new JLabel("文件:"));
        queryPanel.add(fileComboBox);
        queryPanel.add(Box.createHorizontalStrut(10));
        queryPanel.add(new JLabel("搜索:"));
        queryPanel.add(searchField);
        queryPanel.add(searchButton);
        queryPanel.add(refreshButton);

        JScrollPane tableScrollPane = new JScrollPane(resultTable);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        add(queryPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        searchButton.addActionListener(e -> refreshCurrentView());
        refreshButton.addActionListener(e -> loadFiles());
        fileComboBox.addActionListener(e -> refreshCurrentView());
    }

    /**
     * 加载已导入文件
     */
    private void loadFiles() {
        statusLabel.setText("正在加载文件...");
        SwingWorker<List<DataFile>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DataFile> doInBackground() {
                return importService.getAllFiles();
            }

            @Override
            protected void done() {
                try {
                    cachedFiles = get();
                    fileComboBox.removeAllItems();
                    fileComboBox.addItem(new FileItem(null, "所有文件"));
                    for (DataFile file : cachedFiles) {
                        fileComboBox.addItem(new FileItem(file, file.getFileName()));
                    }
                    showFiles(cachedFiles);
                    statusLabel.setText("已加载 " + cachedFiles.size() + " 个文件");
                } catch (Exception e) {
                    LogUtil.error("加载文件列表失败", e);
                    statusLabel.setText("加载失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 刷新当前查询视图
     */
    private void refreshCurrentView() {
        FileItem item = (FileItem) fileComboBox.getSelectedItem();
        if (item == null || item.file == null) {
            showFiles(filterFiles(cachedFiles, searchField.getText()));
            statusLabel.setText("文件列表: " + tableModel.getRowCount() + " 条");
            return;
        }

        loadRows(item.file);
    }

    /**
     * 加载文件数据行
     * @param file 文件信息
     */
    private void loadRows(DataFile file) {
        statusLabel.setText("正在加载数据: " + file.getFileName());
        SwingWorker<List<DataRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DataRow> doInBackground() {
                return importService.getFileData(file.getId(), 1, 500);
            }

            @Override
            protected void done() {
                try {
                    cachedRows = get();
                    List<DataRow> rows = filterRows(cachedRows, searchField.getText());
                    showRows(rows);
                    statusLabel.setText(file.getFileName() + ": " + rows.size() + " 行");
                } catch (Exception e) {
                    LogUtil.error("加载数据行失败", e);
                    statusLabel.setText("加载失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 显示文件列表
     * @param files 文件列表
     */
    private void showFiles(List<DataFile> files) {
        tableModel.setColumnIdentifiers(new String[]{"ID", "文件名", "行数", "列数", "状态", "导入时间"});
        tableModel.setRowCount(0);
        for (DataFile file : files) {
            tableModel.addRow(new Object[]{
                    file.getId(),
                    file.getFileName(),
                    file.getRowCount(),
                    file.getColumnCount(),
                    file.getStatus(),
                    DateUtil.format(file.getImportTime())
            });
        }
    }

    /**
     * 显示数据行
     * @param rows 数据行
     */
    private void showRows(List<DataRow> rows) {
        int maxColumnCount = 0;
        for (DataRow row : rows) {
            if (row.getFields() != null) {
                maxColumnCount = Math.max(maxColumnCount, row.getFields().size());
            }
        }

        String[] columns = new String[maxColumnCount + 1];
        columns[0] = "行号";
        for (int i = 0; i < maxColumnCount; i++) {
            columns[i + 1] = "列 " + (i + 1);
        }

        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        for (DataRow row : rows) {
            Object[] values = new Object[maxColumnCount + 1];
            values[0] = row.getRowIndex();
            for (int i = 0; row.getFields() != null && i < row.getFields().size(); i++) {
                values[i + 1] = row.getFields().get(i);
            }
            tableModel.addRow(values);
        }
    }

    private List<DataFile> filterFiles(List<DataFile> files, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return files;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<DataFile> result = new ArrayList<>();
        for (DataFile file : files) {
            if ((file.getFileName() != null && file.getFileName().toLowerCase().contains(normalizedKeyword)) ||
                    (file.getStatus() != null && file.getStatus().toLowerCase().contains(normalizedKeyword))) {
                result.add(file);
            }
        }
        return result;
    }

    private List<DataRow> filterRows(List<DataRow> rows, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return rows;
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<DataRow> result = new ArrayList<>();
        for (DataRow row : rows) {
            if (row.getFields() == null) {
                continue;
            }
            for (String field : row.getFields()) {
                if (field != null && field.toLowerCase().contains(normalizedKeyword)) {
                    result.add(row);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 文件下拉项
     */
    private static class FileItem {
        private final DataFile file;
        private final String label;

        private FileItem(DataFile file, String label) {
            this.file = file;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
