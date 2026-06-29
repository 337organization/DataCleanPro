package com.datacleanpro.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequiredRuleTest {

    @Test
    void testNonBlankPasses() {
        RequiredRule rule = new RequiredRule();
        assertTrue(rule.validate("hello"));
        assertTrue(rule.validate("x"));
    }

    @Test
    void testBlankFails() {
        RequiredRule rule = new RequiredRule();
        assertFalse(rule.validate(""));
        assertFalse(rule.validate(null));
    }
}
