package com.datacleanpro.validator;

import com.datacleanpro.util.StringUtil;

/**
 * 必填验证规则
 * 验证字段是否为空
 */
public class RequiredRule implements ValidateRule {
    
    private final String errorMessage;
    
    public RequiredRule() {
        this.errorMessage = "该字段为必填项";
    }
    
    public RequiredRule(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public boolean validate(String value) {
        return StringUtil.isNotBlank(value);
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String getRuleType() {
        return "REQUIRED";
    }
    
    @Override
    public String getRuleName() {
        return "必填字段验证";
    }
    
    @Override
    public String getDescription() {
        return "验证字段是否为空或空白";
    }
}
