package com.datacleanpro.cleaner;

import com.datacleanpro.model.DataRow;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateCleanerTest {

    @Test
    void testRemoveDuplicates() {
        DuplicateCleaner cleaner = new DuplicateCleaner();
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", "b")));
        data.add(new DataRow(1L, 1, Arrays.asList("c", "d")));
        data.add(new DataRow(1L, 2, Arrays.asList("a", "b")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(3, result.getTotalRows());
        assertEquals(1, result.getRemovedRows());
        assertEquals(2, data.size());
    }

    @Test
    void testNoDuplicates() {
        DuplicateCleaner cleaner = new DuplicateCleaner();
        List<DataRow> data = new ArrayList<>();
        data.add(new DataRow(1L, 0, Arrays.asList("a", "b")));
        data.add(new DataRow(1L, 1, Arrays.asList("c", "d")));

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(2, result.getTotalRows());
        assertEquals(0, result.getRemovedRows());
        assertEquals(2, data.size());
    }

    @Test
    void testEmptyData() {
        DuplicateCleaner cleaner = new DuplicateCleaner();
        List<DataRow> data = new ArrayList<>();

        DataCleaner.CleanResult result = cleaner.clean(data);

        assertEquals(0, result.getTotalRows());
    }

    @Test
    void testNullData() {
        DuplicateCleaner cleaner = new DuplicateCleaner();
        DataCleaner.CleanResult result = cleaner.clean(null);

        assertEquals(0, result.getTotalRows());
    }

    @Test
    void testGetNameAndDescription() {
        DuplicateCleaner cleaner = new DuplicateCleaner();
        assertNotNull(cleaner.getName());
        assertNotNull(cleaner.getDescription());
    }
}
