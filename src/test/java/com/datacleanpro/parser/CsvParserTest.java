package com.datacleanpro.parser;

import com.datacleanpro.model.DataRow;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    @Test
    void testParseSampleCsv() throws Exception {
        File file = new File("src/main/resources/sample/sample.csv");
        assertTrue(file.exists(), "文件不存在: " + file.getAbsolutePath());

        CsvParser parser = new CsvParser();
        List<DataRow> rows = parser.parse(file);

        assertNotNull(rows);
        assertEquals(11, rows.size());
        assertEquals("姓名", rows.get(0).getField(0));
        assertEquals("年龄", rows.get(0).getField(1));
        assertEquals("张三", rows.get(1).getField(0));
        assertEquals(5, rows.get(0).getFieldCount());
    }

    @Test
    void testParseEmployeesCsv() throws Exception {
        File file = new File("src/main/resources/test/employees.csv");
        assertTrue(file.exists());

        CsvParser parser = new CsvParser();
        List<DataRow> rows = parser.parse(file);

        assertNotNull(rows);
        assertEquals(16, rows.size());
        assertEquals("EMP001", rows.get(1).getField(0));
        assertEquals("技术部", rows.get(1).getField(5));
    }

    @Test
    void testParseDuplicateCsv() throws Exception {
        File file = new File("src/main/resources/test/duplicate_data.csv");
        assertTrue(file.exists());

        CsvParser parser = new CsvParser();
        List<DataRow> rows = parser.parse(file);

        assertNotNull(rows);
        assertEquals(21, rows.size());
    }

    @Test
    void testGetSupportedExtensions() {
        CsvParser parser = new CsvParser();
        assertArrayEquals(new String[]{"csv"}, parser.getSupportedExtensions());
    }

    @Test
    void testGetFileTypeDescription() {
        CsvParser parser = new CsvParser();
        assertTrue(parser.getFileTypeDescription().contains("CSV"));
    }
}
