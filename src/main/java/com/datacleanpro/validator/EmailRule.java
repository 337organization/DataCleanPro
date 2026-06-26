package com.datacleanpro.validator;

import com.datacleanpro.util.StringUtil;

/**
 * 邮箱验证规则
 * 验证邮箱格式
 */
public class EmailRule implements ValidateRule {
    
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private final String errorMessage;
    
    public EmailRule() {
        this.errorMessage = "邮箱格式不正确";
    }
    
    public EmailRule(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public boolean validate(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        return value.matches(EMAIL_REGEX);
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String getRuleType() {
        return "EMAIL";
    }
    
    @Override
    public String getRuleName() {
        return "邮箱格式验证";
    }
    
    @Override
    public String getDescription() {
        return "验证邮箱格式（标准邮箱格式）";
    }
}
