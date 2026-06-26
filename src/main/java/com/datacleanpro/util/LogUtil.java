package com.datacleanpro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 提供统一的日志记录功能
 */
public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("com.datacleanpro.audit");

    /**
     * 获取日志记录器
     * @param clazz 类
     * @return 日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 记录信息日志
     * @param message 日志消息
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * 记录信息日志
     * @param clazz 类
     * @param message 日志消息
     */
    public static void info(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).info(message);
    }

    /**
     * 记录调试日志
     * @param message 日志消息
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * 记录调试日志
     * @param clazz 类
     * @param message 日志消息
     */
    public static void debug(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).debug(message);
    }

    /**
     * 记录警告日志
     * @param message 日志消息
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * 记录警告日志
     * @param clazz 类
     * @param message 日志消息
     */
    public static void warn(Class<?> clazz, String message) {
        LoggerFactory.getLogger(clazz).warn(message);
    }

    /**
     * 记录警告日志
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    /**
     * 记录错误日志
     * @param message 日志消息
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * 记录错误日志
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * 记录错误日志
     * @param clazz 类
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void error(Class<?> clazz, String message, Throwable throwable) {
        LoggerFactory.getLogger(clazz).error(message, throwable);
    }

    /**
     * 记录审计日志
     * @param action 操作
     * @param target 目标
     * @param detail 详情
     */
    public static void audit(String action, String target, String detail) {
        auditLogger.info("Action: {}, Target: {}, Detail: {}", action, target, detail);
    }

    /**
     * 记录审计日志
     * @param action 操作
     * @param target 目标
     * @param status 状态
     * @param detail 详情
     */
    public static void audit(String action, String target, String status, String detail) {
        auditLogger.info("Action: {}, Target: {}, Status: {}, Detail: {}", action, target, status, detail);
    }
}
