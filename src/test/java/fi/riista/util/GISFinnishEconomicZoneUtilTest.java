package fi.riista.util;

import fi.riista.feature.common.entity.GeoLocation;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GISFinnishEconomicZoneUtilTest {

    @Test
    public void testSmoke() {
        final GISFinnishEconomicZoneUtil util = GISFinnishEconomicZoneUtil.getInstance();

        assertFalse(util.containsLocation(new GeoLocation(6853998, 297751)));
        assertTrue(util.containsLocation(new GeoLocation(6861333, 130564)));
    }

}
