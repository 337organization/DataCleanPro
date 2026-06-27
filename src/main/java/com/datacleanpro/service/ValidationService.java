package com.datacleanpro.service;

import com.datacleanpro.dao.DataRowDAO;
import com.datacleanpro.dao.ValidationResultDAO;
import com.datacleanpro.dao.ValidationRuleDAO;
import com.datacleanpro.model.DataRow;
import com.datacleanpro.model.ValidationResult;
import com.datacleanpro.model.ValidationRule;
import com.datacleanpro.util.LogUtil;
import com.datacleanpro.validator.ValidateRule;
import com.datacleanpro.validator.ValidationEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证服务
 * 管理验证规则和执行验证
 */
public class ValidationService {
    
    /**
     * 获取所有验证规则
     * @return 验证规则列表
     */
    public static List<ValidationRule> getAllRules() {
        return ValidationRuleDAO.findAll();
    }
    
    /**
     * 获取激活的验证规则
     * @return 验证规则列表
     */
    public static List<ValidationRule> getActiveRules() {
        return ValidationRuleDAO.findActiveRules();
    }
    
    /**
     * 根据ID获取验证规则
     * @param id 规则ID
     * @return 验证规则
     */
    public static ValidationRule getRuleById(Long id) {
        return ValidationRuleDAO.findById(id);
    }
    
    /**
     * 保存验证规则
     * @param rule 验证规则
     * @return 规则ID
     */
    public static Long saveRule(ValidationRule rule) {
        if (rule.getId() == null) {
            return ValidationRuleDAO.insert(rule);
        } else {
            ValidationRuleDAO.update(rule);
            return rule.getId();
        }
    }
    
    /**
     * 删除验证规则
     * @param id 规则ID
     * @return 是否成功
     */
    public static boolean deleteRule(Long id) {
        return ValidationRuleDAO.deleteById(id);
    }
    
    /**
     * 验证文件数据
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> validateFile(Long fileId) {
        LogUtil.info("开始验证文件，文件ID: " + fileId);
        
        try {
            // 1. 获取数据
            List<DataRow> rows = DataRowDAO.findByFileId(fileId);
            if (rows.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 获取激活的规则
            List<ValidationRule> rules = ValidationRuleDAO.findActiveRules();
            if (rules.isEmpty()) {
                LogUtil.info("没有激活的验证规则");
                return new ArrayList<>();
            }
            
            // 3. 转换并执行规则
            List<ValidationResult> results = new ArrayList<>();
            for (ValidationRule rule : rules) {
                ValidateRule validateRule = createValidateRule(rule);
                if (validateRule == null) {
                    continue;
                }

                for (DataRow row : rows) {
                    if (row.getFields() == null) {
                        continue;
                    }
                    for (int i = 0; i < row.getFields().size(); i++) {
                        String value = row.getFields().get(i);
                        boolean passed = validateRule.validate(value);
                        ValidationResult result = new ValidationResult();
                        result.setFileId(fileId);
                        result.setRuleId(rule.getId());
                        result.setRowIndex(row.getRowIndex());
                        result.setColumnName("列 " + (i + 1));
                        result.setCellValue(value);
                        result.setPassed(passed);
                        if (!passed) {
                            result.setErrorMessage(validateRule.getErrorMessage());
                        }
                        results.add(result);
                    }
                }
            }

            // 4. 保存结果
            if (!results.isEmpty()) {
                ValidationResultDAO.deleteByFileId(fileId);
                ValidationResultDAO.batchInsert(results);
            }
            
            LogUtil.info("文件验证完成，文件ID: " + fileId + ", 结果数量: " + results.size());
            return results;
            
        } catch (Exception e) {
            LogUtil.error("文件验证失败，文件ID: " + fileId, e);
            throw e;
        }
    }
    
    /**
     * 获取文件的验证结果
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> getValidationResults(Long fileId) {
        return ValidationResultDAO.findByFileId(fileId);
    }
    
    /**
     * 获取文件的失败验证结果
     * @param fileId 文件ID
     * @return 验证结果列表
     */
    public static List<ValidationResult> getFailedValidationResults(Long fileId) {
        return ValidationResultDAO.findFailedByFileId(fileId);
    }
    
    /**
     * 获取验证结果统计
     * @param fileId 文件ID
     * @return 统计信息
     */
    public static Map<String, Object> getValidationStatistics(Long fileId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        long totalResults = ValidationResultDAO.countByFileId(fileId);
        long failedResults = ValidationResultDAO.countFailedByFileId(fileId);
        long passedResults = totalResults - failedResults;
        
        stats.put("totalResults", totalResults);
        stats.put("passedResults", passedResults);
        stats.put("failedResults", failedResults);
        stats.put("passRate", totalResults > 0 ? (double) passedResults / totalResults * 100 : 0);
        
        return stats;
    }

    /**
     * 根据规则配置创建验证器
     * @param rule 验证规则配置
     * @return 验证器
     */
    private static ValidateRule createValidateRule(ValidationRule rule) {
        Map<String, String> params = new HashMap<>();
        if (rule.getErrorMessage() != null) {
            params.put("errorMessage", rule.getErrorMessage());
        }
        if (rule.getExpression() != null) {
            params.put("regex", rule.getExpression());
            String[] range = rule.getExpression().split("-");
            if (range.length == 2) {
                params.put("min", range[0]);
                params.put("max", range[1]);
            }
        }
        return ValidationEngine.createRule(rule.getRuleType(), params);
    }
}
