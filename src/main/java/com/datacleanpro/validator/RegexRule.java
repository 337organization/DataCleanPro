package com.datacleanpro.validator;

import com.datacleanpro.util.StringUtil;

/**
 * 正则表达式验证规则
 * 使用自定义正则表达式验证
 */
public class RegexRule implements ValidateRule {
    
    private final String regex;
    private final String errorMessage;
    private final String ruleName;
    
    public RegexRule(String regex) {
        this.regex = regex;
        this.errorMessage = "值不符合正则表达式规则: " + regex;
        this.ruleName = "正则表达式验证";
    }
    
    public RegexRule(String regex, String errorMessage) {
        this.regex = regex;
        this.errorMessage = errorMessage;
        this.ruleName = "正则表达式验证";
    }
    
    public RegexRule(String regex, String errorMessage, String ruleName) {
        this.regex = regex;
        this.errorMessage = errorMessage;
        this.ruleName = ruleName;
    }
    
    @Override
    public boolean validate(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        return value.matches(regex);
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String getRuleType() {
        return "REGEX";
    }
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
    
    @Override
    public String getDescription() {
        return "使用正则表达式验证: " + regex;
    }
    
    public String getRegex() {
        return regex;
    }
}
