package com.datacleanpro.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailRuleTest {

    @Test
    void testValidEmails() {
        EmailRule rule = new EmailRule();
        assertTrue(rule.validate("test@example.com"));
        assertTrue(rule.validate("user.name+tag@company.co.uk"));
        assertTrue(rule.validate("a@b.cn"));
    }

    @Test
    void testInvalidEmails() {
        EmailRule rule = new EmailRule();
        assertFalse(rule.validate("not-an-email"));
        assertFalse(rule.validate("@example.com"));
        assertFalse(rule.validate("user@"));
    }

    @Test
    void testEmptyOrNull() {
        EmailRule rule = new EmailRule();
        assertFalse(rule.validate(""));
        assertFalse(rule.validate(null));
    }
}
