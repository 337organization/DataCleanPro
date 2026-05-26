package com.datacleanpro.validator;

/**
 * 验证规则接口
 * 定义数据验证的通用接口
 */
public interface ValidateRule {
    
    /**
     * 验证值是否符合规则
     * @param value 要验证的值
     * @return 是否符合规则
     */
    boolean validate(String value);
    
    /**
     * 获取错误消息
     * @return 错误消息
     */
    String getErrorMessage();
    
    /**
     * 获取规则类型
     * @return 规则类型
     */
    String getRuleType();
    
    /**
     * 获取规则名称
     * @return 规则名称
     */
    String getRuleName();
    
    /**
     * 获取规则描述
     * @return 规则描述
     */
    String getDescription();
}
