package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HarvestPermitApplicationTypeFactoryTest {

    private static HarvestPermitApplicationTypeFactory factory(final LocalDateTime now) {
        return new HarvestPermitApplicationTypeFactory(now);
    }

    @Test
    public void testMooselikePeriod_FirstDay() {
        final LocalDateTime now = new LocalDateTime(2020, 4, 1, 0, 0);
        final HarvestPermitApplicationTypeDTO dto = factory(now).mooselikeForHuntingYear(2020);

        assertEquals(HarvestPermitCategory.MOOSELIKE, dto.getCategory());
        assertEquals(new LocalDateTime(2020, 4, 1, 0, 0), dto.getBegin());
        assertEquals(new LocalDateTime(2020, 4, 30, 16, 15), dto.getEnd());
        assertEquals(2020, dto.getHuntingYear());
        assertEquals(new BigDecimal("100.00"), dto.getPrice());
        assertTrue(dto.isActive());
    }

    @Test
    public void testMooselikePeriod_BeforeFirstDay() {
        final LocalDateTime now = new LocalDateTime(2020, 3, 31, 23, 59);
        final HarvestPermitApplicationTypeDTO dto = factory(now).mooselikeForHuntingYear(2020);

        assertEquals(HarvestPermitCategory.MOOSELIKE, dto.getCategory());
        assertEquals(new LocalDateTime(2020, 4, 1, 0, 0), dto.getBegin());
        assertEquals(new LocalDateTime(2020, 4, 30, 16, 15), dto.getEnd());
        assertEquals(2020, dto.getHuntingYear());
        assertEquals(new BigDecimal("100.00"), dto.getPrice());
        assertFalse(dto.isActive());
    }

    @Test
    public void testBird() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).birdForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.BIRD, new BigDecimal("70.00"));
        });
    }

    @Test
    public void testMammal() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).mammalForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.MAMMAL, new BigDecimal("70.00"));
        });
    }

    private static void doAssertValuesForAlwaysActive(final HarvestPermitApplicationTypeDTO dto,
                                                      final HarvestPermitCategory expectedCategory,
                                                      final BigDecimal expectedPrice) {
        assertEquals(expectedCategory, dto.getCategory());
        assertNull(dto.getBegin());
        assertNull(dto.getEnd());
        assertEquals(2019, dto.getHuntingYear());
        assertEquals(expectedPrice, dto.getPrice());
        assertTrue(dto.isActive());
    }

    // There are no predefined application periods for large carnivore permits, so assertions whether the
    // application type should be active is updated by request from Riistakeskus
    @Test
    public void testKannanhoidollinenKarhu() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).bearForHuntingYear(2019);
            assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_BEAR, dto.getCategory());
            assertEquals(2019, dto.getHuntingYear());
            assertEquals(new BigDecimal("200.00"), dto.getPrice());
            assertTrue(dto.isActive());
        });
    }

    @Test
    public void testKannanhoidollinenIlves_disabledEveryDay() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).lynxForHuntingYear(2022);
            assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
            assertEquals(2022, dto.getHuntingYear());
            assertEquals(new BigDecimal("200.00"), dto.getPrice());
            assertFalse(dto.isActive());
        });
    }

    @Test
    public void testKannanhoidollinenIlves_2021_firstDay() {
        final LocalDateTime now = new LocalDateTime(2021, 9, 14, 00, 00);
        final HarvestPermitApplicationTypeDTO dto = factory(now).lynxForHuntingYear(2021);

        assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
        assertEquals(new LocalDateTime(2021, 9, 14, 0, 0), dto.getBegin());
        assertEquals(new LocalDateTime(2021, 10, 10, 23, 59), dto.getEnd());
        assertEquals(2021, dto.getHuntingYear());
        assertEquals(new BigDecimal("200.00"), dto.getPrice());
        assertTrue(dto.isActive());
    }

    @Test
    public void testKannanhoidollinenIlves_2021_beforeFirstDay() {
        final LocalDateTime now = new LocalDateTime(2021, 9, 13, 23, 59);
        final HarvestPermitApplicationTypeDTO dto = factory(now).lynxForHuntingYear(2021);

        assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
        assertEquals(new LocalDateTime(2021, 9, 14, 0, 0), dto.getBegin());
        assertEquals(new LocalDateTime(2021, 10, 10, 23, 59), dto.getEnd());
        assertEquals(2021, dto.getHuntingYear());
        assertEquals(new BigDecimal("200.00"), dto.getPrice());
        assertFalse(dto.isActive());
    }

    @Test
    public void testKannanhoidollinenIlves_2021_afterLastDay() {
        final LocalDateTime now = new LocalDateTime(2021, 10, 11, 0, 0);
        final HarvestPermitApplicationTypeDTO dto = factory(now).lynxForHuntingYear(2021);

        assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
        assertEquals(new LocalDateTime(2021, 9, 14, 0, 0), dto.getBegin());
        assertEquals(new LocalDateTime(2021, 10, 10, 23, 59), dto.getEnd());
        assertEquals(2021, dto.getHuntingYear());
        assertEquals(new BigDecimal("200.00"), dto.getPrice());
        assertFalse(dto.isActive());
    }

    @Test
    public void testKannanhoidollinenIlvesPoronhoitoalueella() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).lynxPoronhoitoForHuntingYear(2019);
            assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO, dto.getCategory());
            assertEquals(2019, dto.getHuntingYear());
            assertEquals(new BigDecimal("200.00"), dto.getPrice());
            assertTrue(dto.isActive());
        });
    }

    @Test
    public void testNestRemoval() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).nestRemovalForCalendarYear(2019);
            assertEquals(HarvestPermitCategory.NEST_REMOVAL, dto.getCategory());
            assertEquals(2019, dto.getHuntingYear());
            assertEquals(new BigDecimal("70.00"), dto.getPrice());
            assertTrue(dto.isActive());
        });
    }

    @Test
    public void testLawSection10() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).lawSectionTenForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.LAW_SECTION_TEN, new BigDecimal("110.00"));
        });
    }

    @Test
    public void testWeaponTransportation() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).weaponTransportationForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.WEAPON_TRANSPORTATION, new BigDecimal("70.00"));
        });
    }

    @Test
    public void testDisability() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).disabilityForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.DISABILITY, new BigDecimal("155.00"));
        });
    }

    @Test
    public void testDogDisturbance() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).dogDisturbanceForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.DOG_DISTURBANCE, new BigDecimal("80.00"));
        });
    }

    @Test
    public void testDogUnleash() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).dogUnleashForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.DOG_UNLEASH, new BigDecimal("80.00"));
        });
    }

    @Test
    public void testDeportation() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).deportationForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.DEPORTATION, new BigDecimal("0.00"));
        });
    }

    @Test
    public void testResearch() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).researchForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.RESEARCH, new BigDecimal("70.00"));
        });
    }

    @Test
    public void testImporting() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).importingForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.IMPORTING, new BigDecimal("140.00"));
        });
    }

    public void testGameManagement() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).gameManagementForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.GAME_MANAGEMENT, new BigDecimal("100.00"));
        });
    }

    private static void assertForEveryDayOfYear(final Consumer<LocalDateTime> consumer) {
        final LocalDateTime firsDayOfYear = new LocalDateTime(2019, 1, 1, 0, 0);
        IntStream.range(0, 365).forEach(plusDays -> {
            consumer.accept(firsDayOfYear.plusDays(plusDays));
        });
    }
}
