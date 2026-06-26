package com.datacleanpro.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 * 提供日期时间相关的工具方法
 */
public class DateUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期时间字符串
     * @param dateTimeStr 日期时间字符串
     * @return 日期时间
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    /**
     * 解析日期时间字符串
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return 日期时间
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前日期时间
     * @return 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期时间字符串
     * @return 当前日期时间字符串
     */
    public static String nowString() {
        return format(now());
    }

    /**
     * 计算两个日期时间之间的差值（毫秒）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（毫秒）
     */
    public static long betweenMillis(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的差值（秒）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（秒）
     */
    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 格式化持续时间
     * @param millis 毫秒
     * @return 格式化后的持续时间
     */
    public static String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60 * 1000) {
            return String.format("%.2fs", millis / 1000.0);
        } else if (millis < 60 * 60 * 1000) {
            long minutes = millis / (60 * 1000);
            long seconds = (millis % (60 * 1000)) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        } else {
            long hours = millis / (60 * 60 * 1000);
            long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);
            return String.format("%dh %dm", hours, minutes);
        }
    }
}
