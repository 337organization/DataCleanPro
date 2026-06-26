package com.datacleanpro.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置工具类
 * 读取和管理应用程序配置
 */
public class ConfigUtil {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "application.properties";

    static {
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                LogUtil.info("配置文件加载成功: " + CONFIG_FILE);
            } else {
                LogUtil.warn("配置文件未找到: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            LogUtil.error("加载配置文件失败: " + CONFIG_FILE, e);
        }
    }

    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    public static String getString(String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取整数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LogUtil.warn("配置值格式错误: " + key + " = " + value);
            }
        }
        return defaultValue;
    }

    /**
     * 获取长整数配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static long getLong(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LogUtil.warn("配置值格式错误: " + key + " = " + value);
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * 获取数据库URL
     * @return 数据库URL
     */
    public static String getDbUrl() {
        return getString("db.url");
    }

    /**
     * 获取数据库用户名
     * @return 数据库用户名
     */
    public static String getDbUsername() {
        return getString("db.username");
    }

    /**
     * 获取数据库密码
     * @return 数据库密码
     */
    public static String getDbPassword() {
        return getString("db.password");
    }

    /**
     * 获取数据库驱动
     * @return 数据库驱动
     */
    public static String getDbDriver() {
        return getString("db.driver");
    }

    /**
     * 获取连接池大小
     * @return 连接池大小
     */
    public static int getDbPoolSize() {
        return getInt("db.pool.size", 5);
    }

    /**
     * 获取上传文件存储路径
     * @return 上传文件存储路径
     */
    public static String getUploadPath() {
        return getString("app.storage.uploads", "storage/uploads/");
    }

    /**
     * 获取报告文件存储路径
     * @return 报告文件存储路径
     */
    public static String getReportPath() {
        return getString("app.storage.reports", "storage/reports/");
    }

    /**
     * 获取日志文件存储路径
     * @return 日志文件存储路径
     */
    public static String getLogPath() {
        return getString("app.storage.logs", "storage/logs/");
    }

    /**
     * 获取错误文件存储路径
     * @return 错误文件存储路径
     */
    public static String getErrorPath() {
        return getString("app.storage.errors", "storage/errors/");
    }

    /**
     * 获取服务器端口
     * @return 服务器端口
     */
    public static int getServerPort() {
        return getInt("server.port", 8888);
    }

    /**
     * 获取服务器主机
     * @return 服务器主机
     */
    public static String getServerHost() {
        return getString("server.host", "localhost");
    }

    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    public static int getThreadPoolSize() {
        return getInt("thread.pool.size", 4);
    }
}
