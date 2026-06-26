package com.datacleanpro.view;

import javax.swing.*;
import java.awt.*;

/**
 * 数据验证面板
 * 处理数据验证规则管理
 */
public class ValidationPanel extends JPanel {
    
    private JTable rulesTable;
    private JButton addRuleButton;
    private JButton editRuleButton;
    private JButton deleteRuleButton;
    private JButton validateButton;
    private JTextArea resultArea;
    
    public ValidationPanel() {
        initComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initComponents() {
        String[] columns = {"规则名称", "规则类型", "目标列", "状态"};
        Object[][] data = {
            {"手机号格式", "PHONE", "phone", "启用"},
            {"邮箱格式", "EMAIL", "email", "启用"},
            {"必填字段", "REQUIRED", "all", "启用"}
        };
        
        rulesTable = new JTable(data, columns);
        addRuleButton = new JButton("添加规则");
        editRuleButton = new JButton("编辑规则");
        deleteRuleButton = new JButton("删除规则");
        validateButton = new JButton("执行验证");
        resultArea = new JTextArea();
        
        resultArea.setEditable(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addRuleButton);
        buttonPanel.add(editRuleButton);
        buttonPanel.add(deleteRuleButton);
        buttonPanel.add(validateButton);
        
        // 规则表格
        JScrollPane tableScrollPane = new JScrollPane(rulesTable);
        
        // 结果面板
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(800, 200));
        
        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                                            tableScrollPane, resultScrollPane);
        splitPane.setDividerLocation(300);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        addRuleButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "添加规则功能即将实现", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        
        editRuleButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "编辑规则功能即将实现", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        
        deleteRuleButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "删除规则功能即将实现", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        
        validateButton.addActionListener(e -> {
            resultArea.append("开始执行数据验证...\n");
            // TODO: 实现验证功能
        });
    }
}
