package com.datacleanpro.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具类
 * 提供文件操作相关的工具方法
 */
public class FileUtil {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("xlsx", "xls", "csv");

    /**
     * 获取文件扩展名
     * @param file 文件
     * @return 文件扩展名
     */
    public static String getFileExtension(File file) {
        if (file == null || file.getName().isEmpty()) {
            return "";
        }
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查文件是否为允许的类型
     * @param file 文件
     * @return 是否允许
     */
    public static boolean isAllowedFileType(File file) {
        String extension = getFileExtension(file);
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 检查文件是否为允许的类型
     * @param fileName 文件名
     * @return 是否允许
     */
    public static boolean isAllowedFileType(String fileName) {
        String extension = getFileExtension(fileName);
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 检查文件是否为Excel文件
     * @param file 文件
     * @return 是否为Excel文件
     */
    public static boolean isExcelFile(File file) {
        String extension = getFileExtension(file);
        return "xlsx".equals(extension) || "xls".equals(extension);
    }

    /**
     * 检查文件是否为CSV文件
     * @param file 文件
     * @return 是否为CSV文件
     */
    public static boolean isCsvFile(File file) {
        String extension = getFileExtension(file);
        return "csv".equals(extension);
    }

    /**
     * 创建目录
     * @param dirPath 目录路径
     * @return 是否创建成功
     */
    public static boolean createDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                LogUtil.info("目录创建成功: " + dirPath);
            }
            return true;
        } catch (IOException e) {
            LogUtil.error("目录创建失败: " + dirPath, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件大小（字节）
     * @param filePath 文件路径
     * @return 文件大小
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            LogUtil.error("获取文件大小失败: " + filePath, e);
            return -1;
        }
    }

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            LogUtil.error("删除文件失败: " + filePath, e);
            return false;
        }
    }

    /**
     * 获取文件名（不含扩展名）
     * @param fileName 文件名
     * @return 文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    /**
     * 生成唯一的文件名
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String baseName = getFileNameWithoutExtension(originalFileName);
        long timestamp = System.currentTimeMillis();
        return baseName + "_" + timestamp + "." + extension;
    }
}
