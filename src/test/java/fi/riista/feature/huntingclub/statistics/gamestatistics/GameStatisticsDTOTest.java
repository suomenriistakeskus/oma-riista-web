package fi.riista.feature.huntingclub.statistics.gamestatistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameStatisticsDTOTest {

    @Test
    public void testCombineDataAnnually() {
        GameStatisticsDTO dto = new GameStatisticsDTO();
        //Add data for year 2015
        dto.appendTimestampForYear(2015);
        dto.appendData("data1", 10);
        dto.appendData("data2", 50);

        //Add data for same year
        dto.appendTimestampForYear(2015);
        dto.appendData("data1", 2);
        dto.appendData("data2", 3);

        //Add data for same year
        dto.appendTimestampForYear(2015);
        dto.appendData("data1", 1);
        dto.appendData("data2", 1);

        //Add data for different year
        dto.appendTimestampForYear(2016);
        dto.appendData("data1", 888);
        dto.appendData("data2", 999);

        assertEquals(dto.getTimestamps().size(), 4);
        assertFalse(dto.containsAnnuallyCombinedData());

        dto.combineDataAnnually();

        assertTrue(dto.containsAnnuallyCombinedData());
        assertEquals(dto.getTimestamps().size(), 2);
        assertEquals(dto.getTimestamps().get(0).getYear(), 2015);
        assertEquals(dto.getTimestamps().get(1).getYear(), 2016);

        assertEquals(dto.getDatasets().get("data1").size(), 2);
        assertEquals(dto.getDatasets().get("data2").size(), 2);
        //Year 2015 data is combined
        assertEquals(dto.getDatasets().get("data1").get(0).intValue(), 13);
        assertEquals(dto.getDatasets().get("data2").get(0).intValue(), 54);
        //Year 2016 data is unchanged
        assertEquals(dto.getDatasets().get("data1").get(1).intValue(), 888);
        assertEquals(dto.getDatasets().get("data2").get(1).intValue(), 999);
    }

}
