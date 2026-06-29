package com.datacleanpro.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumberRangeRuleTest {

    @Test
    void testWithinRange() {
        NumberRangeRule rule = new NumberRangeRule(0, 100);
        assertTrue(rule.validate("50"));
        assertTrue(rule.validate("0"));
        assertTrue(rule.validate("100"));
    }

    @Test
    void testOutOfRange() {
        NumberRangeRule rule = new NumberRangeRule(0, 100);
        assertFalse(rule.validate("-1"));
        assertFalse(rule.validate("101"));
    }

    @Test
    void testNonNumeric() {
        NumberRangeRule rule = new NumberRangeRule(0, 100);
        assertFalse(rule.validate("abc"));
        assertFalse(rule.validate(""));
        assertFalse(rule.validate(null));
    }
}
