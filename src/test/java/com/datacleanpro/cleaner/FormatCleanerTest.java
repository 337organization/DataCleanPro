package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatCleanerTest {

    @Test
    void testTrimWhitespace() {
        FormatCleaner cleaner = new FormatCleaner(true, false, false);
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("  张三 ", "  25  ")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(1, result.getAffectedRows());
        assertEquals("张三", data.get(0).getField(0));
        assertEquals("25", data.get(0).getField(1));
    }

    @Test
    void testNormalizeCase() {
        FormatCleaner cleaner = new FormatCleaner(false, true, false);
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("ZhangSan", "Hello World")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(1, result.getAffectedRows());
        assertEquals("zhangsan", data.get(0).getField(0));
        assertEquals("hello world", data.get(0).getField(1));
    }

    @Test
    void testNoChange() {
        FormatCleaner cleaner = new FormatCleaner(true, false, false);
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", "b")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(0, result.getAffectedRows());
    }

    @Test
    void testEmptyData() {
        FormatCleaner cleaner = new FormatCleaner();
        DataCleaner.CleanResult result = cleaner.clean(new ArrayList<>());

        assertEquals(0, result.getTotalRows());
    }
}
