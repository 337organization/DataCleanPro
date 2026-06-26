package com.datacleanpro.validator;

import com.datacleanpro.model.DataRow;
import com.datacleanpro.model.ValidationResult;
import com.datacleanpro.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 验证引擎
 * 管理和执行验证规则
 */
public class ValidationEngine {
    
    private static final Map<String, Supplier<ValidateRule>> RULE_REGISTRY = new HashMap<>();
    
    static {
        // 注册内置规则
        RULE_REGISTRY.put("PHONE", PhoneRule::new);
        RULE_REGISTRY.put("EMAIL", EmailRule::new);
        RULE_REGISTRY.put("REQUIRED", RequiredRule::new);
        RULE_REGISTRY.put("NUMBER_RANGE", () -> new NumberRangeRule(0, 100));
        RULE_REGISTRY.put("REGEX", () -> new RegexRule(".*"));
        
        LogUtil.info("验证引擎初始化完成，注册规则类型: " + RULE_REGISTRY.keySet());
    }
    
    /**
     * 创建验证规则
     * @param ruleType 规则类型
     * @return 验证规则
     */
    public static ValidateRule createRule(String ruleType) {
        Supplier<ValidateRule> supplier = RULE_REGISTRY.get(ruleType);
        if (supplier == null) {
            LogUtil.warn("未知的规则类型: " + ruleType);
            return null;
        }
        return supplier.get();
    }
    
    /**
     * 创建验证规则
     * @param ruleType 规则类型
     * @param params 参数
     * @return 验证规则
     */
    public static ValidateRule createRule(String ruleType, Map<String, String> params) {
        switch (ruleType) {
            case "PHONE":
                String phoneError = params != null ? params.get("errorMessage") : null;
                return phoneError != null ? new PhoneRule(phoneError) : new PhoneRule();
            case "EMAIL":
                String emailError = params != null ? params.get("errorMessage") : null;
                return emailError != null ? new EmailRule(emailError) : new EmailRule();
            case "REQUIRED":
                String requiredError = params != null ? params.get("errorMessage") : null;
                return requiredError != null ? new RequiredRule(requiredError) : new RequiredRule();
            case "NUMBER_RANGE":
                double min = params != null && params.containsKey("min") ? Double.parseDouble(params.get("min")) : 0;
                double max = params != null && params.containsKey("max") ? Double.parseDouble(params.get("max")) : 100;
                String rangeError = params != null ? params.get("errorMessage") : null;
                return rangeError != null ? new NumberRangeRule(min, max, rangeError) : new NumberRangeRule(min, max);
            case "REGEX":
                String regex = params != null ? params.get("regex") : ".*";
                String regexError = params != null ? params.get("errorMessage") : null;
                return regexError != null ? new RegexRule(regex, regexError) : new RegexRule(regex);
            default:
                LogUtil.warn("未知的规则类型: " + ruleType);
                return null;
        }
    }
    
    /**
     * 验证单个值
     * @param rule 验证规则
     * @param value 值
     * @return 是否通过验证
     */
    public static boolean validate(ValidateRule rule, String value) {
        if (rule == null) {
            return true;
        }
        return rule.validate(value);
    }
    
    /**
     * 验证数据行
     * @param rules 验证规则列表
     * @param row 数据行
     * @param rowIndex 行索引
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> validateRow(List<ValidateRule> rules, DataRow row, int rowIndex, Long fileId) {
        List<ValidationResult> results = new ArrayList<>();
        
        if (rules == null || rules.isEmpty() || row == null) {
            return results;
        }
        
        List<String> fields = row.getFields();
        if (fields == null || fields.isEmpty()) {
            return results;
        }
        
        for (ValidateRule rule : rules) {
            for (int i = 0; i < fields.size(); i++) {
                String value = fields.get(i);
                boolean passed = rule.validate(value);
                
                ValidationResult result = new ValidationResult();
                result.setFileId(fileId);
                result.setRowIndex(rowIndex);
                result.setColumnName("Column_" + i);
                result.setCellValue(value);
                result.setPassed(passed);
                
                if (!passed) {
                    result.setErrorMessage(rule.getErrorMessage());
                }
                
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * 验证数据列表
     * @param rules 验证规则列表
     * @param data 数据行列表
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> validateData(List<ValidateRule> rules, List<DataRow> data, Long fileId) {
        List<ValidationResult> allResults = new ArrayList<>();
        
        if (rules == null || rules.isEmpty() || data == null || data.isEmpty()) {
            return allResults;
        }
        
        LogUtil.info("开始验证数据，规则数量: " + rules.size() + ", 数据行数: " + data.size());
        
        for (int i = 0; i < data.size(); i++) {
            DataRow row = data.get(i);
            List<ValidationResult> rowResults = validateRow(rules, row, i, fileId);
            allResults.addAll(rowResults);
        }
        
        long failedCount = allResults.stream().filter(r -> !r.isPassed()).count();
        LogUtil.info("数据验证完成，总验证项: " + allResults.size() + ", 失败项: " + failedCount);
        
        return allResults;
    }
    
    /**
     * 注册自定义规则类型
     * @param ruleType 规则类型
     * @param supplier 规则提供者
     */
    public static void registerRule(String ruleType, Supplier<ValidateRule> supplier) {
        if (ruleType != null && supplier != null) {
            RULE_REGISTRY.put(ruleType, supplier);
            LogUtil.info("注册自定义规则类型: " + ruleType);
        }
    }
}
