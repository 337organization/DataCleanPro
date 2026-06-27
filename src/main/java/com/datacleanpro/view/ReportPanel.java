package com.datacleanpro.view;

import com.datacleanpro.model.DataFile;
import com.datacleanpro.service.DataImportService;
import com.datacleanpro.service.ReportService;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表面板
 * 处理报表导出
 */
public class ReportPanel extends JPanel {

    private JComboBox<FileItem> fileComboBox;
    private JButton refreshButton;
    private JButton exportDataButton;
    private JButton exportHistoryButton;
    private JButton exportAllButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private DataImportService importService;
    private List<DataFile> cachedFiles;

    public ReportPanel() {
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
        exportDataButton = new JButton("导出数据");
        exportHistoryButton = new JButton("导出历史记录");
        exportAllButton = new JButton("导出所有");
        logArea = new JTextArea();
        progressBar = new JProgressBar(0, 100);

        logArea.setEditable(false);
        progressBar.setStringPainted(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JLabel("文件:"));
        buttonPanel.add(fileComboBox);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportDataButton);
        buttonPanel.add(exportHistoryButton);
        buttonPanel.add(exportAllButton);

        JScrollPane logScrollPane = new JScrollPane(logArea);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadFiles());
        exportDataButton.addActionListener(e -> exportSelectedData());
        exportHistoryButton.addActionListener(e -> exportHistory());
        exportAllButton.addActionListener(e -> exportAll());
    }

    /**
     * 加载可导出的文件
     */
    private void loadFiles() {
        appendLog("加载文件列表...");
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
                    appendLog("已加载 " + cachedFiles.size() + " 个文件");
                } catch (Exception e) {
                    LogUtil.error("加载导出文件列表失败", e);
                    appendLog("加载失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 导出选中文件数据
     */
    private void exportSelectedData() {
        FileItem item = (FileItem) fileComboBox.getSelectedItem();
        if (item == null || item.file == null) {
            JOptionPane.showMessageDialog(this, "请先导入并选择一个文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        exportDataFile(item.file);
    }

    private void exportDataFile(DataFile file) {
        progressBar.setIndeterminate(true);
        appendLog("开始导出数据: " + file.getFileName());
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ReportService.exportToExcel(file.getId());
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                try {
                    appendLog("导出成功: " + get());
                } catch (Exception e) {
                    LogUtil.error("导出数据失败", e);
                    appendLog("导出失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 导出历史记录
     */
    private void exportHistory() {
        progressBar.setIndeterminate(true);
        appendLog("开始导出历史记录...");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ReportService.exportHistoryToExcel();
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                try {
                    appendLog("历史记录导出成功: " + get());
                } catch (Exception e) {
                    LogUtil.error("导出历史记录失败", e);
                    appendLog("导出失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 导出所有文件和历史记录
     */
    private void exportAll() {
        if (cachedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前没有可导出的文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setIndeterminate(true);
        appendLog("开始导出所有数据...");
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                List<String> paths = new ArrayList<>();
                for (DataFile file : cachedFiles) {
                    paths.add(ReportService.exportToExcel(file.getId()));
                }
                paths.add(ReportService.exportHistoryToExcel());
                return paths;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                try {
                    for (String path : get()) {
                        appendLog("导出成功: " + path);
                    }
                } catch (Exception e) {
                    LogUtil.error("导出所有数据失败", e);
                    appendLog("导出失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
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
