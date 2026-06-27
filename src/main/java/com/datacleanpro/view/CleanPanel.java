package com.datacleanpro.view;

import com.datacleanpro.cleaner.DataCleaner;
import com.datacleanpro.model.DataFile;
import com.datacleanpro.service.DataImportService;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 数据清洗面板
 * 处理数据清洗操作
 */
public class CleanPanel extends JPanel {

    private JComboBox<FileItem> fileComboBox;
    private JButton refreshButton;
    private JButton cleanDuplicateButton;
    private JButton cleanEmptyButton;
    private JButton cleanFormatButton;
    private JButton cleanAllButton;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private DataImportService importService;
    private List<DataFile> cachedFiles;

    public CleanPanel() {
        importService = new DataImportService();
        cachedFiles = new ArrayList<>();
        initComponents();
        setupLayout();
        setupEventHandlers();
        loadFiles();
    }

    private void initComponents() {
        fileComboBox = new JComboBox<>();
        refreshButton = new JButton("刷新文件");
        cleanDuplicateButton = new JButton("去除重复数据");
        cleanEmptyButton = new JButton("处理空值");
        cleanFormatButton = new JButton("格式化数据");
        cleanAllButton = new JButton("执行所有清洗");
        resultArea = new JTextArea();
        progressBar = new JProgressBar(0, 100);

        resultArea.setEditable(false);
        progressBar.setStringPainted(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JLabel("文件:"));
        buttonPanel.add(fileComboBox);
        buttonPanel.add(refreshButton);
        buttonPanel.add(cleanDuplicateButton);
        buttonPanel.add(cleanEmptyButton);
        buttonPanel.add(cleanFormatButton);
        buttonPanel.add(cleanAllButton);

        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.NORTH);
        add(resultScrollPane, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadFiles());
        cleanDuplicateButton.addActionListener(e -> runClean("去除重复数据", importService::cleanDuplicates));
        cleanEmptyButton.addActionListener(e -> runClean("处理空值", importService::cleanEmptyValues));
        cleanFormatButton.addActionListener(e -> runClean("格式化数据", importService::cleanFormat));
        cleanAllButton.addActionListener(e -> runClean("执行所有清洗", importService::cleanData));
    }

    /**
     * 加载已导入文件
     */
    private void loadFiles() {
        appendResult("加载文件列表...");
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
                    for (DataFile file : cachedFiles) {
                        fileComboBox.addItem(new FileItem(file));
                    }
                    appendResult("已加载 " + cachedFiles.size() + " 个文件");
                } catch (Exception e) {
                    LogUtil.error("加载清洗文件列表失败", e);
                    appendResult("加载失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 执行清洗操作
     * @param actionName 操作名称
     * @param action 清洗函数
     */
    private void runClean(String actionName, Function<Long, DataCleaner.CleanResult> action) {
        FileItem item = (FileItem) fileComboBox.getSelectedItem();
        if (item == null || item.file == null) {
            JOptionPane.showMessageDialog(this, "请先导入并选择一个文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setIndeterminate(true);
        appendResult("开始" + actionName + ": " + item.file.getFileName());
        SwingWorker<DataCleaner.CleanResult, Void> worker = new SwingWorker<>() {
            @Override
            protected DataCleaner.CleanResult doInBackground() {
                return action.apply(item.file.getId());
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                try {
                    DataCleaner.CleanResult result = get();
                    appendResult(actionName + "完成");
                    appendResult("总行数: " + result.getTotalRows());
                    appendResult("影响行数: " + result.getAffectedRows());
                    appendResult("删除行数: " + result.getRemovedRows());
                    appendResult("详情: " + result.getDetail());
                    appendResult("");
                    loadFiles();
                } catch (Exception e) {
                    LogUtil.error(actionName + "失败", e);
                    appendResult(actionName + "失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void appendResult(String message) {
        resultArea.append(message + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    /**
     * 文件下拉项
     */
    private static class FileItem {
        private final DataFile file;

        private FileItem(DataFile file) {
            this.file = file;
        }

        @Override
        public String toString() {
            return file == null ? "" : file.getFileName() + " (" + file.getRowCount() + " 行)";
        }
    }
}
