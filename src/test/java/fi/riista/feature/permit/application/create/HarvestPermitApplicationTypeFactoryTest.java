package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HarvestPermitApplicationTypeFactoryTest {

    private static HarvestPermitApplicationTypeFactory factory(final LocalDate today) {
        return new HarvestPermitApplicationTypeFactory(today);
    }

    @Test
    public void testAll_mooseAndBirdShouldBeActive() {
        final LocalDate today = new LocalDate(2019, 4, 3);
        final List<HarvestPermitApplicationTypeDTO> list = factory(today).listAll();
        // mooselike, bird, bear, lynx, lynx poronhoito
        assertEquals(5, list.size());

        for (HarvestPermitApplicationTypeDTO dto : list) {
            if (dto.getCategory() == HarvestPermitCategory.MOOSELIKE ||
                    dto.getCategory() == HarvestPermitCategory.BIRD) {
                assertTrue(dto.isActive());
            } else {
                assertFalse(dto.isActive());
            }
        }
    }

    @Test
    public void testMooselikePeriod_FirstDay() {
        final LocalDate today = new LocalDate(2019, 4, 3);
        final HarvestPermitApplicationTypeDTO dto = factory(today).mooselikeForHuntingYear(2019);

        assertEquals(HarvestPermitCategory.MOOSELIKE, dto.getCategory());
        assertEquals(new LocalDate(2019, 4, 3), dto.getBegin());
        assertEquals(new LocalDate(2019, 4, 30), dto.getEnd());
        assertEquals(2019, dto.getHuntingYear());
        assertEquals(new BigDecimal("90.00"), dto.getPrice());
        assertTrue(dto.isActive());
    }

    @Test
    public void testMooselikePeriod_BeforeFirstDay() {
        final LocalDate today = new LocalDate(2019, 4, 2);
        final HarvestPermitApplicationTypeDTO dto = factory(today).mooselikeForHuntingYear(2019);

        assertEquals(HarvestPermitCategory.MOOSELIKE, dto.getCategory());
        assertEquals(new LocalDate(2019, 4, 3), dto.getBegin());
        assertEquals(new LocalDate(2019, 4, 30), dto.getEnd());
        assertEquals(2019, dto.getHuntingYear());
        assertEquals(new BigDecimal("90.00"), dto.getPrice());
        assertFalse(dto.isActive());
    }

    @Test
    public void testBird() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).birdForCalendarYear(2019);
            doAssertValuesForAlwaysActive(dto, HarvestPermitCategory.BIRD, new BigDecimal("70.00"));
        });
    }

    private void doAssertValuesForAlwaysActive(final HarvestPermitApplicationTypeDTO dto,
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
            assertFalse(dto.isActive());
        });
    }

    @Test
    @Ignore(value = "Activate after lynx period is over")
    public void testKannanhoidollinenIlves() {
        assertForEveryDayOfYear(today -> {
            final HarvestPermitApplicationTypeDTO dto = factory(today).lynxForHuntingYear(2019);
            assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
            assertEquals(2019, dto.getHuntingYear());
            assertEquals(new BigDecimal("200.00"), dto.getPrice());
            assertFalse(dto.isActive());
        });
    }

    @Test
    public void testLynxPeriod_FirstDay() {
        final LocalDate today = new LocalDate(2019, 9, 10);
        final HarvestPermitApplicationTypeDTO dto = factory(today).lynxForHuntingYear(2019);

        assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
        assertEquals(new LocalDate(2019, 9, 10), dto.getBegin());
        assertEquals(new LocalDate(2019, 9, 30), dto.getEnd());
        assertEquals(2019, dto.getHuntingYear());
        assertEquals(new BigDecimal("200.00"), dto.getPrice());
        assertTrue(dto.isActive());
    }

    @Test
    public void testLynxPeriod_BeforeFirstDay() {
        final LocalDate today = new LocalDate(2019, 9, 9);
        final HarvestPermitApplicationTypeDTO dto = factory(today).lynxForHuntingYear(2019);

        assertEquals(HarvestPermitCategory.LARGE_CARNIVORE_LYNX, dto.getCategory());
        assertEquals(new LocalDate(2019, 9, 10), dto.getBegin());
        assertEquals(new LocalDate(2019, 9, 30), dto.getEnd());
        assertEquals(2019, dto.getHuntingYear());
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
            assertFalse(dto.isActive());
        });
    }


    private void assertForEveryDayOfYear(final Consumer<LocalDate> consumer) {
        final LocalDate firsDayOfYear = new LocalDate(2019, 1, 1);
        IntStream.range(0, 365).forEach(plusDays -> {
            consumer.accept(firsDayOfYear.plusDays(plusDays));
        });
    }
}
