package fi.riista.feature.gis.zone;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AreaEntityTest extends EmbeddedDatabaseTest {

    @Test
    public void testGetLatestCombinedModificationTime() throws InterruptedException {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubArea area = model().newHuntingClubArea(club);

        // Check that invariant holds.
        assertNull(area.getLifecycleFields().getModificationTime());

        assertNull(area.getLatestCombinedModificationTime());

        persistInNewTransaction();

        final Date areaMtime = area.getLifecycleFields().getModificationTime();

        // Check that invariant holds.
        assertNotNull(areaMtime);

        assertEquals(areaMtime, area.getLatestCombinedModificationTime());

        final GISZone zone = model().newGISZone();
        area.setZone(zone);

        // Check that invariant holds.
        assertNull(zone.getLifecycleFields().getModificationTime());

        assertEquals(areaMtime, area.getLatestCombinedModificationTime());

        persistInNewTransaction();

        // Sleep for 1 millisecond to force zone modification time to differ from area's.
        Thread.sleep(1);

        runInTransaction(() -> zone.setComputedAreaSize(10_000));

        final Date zoneMtime = zone.getLifecycleFields().getModificationTime();

        // Check that invariants hold.
        assertNotNull(zoneMtime);
        assertTrue(zoneMtime.compareTo(area.getLifecycleFields().getModificationTime()) > 0);

        assertEquals(zoneMtime, area.getLatestCombinedModificationTime());
    }
}
