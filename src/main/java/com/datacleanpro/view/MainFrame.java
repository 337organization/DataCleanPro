package com.datacleanpro.view;

import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗口类
 * 应用程序的主界面
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private ImportPanel importPanel;
    private CleanPanel cleanPanel;
    private ValidationPanel validationPanel;
    private QueryPanel queryPanel;
    private ReportPanel reportPanel;
    private HistoryPanel historyPanel;
    private StatusBar statusBar;
    
    public MainFrame() {
        initComponents();
        setupLayout();
        setupMenuBar();
        setupStatusBar();
        setupWindowProperties();
        
        LogUtil.info("主窗口初始化完成");
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        importPanel = new ImportPanel();
        cleanPanel = new CleanPanel();
        validationPanel = new ValidationPanel();
        queryPanel = new QueryPanel();
        reportPanel = new ReportPanel();
        historyPanel = new HistoryPanel();
        statusBar = new StatusBar();
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 添加选项卡
        tabbedPane.addTab("数据导入", importPanel);
        tabbedPane.addTab("数据清洗", cleanPanel);
        tabbedPane.addTab("数据验证", validationPanel);
        tabbedPane.addTab("数据查询", queryPanel);
        tabbedPane.addTab("报表导出", reportPanel);
        tabbedPane.addTab("历史记录", historyPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * 设置菜单栏
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(new JMenuItem("打开文件"));
        fileMenu.add(new JMenuItem("保存"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("退出"));
        
        // 工具菜单
        JMenu toolsMenu = new JMenu("工具");
        toolsMenu.add(new JMenuItem("数据清洗"));
        toolsMenu.add(new JMenuItem("数据验证"));
        toolsMenu.add(new JMenuItem("数据导出"));
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.add(new JMenuItem("关于"));
        helpMenu.add(new JMenuItem("帮助文档"));
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * 设置状态栏
     */
    private void setupStatusBar() {
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * 设置窗口属性
     */
    private void setupWindowProperties() {
        setTitle("DataCleanPro - 自动化数据处理与清洗系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // 设置图标（如果有的话）
        // setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
    }
    
    /**
     * 显示消息对话框
     * @param message 消息
     * @param title 标题
     * @param messageType 消息类型
     */
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    /**
     * 显示确认对话框
     * @param message 消息
     * @param title 标题
     * @return 用户选择
     */
    public int showConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
    }
    
    /**
     * 获取状态栏
     * @return 状态栏
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    /**
     * 更新状态栏消息
     * @param message 消息
     */
    public void updateStatus(String message) {
        statusBar.setMessage(message);
    }
    
    /**
     * 更新状态栏进度
     * @param progress 进度
     */
    public void updateProgress(int progress) {
        statusBar.setProgress(progress);
    }
}
