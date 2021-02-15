package fi.riista.feature.permit.application.carnivore.species;

import com.google.common.collect.Range;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_BEAR;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF;
import static org.junit.Assert.assertEquals;


public class CarnivorePermitSpeciesTest implements DefaultEntitySupplierProvider {

    private Riistanhoitoyhdistys rhy;

    @Before
    public void setup() {
        rhy = getEntitySupplier().newRiistanhoitoyhdistys();
    }

    @Test
    public void testPeriod_bear() {
        final LocalDate expectedBegin = new LocalDate(2019, 8, 20);
        final LocalDate expectedEnd = new LocalDate(2019, 10, 31);
        final HarvestPermitApplication application = createApplication(LARGE_CARNIVORE_BEAR, 2019);
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);
        assertEquals(expectedBegin, period.lowerEndpoint());
        assertEquals(expectedEnd, period.upperEndpoint());
    }

    @Test
    public void testPeriod_lynx() {
        final LocalDate expectedBegin = new LocalDate(2020, 12, 1);
        final LocalDate expectedEnd = new LocalDate(2021, 2, 28);
        final HarvestPermitApplication application = createApplication(LARGE_CARNIVORE_LYNX, 2020);
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);
        assertEquals(expectedBegin, period.lowerEndpoint());
        assertEquals(expectedEnd, period.upperEndpoint());
    }

    @Test
    public void testPeriod_lynx_leapYear() {
        final LocalDate expectedBegin = new LocalDate(2019, 12, 1);
        final LocalDate expectedEnd = new LocalDate(2020, 2, 29);
        final HarvestPermitApplication application = createApplication(LARGE_CARNIVORE_LYNX, 2019);
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);
        assertEquals(expectedBegin, period.lowerEndpoint());
        assertEquals(expectedEnd, period.upperEndpoint());
    }

    @Test
    public void testPeriod_lynxPoronhoito() {
        final LocalDate expectedBegin = new LocalDate(2020, 10, 1);
        final LocalDate expectedEnd = new LocalDate(2021, 2, 28);
        final HarvestPermitApplication application = createApplication(LARGE_CARNIVORE_LYNX_PORONHOITO, 2020);
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);
        assertEquals(expectedBegin, period.lowerEndpoint());
        assertEquals(expectedEnd, period.upperEndpoint());
    }

    @Test
    public void testPeriod_lynxPoronhoito_leapYear() {
        final LocalDate expectedBegin = new LocalDate(2019, 10, 1);
        final LocalDate expectedEnd = new LocalDate(2020, 2, 29);
        final HarvestPermitApplication application = createApplication(LARGE_CARNIVORE_LYNX_PORONHOITO, 2019);
        final Range<LocalDate> period = CarnivorePermitSpecies.getPeriod(application);
        assertEquals(expectedBegin, period.lowerEndpoint());
        assertEquals(expectedEnd, period.upperEndpoint());
    }

    @Test
    public void testSpecies() {
        assertEquals(GameSpecies.OFFICIAL_CODE_BEAR, CarnivorePermitSpecies.getSpecies(LARGE_CARNIVORE_BEAR));
        assertEquals(GameSpecies.OFFICIAL_CODE_LYNX, CarnivorePermitSpecies.getSpecies(LARGE_CARNIVORE_LYNX));
        assertEquals(GameSpecies.OFFICIAL_CODE_LYNX,
                CarnivorePermitSpecies.getSpecies(LARGE_CARNIVORE_LYNX_PORONHOITO));
        assertEquals(GameSpecies.OFFICIAL_CODE_WOLF, CarnivorePermitSpecies.getSpecies(LARGE_CARNIVORE_WOLF));
    }

    @Test
    public void testAssertSpecies() {
        CarnivorePermitSpecies.assertSpecies(LARGE_CARNIVORE_BEAR, GameSpecies.OFFICIAL_CODE_BEAR);
        CarnivorePermitSpecies.assertSpecies(LARGE_CARNIVORE_LYNX, GameSpecies.OFFICIAL_CODE_LYNX);
        CarnivorePermitSpecies.assertSpecies(LARGE_CARNIVORE_LYNX_PORONHOITO, GameSpecies.OFFICIAL_CODE_LYNX);
        CarnivorePermitSpecies.assertSpecies(LARGE_CARNIVORE_WOLF, GameSpecies.OFFICIAL_CODE_WOLF);
    }

    @Test
    public void testAssertSpecies_invalidSpecies() {
        assertAllOtherThrows(LARGE_CARNIVORE_BEAR, GameSpecies.OFFICIAL_CODE_BEAR);
        assertAllOtherThrows(LARGE_CARNIVORE_LYNX, GameSpecies.OFFICIAL_CODE_LYNX);
        assertAllOtherThrows(LARGE_CARNIVORE_LYNX_PORONHOITO, GameSpecies.OFFICIAL_CODE_LYNX);
        assertAllOtherThrows(LARGE_CARNIVORE_WOLF, GameSpecies.OFFICIAL_CODE_WOLF);
    }

    private static void assertAllOtherThrows(final HarvestPermitCategory category, final int officialCode) {
        Arrays.stream(GameSpecies.ALL_GAME_SPECIES_CODES)
                .filter(code -> code != officialCode)
                .forEach(code -> {
                    try {
                        CarnivorePermitSpecies.assertSpecies(category, code);
                    } catch (IllegalArgumentException iae) {
                        return;
                    }
                    Assert.fail("Should have thrown an exception");
                });
    }

    private HarvestPermitApplication createApplication(final HarvestPermitCategory largeCarnivoreLynxPoronhoito,
                                                       final int applicationYear) {
        final HarvestPermitApplication application = getEntitySupplier().newHarvestPermitApplication(rhy, null,
                largeCarnivoreLynxPoronhoito);
        application.setApplicationYear(applicationYear);
        return application;
    }
}
