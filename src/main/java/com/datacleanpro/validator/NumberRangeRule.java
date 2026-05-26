package com.datacleanpro.validator;

import com.datacleanpro.util.StringUtil;

/**
 * 数字范围验证规则
 * 验证数字是否在指定范围内
 */
public class NumberRangeRule implements ValidateRule {
    
    private final double min;
    private final double max;
    private final String errorMessage;
    
    public NumberRangeRule(double min, double max) {
        this.min = min;
        this.max = max;
        this.errorMessage = "数值范围应在 " + min + " 到 " + max + " 之间";
    }
    
    public NumberRangeRule(double min, double max, String errorMessage) {
        this.min = min;
        this.max = max;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public boolean validate(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        
        try {
            double numValue = Double.parseDouble(value);
            return numValue >= min && numValue <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String getRuleType() {
        return "NUMBER_RANGE";
    }
    
    @Override
    public String getRuleName() {
        return "数字范围验证";
    }
    
    @Override
    public String getDescription() {
        return "验证数字是否在 " + min + " 到 " + max + " 之间";
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
}
