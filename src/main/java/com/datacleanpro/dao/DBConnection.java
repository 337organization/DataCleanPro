package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 数据库连接管理类
 * 使用连接池管理数据库连接
 */
public class DBConnection {
    private static final int POOL_SIZE = ConfigUtil.getDbPoolSize();
    private static final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(POOL_SIZE);
    private static boolean initialized = false;

    /**
     * 初始化连接池
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            // 加载数据库驱动
            Class.forName(ConfigUtil.getDbDriver());
            LogUtil.info("数据库驱动加载成功: " + ConfigUtil.getDbDriver());

            // 创建连接池
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = createConnection();
                if (conn != null) {
                    pool.offer(conn);
                }
            }

            initialized = true;
            LogUtil.info("数据库连接池初始化成功，池大小: " + pool.size());
        } catch (ClassNotFoundException e) {
            LogUtil.error("数据库驱动未找到", e);
            throw new DatabaseImportException("数据库驱动未找到", e);
        }
    }

    /**
     * 创建数据库连接
     * @return 数据库连接
     */
    private static Connection createConnection() {
        try {
            return DriverManager.getConnection(
                    ConfigUtil.getDbUrl(),
                    ConfigUtil.getDbUsername(),
                    ConfigUtil.getDbPassword()
            );
        } catch (SQLException e) {
            LogUtil.error("创建数据库连接失败", e);
            return null;
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }

        try {
            Connection conn = pool.poll(5, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                conn = createConnection();
                if (conn == null) {
                    throw new SQLException("无法创建数据库连接");
                }
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断", e);
        }
    }

    /**
     * 释放数据库连接
     * @param conn 数据库连接
     */
    public static void release(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    pool.offer(conn);
                }
            } catch (SQLException e) {
                LogUtil.warn("检查连接状态失败", e);
            }
        }
    }

    /**
     * 关闭数据库连接
     * @param conn 数据库连接
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LogUtil.warn("关闭连接失败", e);
            }
        }
    }

    /**
     * 关闭所有连接
     */
    public static void closeAll() {
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LogUtil.warn("关闭连接失败", e);
            }
        }
        initialized = false;
        LogUtil.info("所有数据库连接已关闭");
    }

    /**
     * 测试数据库连接
     * @return 是否连接成功
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LogUtil.error("测试数据库连接失败", e);
            return false;
        }
    }

    /**
     * 获取连接池可用连接数
     * @return 可用连接数
     */
    public static int getAvailableConnections() {
        return pool.size();
    }

    /**
     * 开始事务
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    /**
     * 提交事务
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    public static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    /**
     * 回滚事务
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    public static void rollbackTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }
}
