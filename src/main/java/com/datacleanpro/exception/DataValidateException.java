package com.datacleanpro.exception;

/**
 * 数据验证异常类
 * 当数据验证失败时抛出
 */
public class DataValidateException extends DataCleanException {
    private String fieldName;
    private String fieldValue;
    private String ruleType;

    public DataValidateException(String message) {
        super("DATA_VALIDATE_ERROR", message);
    }

    public DataValidateException(String message, Throwable cause) {
        super("DATA_VALIDATE_ERROR", message, cause);
    }

    public DataValidateException(String fieldName, String fieldValue, String ruleType) {
        super("DATA_VALIDATE_ERROR", 
              String.format("数据验证失败: 字段 '%s' 的值 '%s' 不符合规则 '%s'", 
                          fieldName, fieldValue, ruleType));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.ruleType = ruleType;
    }

    public DataValidateException(String message, String fieldName, String fieldValue, String ruleType) {
        super("DATA_VALIDATE_ERROR", message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.ruleType = ruleType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    @Override
    public String toString() {
        return "DataValidateException{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                '}';
    }
}
