package com.datacleanpro.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneRuleTest {

    @Test
    void testValidPhones() {
        PhoneRule rule = new PhoneRule();
        assertTrue(rule.validate("13800138000"));
        assertTrue(rule.validate("15912345678"));
        assertTrue(rule.validate("19912345678"));
    }

    @Test
    void testInvalidPhones() {
        PhoneRule rule = new PhoneRule();
        assertFalse(rule.validate("12345678901"));
        assertFalse(rule.validate("1380013800"));
        assertFalse(rule.validate("23800138000"));
        assertFalse(rule.validate("1380013800a"));
    }

    @Test
    void testEmptyOrNull() {
        PhoneRule rule = new PhoneRule();
        assertFalse(rule.validate(""));
        assertFalse(rule.validate(null));
    }

    @Test
    void testCustomErrorMessage() {
        PhoneRule rule = new PhoneRule("自定义错误");
        assertEquals("自定义错误", rule.getErrorMessage());
    }
}
