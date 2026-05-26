package com.datacleanpro.view;

import javax.swing.*;
import java.awt.*;

/**
 * 状态栏类
 * 显示应用程序状态信息
 */
public class StatusBar extends JPanel {
    
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JLabel connectionLabel;
    
    public StatusBar() {
        initComponents();
        setupLayout();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        messageLabel = new JLabel("就绪");
        progressBar = new JProgressBar(0, 100);
        connectionLabel = new JLabel("数据库: 未连接");
        
        progressBar.setPreferredSize(new Dimension(200, 20));
        progressBar.setStringPainted(true);
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(messageLabel);
        
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(progressBar);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(connectionLabel);
        
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * 设置消息
     * @param message 消息
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
    
    /**
     * 设置进度
     * @param progress 进度
     */
    public void setProgress(int progress) {
        progressBar.setValue(progress);
        progressBar.setString(progress + "%");
    }
    
    /**
     * 设置连接状态
     * @param connected 是否连接
     */
    public void setConnectionStatus(boolean connected) {
        if (connected) {
            connectionLabel.setText("数据库: 已连接");
            connectionLabel.setForeground(Color.GREEN);
        } else {
            connectionLabel.setText("数据库: 未连接");
            connectionLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * 显示进度条
     * @param visible 是否显示
     */
    public void showProgress(boolean visible) {
        progressBar.setVisible(visible);
    }
}
