package com.datacleanpro.model;

import java.time.LocalDateTime;

/**
 * 验证规则模型类
 * 表示一个数据验证规则
 */
public class ValidationRule {
    private Long id;
    private String ruleName;
    private String ruleType;
    private String targetColumn;
    private String expression;
    private String errorMessage;
    private boolean active;
    private LocalDateTime createdAt;

    public ValidationRule() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public ValidationRule(String ruleName, String ruleType, String errorMessage) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.errorMessage = errorMessage;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ValidationRule{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", targetColumn='" + targetColumn + '\'' +
                ", active=" + active +
                '}';
    }
}
