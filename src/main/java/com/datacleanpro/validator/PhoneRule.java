package com.datacleanpro.validator;

import com.datacleanpro.util.StringUtil;

/**
 * 手机号验证规则
 * 验证中国大陆手机号格式
 */
public class PhoneRule implements ValidateRule {
    
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private final String errorMessage;
    
    public PhoneRule() {
        this.errorMessage = "手机号格式不正确";
    }
    
    public PhoneRule(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public boolean validate(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        return value.matches(PHONE_REGEX);
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String getRuleType() {
        return "PHONE";
    }
    
    @Override
    public String getRuleName() {
        return "手机号格式验证";
    }
    
    @Override
    public String getDescription() {
        return "验证中国大陆手机号格式（11位数字，以1开头，第二位3-9）";
    }
}
