package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EmptyValueCleanerTest {

    @Test
    void testFillEmptyWithDefault() {
        EmptyValueCleaner cleaner = new EmptyValueCleaner();
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", "", "c")));
        data.add(new DataRow(1L, 1, Arrays.asList("d", "e", "")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(2, result.getAffectedRows());
        assertEquals("", data.get(0).getField(1));
        assertEquals("", data.get(1).getField(2));
    }

    @Test
    void testRemoveEmptyRows() {
        EmptyValueCleaner cleaner = new EmptyValueCleaner(true, "");
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", "b")));
        data.add(new DataRow(1L, 1, Arrays.asList("", "")));
        data.add(new DataRow(1L, 2, Arrays.asList("", null)));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(2, result.getRemovedRows());
        assertEquals(1, data.size());
    }

    @Test
    void testNullField() {
        EmptyValueCleaner cleaner = new EmptyValueCleaner();
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", null, "c")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(1, result.getAffectedRows());
        assertEquals("", data.get(0).getField(1));
    }

    @Test
    void testEmptyData() {
        EmptyValueCleaner cleaner = new EmptyValueCleaner();
        DataCleaner.CleanResult result = cleaner.clean(new ArrayList<>());

        assertEquals(0, result.getTotalRows());
    }
}
