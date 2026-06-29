package com.datacleanpro.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Test
    void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("a"));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("a"));
    }

    @Test
    void testIsNumeric() {
        assertTrue(StringUtil.isNumeric("123"));
        assertFalse(StringUtil.isNumeric("12a3"));
        assertFalse(StringUtil.isNumeric(""));
        assertFalse(StringUtil.isNumeric(null));
    }

    @Test
    void testTruncate() {
        assertEquals("abc", StringUtil.truncate("abc", 5));
        assertEquals("abcde...", StringUtil.truncate("abcdef", 5));
        assertNull(StringUtil.truncate(null, 5));
    }

    @Test
    void testLeftPad() {
        assertEquals("  a", StringUtil.leftPad("a", 3, ' '));
        assertEquals("abc", StringUtil.leftPad("abc", 2, ' '));
    }
}
