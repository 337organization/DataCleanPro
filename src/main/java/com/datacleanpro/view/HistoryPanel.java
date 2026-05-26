package com.datacleanpro.view;

import javax.swing.*;
import java.awt.*;

/**
 * 历史记录面板
 * 显示任务历史记录
 */
public class HistoryPanel extends JPanel {
    
    private JTable historyTable;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton exportButton;
    private JLabel statusLabel;
    
    public HistoryPanel() {
        initComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initComponents() {
        String[] columns = {"ID", "操作", "目标", "状态", "执行时间", "创建时间"};
        Object[][] data = {};
        historyTable = new JTable(data, columns);
        
        refreshButton = new JButton("刷新");
        clearButton = new JButton("清空");
        exportButton = new JButton("导出");
        statusLabel = new JLabel("就绪");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);
        
        // 表格
        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        
        // 状态面板
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> {
            statusLabel.setText("刷新历史记录...");
            // TODO: 实现刷新功能
        });
        
        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "确定要清空所有历史记录吗？", 
                "确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                statusLabel.setText("清空历史记录...");
                // TODO: 实现清空功能
            }
        });
        
        exportButton.addActionListener(e -> {
            statusLabel.setText("导出历史记录...");
            // TODO: 实现导出功能
        });
    }
}
