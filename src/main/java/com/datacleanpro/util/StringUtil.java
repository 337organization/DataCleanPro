package com.datacleanpro.util;

/**
 * 字符串工具类
 * 提供字符串处理相关的工具方法
 */
public class StringUtil {

    /**
     * 检查字符串是否为空
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 检查字符串是否不为空
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 检查字符串是否为空白
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为空白
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 去除字符串两端空格
     * @param str 字符串
     * @return 处理后的字符串
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串两端空格，如果为空则返回空字符串
     * @param str 字符串
     * @return 处理后的字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 去除字符串两端空格，如果为空则返回默认值
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 处理后的字符串
     */
    public static String trimToDefault(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str.trim();
    }

    /**
     * 截断字符串
     * @param str 字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 检查字符串是否包含子字符串
     * @param str 字符串
     * @param searchStr 子字符串
     * @return 是否包含
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.contains(searchStr);
    }

    /**
     * 检查字符串是否以指定前缀开头
     * @param str 字符串
     * @param prefix 前缀
     * @return 是否以指定前缀开头
     */
    public static boolean startsWith(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * 检查字符串是否以指定后缀结尾
     * @param str 字符串
     * @param suffix 后缀
     * @return 是否以指定后缀结尾
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * 反转字符串
     * @param str 字符串
     * @return 反转后的字符串
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * 重复字符串
     * @param str 字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        return str.repeat(count);
    }

    /**
     * 左填充字符串
     * @param str 字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            str = "";
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return repeat(String.valueOf(padChar), pads) + str;
    }

    /**
     * 右填充字符串
     * @param str 字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            str = "";
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str + repeat(String.valueOf(padChar), pads);
    }

    /**
     * 检查字符串是否为数字
     * @param str 字符串
     * @return 是否为数字
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查字符串是否为字母
     * @param str 字符串
     * @return 是否为字母
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查字符串是否为字母或数字
     * @param str 字符串
     * @return 是否为字母或数字
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
