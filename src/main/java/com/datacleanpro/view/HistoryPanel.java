package com.datacleanpro.view;

import com.datacleanpro.model.TaskHistory;
import com.datacleanpro.service.ReportService;
import com.datacleanpro.service.TaskHistoryService;
import com.datacleanpro.util.DateUtil;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 历史记录面板
 * 显示任务历史记录
 */
public class HistoryPanel extends JPanel {

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton exportButton;
    private JLabel statusLabel;

    public HistoryPanel() {
        initComponents();
        setupLayout();
        setupEventHandlers();
        loadHistory();
    }

    private void initComponents() {
        tableModel = new DefaultTableModel(new String[]{"ID", "操作", "目标", "状态", "执行时间", "创建时间", "详情"}, 0);
        historyTable = new JTable(tableModel);

        refreshButton = new JButton("刷新");
        clearButton = new JButton("清空");
        exportButton = new JButton("导出");
        statusLabel = new JLabel("就绪");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        JScrollPane tableScrollPane = new JScrollPane(historyTable);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);

        add(buttonPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadHistory());
        clearButton.addActionListener(e -> clearHistory());
        exportButton.addActionListener(e -> exportHistory());
    }

    /**
     * 加载历史记录
     */
    private void loadHistory() {
        statusLabel.setText("正在刷新历史记录...");
        SwingWorker<List<TaskHistory>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TaskHistory> doInBackground() {
                return TaskHistoryService.getAllHistory();
            }

            @Override
            protected void done() {
                try {
                    List<TaskHistory> histories = get();
                    tableModel.setRowCount(0);
                    for (TaskHistory history : histories) {
                        tableModel.addRow(new Object[]{
                                history.getId(),
                                history.getAction(),
                                history.getTarget(),
                                history.getStatus(),
                                DateUtil.formatDuration(history.getExecutionTime() != null ? history.getExecutionTime() : 0),
                                DateUtil.format(history.getCreatedAt()),
                                history.getDetail()
                        });
                    }
                    statusLabel.setText("已加载 " + histories.size() + " 条历史记录");
                } catch (Exception e) {
                    LogUtil.error("刷新历史记录失败", e);
                    statusLabel.setText("刷新失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 清空历史记录
     */
    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(this, "确定要清空所有历史记录吗？",
                "确认", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        statusLabel.setText("正在清空历史记录...");
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int deletedCount = 0;
                for (TaskHistory history : TaskHistoryService.getAllHistory()) {
                    if (history.getId() != null && TaskHistoryService.deleteHistory(history.getId())) {
                        deletedCount++;
                    }
                }
                return deletedCount;
            }

            @Override
            protected void done() {
                try {
                    int deletedCount = get();
                    statusLabel.setText("已清空 " + deletedCount + " 条历史记录");
                    loadHistory();
                } catch (Exception e) {
                    LogUtil.error("清空历史记录失败", e);
                    statusLabel.setText("清空失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 导出历史记录
     */
    private void exportHistory() {
        statusLabel.setText("正在导出历史记录...");
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return ReportService.exportHistoryToExcel();
            }

            @Override
            protected void done() {
                try {
                    String path = get();
                    statusLabel.setText("导出成功: " + path);
                    JOptionPane.showMessageDialog(HistoryPanel.this, "历史记录已导出:\n" + path,
                            "导出成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    LogUtil.error("导出历史记录失败", e);
                    statusLabel.setText("导出失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}
