package com.datacleanpro.dao;

import com.datacleanpro.exception.DatabaseImportException;
import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
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
    private static final String LOCAL_DB_URL = "jdbc:h2:file:./storage/local/datacleanpro;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    private static final String LOCAL_DB_USERNAME = "sa";
    private static final String LOCAL_DB_PASSWORD = "";
    private static boolean initialized = false;
    private static boolean localStorageMode = false;
    private static String activeDriver = ConfigUtil.getDbDriver();
    private static String activeUrl = ConfigUtil.getDbUrl();
    private static String activeUsername = ConfigUtil.getDbUsername();
    private static String activePassword = ConfigUtil.getDbPassword();

    /**
     * 初始化连接池
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        pool.clear();
        localStorageMode = false;
        activeDriver = ConfigUtil.getDbDriver();
        activeUrl = ConfigUtil.getDbUrl();
        activeUsername = ConfigUtil.getDbUsername();
        activePassword = ConfigUtil.getDbPassword();

        try {
            Class.forName(activeDriver);
            LogUtil.info("数据库驱动加载成功: " + activeDriver);
            fillConnectionPool(false);

            if (pool.isEmpty()) {
                LogUtil.warn("MySQL连接不可用，切换到本地数据存储");
                initializeLocalStorage();
            } else {
                initialized = true;
                LogUtil.info("数据库连接池初始化成功，模式: MySQL，池大小: " + pool.size());
            }
        } catch (ClassNotFoundException e) {
            LogUtil.warn("MySQL驱动未找到，切换到本地数据存储", e);
            initializeLocalStorage();
        }
    }

    /**
     * 创建数据库连接
     * @return 数据库连接
     */
    private static Connection createConnection() {
        return createConnection(true);
    }

    /**
     * 创建数据库连接
     * @param logFailure 是否记录失败堆栈
     * @return 数据库连接
     */
    private static Connection createConnection(boolean logFailure) {
        try {
            return DriverManager.getConnection(
                    activeUrl,
                    activeUsername,
                    activePassword
            );
        } catch (SQLException e) {
            if (logFailure) {
                LogUtil.error("创建数据库连接失败", e);
            }
            return null;
        }
    }

    /**
     * 填充连接池
     * @param logFailure 是否记录失败堆栈
     */
    private static void fillConnectionPool(boolean logFailure) {
        for (int i = 0; i < POOL_SIZE; i++) {
            Connection conn = createConnection(logFailure);
            if (conn != null) {
                pool.offer(conn);
            }
        }
    }

    /**
     * 初始化本地嵌入式数据库
     */
    private static void initializeLocalStorage() {
        try {
            File localDir = new File("storage/local");
            if (!localDir.exists() && !localDir.mkdirs()) {
                throw new DatabaseImportException("无法创建本地数据目录: " + localDir.getAbsolutePath());
            }

            activeDriver = "org.h2.Driver";
            activeUrl = LOCAL_DB_URL;
            activeUsername = LOCAL_DB_USERNAME;
            activePassword = LOCAL_DB_PASSWORD;
            localStorageMode = true;

            Class.forName(activeDriver);
            try (Connection conn = DriverManager.getConnection(activeUrl, activeUsername, activePassword)) {
                initializeLocalSchema(conn);
            }

            fillConnectionPool(true);
            if (pool.isEmpty()) {
                throw new DatabaseImportException("本地数据存储初始化失败");
            }

            initialized = true;
            LogUtil.info("数据库连接池初始化成功，模式: 本地H2，池大小: " + pool.size());
        } catch (ClassNotFoundException e) {
            LogUtil.error("H2本地数据库驱动未找到", e);
            throw new DatabaseImportException("H2本地数据库驱动未找到", e);
        } catch (SQLException e) {
            LogUtil.error("初始化本地数据存储失败", e);
            throw new DatabaseImportException("初始化本地数据存储失败", e);
        }
    }

    /**
     * 初始化本地数据库表结构
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    private static void initializeLocalSchema(Connection conn) throws SQLException {
        String[] ddlStatements = {
                "CREATE TABLE IF NOT EXISTS data_file (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "file_name VARCHAR(255) NOT NULL, " +
                        "file_path VARCHAR(500) NOT NULL, " +
                        "file_type VARCHAR(10) NOT NULL, " +
                        "row_count INT DEFAULT 0, " +
                        "column_count INT DEFAULT 0, " +
                        "import_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "status VARCHAR(20) DEFAULT 'IMPORTED', " +
                        "description VARCHAR(500))",
                "CREATE INDEX IF NOT EXISTS idx_data_file_type ON data_file(file_type)",
                "CREATE INDEX IF NOT EXISTS idx_data_file_status ON data_file(status)",
                "CREATE INDEX IF NOT EXISTS idx_data_file_import_time ON data_file(import_time)",
                "CREATE TABLE IF NOT EXISTS data_row (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "file_id BIGINT NOT NULL, " +
                        "row_index INT NOT NULL, " +
                        "row_data CLOB NOT NULL, " +
                        "is_deleted BOOLEAN DEFAULT FALSE, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE)",
                "CREATE INDEX IF NOT EXISTS idx_data_row_file_id ON data_row(file_id)",
                "CREATE INDEX IF NOT EXISTS idx_data_row_index ON data_row(row_index)",
                "CREATE INDEX IF NOT EXISTS idx_data_row_deleted ON data_row(is_deleted)",
                "CREATE TABLE IF NOT EXISTS clean_task (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "file_id BIGINT NOT NULL, " +
                        "task_type VARCHAR(50) NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'PENDING', " +
                        "rows_affected INT DEFAULT 0, " +
                        "detail CLOB, " +
                        "start_time DATETIME, " +
                        "end_time DATETIME, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE)",
                "CREATE INDEX IF NOT EXISTS idx_clean_task_file_id ON clean_task(file_id)",
                "CREATE INDEX IF NOT EXISTS idx_clean_task_type ON clean_task(task_type)",
                "CREATE INDEX IF NOT EXISTS idx_clean_task_status ON clean_task(status)",
                "CREATE TABLE IF NOT EXISTS validation_rule (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "rule_name VARCHAR(100) NOT NULL, " +
                        "rule_type VARCHAR(50) NOT NULL, " +
                        "target_column VARCHAR(100), " +
                        "expression VARCHAR(500), " +
                        "error_message VARCHAR(255), " +
                        "is_active BOOLEAN DEFAULT TRUE, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE INDEX IF NOT EXISTS idx_validation_rule_type ON validation_rule(rule_type)",
                "CREATE INDEX IF NOT EXISTS idx_validation_rule_active ON validation_rule(is_active)",
                "CREATE TABLE IF NOT EXISTS validation_result (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "file_id BIGINT NOT NULL, " +
                        "rule_id BIGINT NOT NULL, " +
                        "row_index INT NOT NULL, " +
                        "column_name VARCHAR(100), " +
                        "cell_value VARCHAR(1000), " +
                        "is_passed BOOLEAN NOT NULL, " +
                        "error_message VARCHAR(255), " +
                        "validated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (rule_id) REFERENCES validation_rule(id) ON DELETE CASCADE)",
                "CREATE INDEX IF NOT EXISTS idx_validation_result_file_id ON validation_result(file_id)",
                "CREATE INDEX IF NOT EXISTS idx_validation_result_rule_id ON validation_result(rule_id)",
                "CREATE INDEX IF NOT EXISTS idx_validation_result_passed ON validation_result(is_passed)",
                "CREATE TABLE IF NOT EXISTS task_history (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "task_id BIGINT, " +
                        "action VARCHAR(50) NOT NULL, " +
                        "target VARCHAR(255), " +
                        "detail CLOB, " +
                        "status VARCHAR(20) DEFAULT 'SUCCESS', " +
                        "error_message CLOB, " +
                        "execution_time BIGINT, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)",
                "CREATE INDEX IF NOT EXISTS idx_task_history_action ON task_history(action)",
                "CREATE INDEX IF NOT EXISTS idx_task_history_status ON task_history(status)",
                "CREATE INDEX IF NOT EXISTS idx_task_history_created_at ON task_history(created_at)",
                "CREATE TABLE IF NOT EXISTS imported_table_data (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "file_id BIGINT NOT NULL, " +
                        "table_name VARCHAR(100) NOT NULL, " +
                        "column_definitions CLOB NOT NULL, " +
                        "row_data CLOB NOT NULL, " +
                        "row_index INT NOT NULL, " +
                        "imported_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (file_id) REFERENCES data_file(id) ON DELETE CASCADE)",
                "CREATE INDEX IF NOT EXISTS idx_imported_data_file_id ON imported_table_data(file_id)",
                "CREATE INDEX IF NOT EXISTS idx_imported_data_table_name ON imported_table_data(table_name)"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String ddl : ddlStatements) {
                stmt.execute(ddl);
            }
        }

        insertDefaultValidationRules(conn);
    }

    /**
     * 初始化默认验证规则
     * @param conn 数据库连接
     * @throws SQLException SQL异常
     */
    private static void insertDefaultValidationRules(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM validation_rule";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        String insertSql = "INSERT INTO validation_rule (rule_name, rule_type, target_column, expression, error_message, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            addValidationRule(stmt, "手机号格式", "PHONE", "phone", "^1[3-9]\\d{9}$", "手机号格式不正确");
            addValidationRule(stmt, "邮箱格式", "EMAIL", "email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", "邮箱格式不正确");
            addValidationRule(stmt, "必填字段", "REQUIRED", null, null, "该字段为必填项");
            addValidationRule(stmt, "年龄范围", "NUMBER_RANGE", "age", "0-150", "年龄范围应在0-150之间");
            stmt.executeBatch();
        }
    }

    /**
     * 添加默认验证规则到批处理
     */
    private static void addValidationRule(PreparedStatement stmt, String name, String type, String targetColumn,
                                          String expression, String errorMessage) throws SQLException {
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.setString(3, targetColumn);
        stmt.setString(4, expression);
        stmt.setString(5, errorMessage);
        stmt.setBoolean(6, true);
        stmt.addBatch();
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
                    if (!pool.offer(conn)) {
                        conn.close();
                    }
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
     * 是否正在使用本地数据存储
     * @return 是否本地模式
     */
    public static boolean isLocalStorageMode() {
        return localStorageMode;
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
