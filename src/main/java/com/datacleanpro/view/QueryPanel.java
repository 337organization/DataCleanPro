package com.datacleanpro.view;

import javax.swing.*;
import java.awt.*;

/**
 * 数据查询面板
 * 处理数据查询和筛选
 */
public class QueryPanel extends JPanel {
    
    private JTextField searchField;
    private JButton searchButton;
    private JButton refreshButton;
    private JTable resultTable;
    private JComboBox<String> fileComboBox;
    private JLabel statusLabel;
    
    public QueryPanel() {
        initComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initComponents() {
        searchField = new JTextField(20);
        searchButton = new JButton("搜索");
        refreshButton = new JButton("刷新");
        
        String[] columns = {"ID", "文件名", "行数", "列数", "状态", "导入时间"};
        Object[][] data = {};
        resultTable = new JTable(data, columns);
        
        fileComboBox = new JComboBox<>();
        fileComboBox.addItem("所有文件");
        statusLabel = new JLabel("就绪");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 查询面板
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryPanel.add(new JLabel("文件:"));
        queryPanel.add(fileComboBox);
        queryPanel.add(Box.createHorizontalStrut(10));
        queryPanel.add(new JLabel("搜索:"));
        queryPanel.add(searchField);
        queryPanel.add(searchButton);
        queryPanel.add(refreshButton);
        
        // 表格
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        
        // 状态面板
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        add(queryPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText();
            statusLabel.setText("搜索: " + searchText);
            // TODO: 实现搜索功能
        });
        
        refreshButton.addActionListener(e -> {
            statusLabel.setText("刷新数据...");
            // TODO: 实现刷新功能
        });
    }
}
