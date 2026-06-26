package com.datacleanpro;

import com.datacleanpro.dao.DBConnection;
import com.datacleanpro.network.Server;
import com.datacleanpro.util.LogUtil;
import com.datacleanpro.view.MainFrame;

import javax.swing.*;

/**
 * DataCleanPro 应用程序入口
 * 自动化数据处理与清洗系统
 */
public class App {
    
    private static Server server;
    
    public static void main(String[] args) {
        LogUtil.info("DataCleanPro 应用程序启动");
        
        try {
            // 1. 初始化数据库连接
            LogUtil.info("初始化数据库连接...");
            try {
                DBConnection.initialize();
            } catch (Exception dbEx) {
                LogUtil.warn("数据库连接失败，部分功能将不可用: " + dbEx.getMessage());
            }
            
            // 2. 启动服务器（后台线程）
            LogUtil.info("启动服务器...");
            server = new Server();
            Thread serverThread = new Thread(() -> {
                server.start();
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            // 3. 启动GUI
            LogUtil.info("启动图形界面...");
            SwingUtilities.invokeLater(() -> {
                try {
                    // 设置外观和感觉
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    LogUtil.warn("设置外观失败", e);
                }
                
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                
                LogUtil.info("图形界面启动成功");
            });
            
            // 4. 注册关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LogUtil.info("应用程序关闭中...");
                if (server != null) {
                    server.stop();
                }
                DBConnection.closeAll();
                LogUtil.info("应用程序已关闭");
            }));
            
        } catch (Exception e) {
            LogUtil.error("应用程序启动失败", e);
            JOptionPane.showMessageDialog(null, 
                "应用程序启动失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    /**
     * 获取服务器实例
     * @return 服务器实例
     */
    public static Server getServer() {
        return server;
    }
}
