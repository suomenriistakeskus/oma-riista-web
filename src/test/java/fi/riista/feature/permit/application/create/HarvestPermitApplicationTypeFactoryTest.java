package fi.riista.feature.permit.application.create;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.schedule.HarvestPermitApplicationScheduleDTO;
import fi.riista.feature.permit.decision.PermitDecisionPaymentAmount;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDateTime;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import static fi.riista.feature.permit.decision.PermitDecision.DecisionType.HARVEST_PERMIT;
import static fi.riista.util.DateUtil.toDateTimeNullSafe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(Theories.class)
public class HarvestPermitApplicationTypeFactoryTest extends EmbeddedDatabaseTest {

    private static HarvestPermitApplicationTypeFactory factory(final LocalDateTime now) {
        return new HarvestPermitApplicationTypeFactory(now);
    }

    @Theory
    public void testBeginTime(final HarvestPermitCategory harvestPermitCategory) {
        final LocalDateTime beginTime = new LocalDateTime(2020, 4, 1, 0, 0);
        final LocalDateTime endTime = new LocalDateTime(2020, 4, 30, 16, 15);
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, beginTime, endTime);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 4, 1, 0, 0);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    beginTime,
                    endTime,
                    now.getYear(),
                    expectedPrice,
                    true);
        });
    }

    @Theory
    public void testBeforeBeginTime(final HarvestPermitCategory harvestPermitCategory) {
        final LocalDateTime beginTime = new LocalDateTime(2020, 4, 1, 0, 0);
        final LocalDateTime endTime = new LocalDateTime(2020, 4, 30, 16, 15);
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, beginTime, endTime);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 3, 31, 23, 59);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    beginTime,
                    endTime,
                    now.getYear(),
                    expectedPrice,
                    false);
        });
    }

    @Theory
    public void testEndTime(final HarvestPermitCategory harvestPermitCategory) {
        final LocalDateTime beginTime = new LocalDateTime(2020, 4, 1, 0, 0);
        final LocalDateTime endTime = new LocalDateTime(2020, 4, 30, 16, 15);
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, beginTime, endTime);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 4, 30, 16, 15);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    beginTime,
                    endTime,
                    now.getYear(),
                    expectedPrice,
                    true);
        });
    }

    @Theory
    public void testAfterEndTime(final HarvestPermitCategory harvestPermitCategory) {
        final LocalDateTime beginTime = new LocalDateTime(2020, 4, 1, 0, 0);
        final LocalDateTime endTime = new LocalDateTime(2020, 4, 30, 16, 15);
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, beginTime, endTime);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 4, 30, 16, 16);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    beginTime,
                    endTime,
                    now.getYear(),
                    expectedPrice,
                    false);
        });
    }

    @Theory
    public void testClosed(final HarvestPermitCategory harvestPermitCategory) {
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, false);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 4, 2, 12, 00);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    null,
                    null,
                    now.getYear(),
                    expectedPrice,
                    false);
        });
    }

    @Theory
    public void testOpen(final HarvestPermitCategory harvestPermitCategory) {
        final HarvestPermitApplicationScheduleDTO schedule = createSchedule(harvestPermitCategory, true);

        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(harvestPermitCategory, 1);
        final BigDecimal expectedPrice = PermitDecisionPaymentAmount.getDefaultPaymentAmount(HARVEST_PERMIT, permitTypeCode);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final LocalDateTime now = new LocalDateTime(2020, 3, 30, 12, 00);
            final HarvestPermitApplicationTypeDTO dto =
                    factory(now).resolve(schedule);

            assertTypeDTO(
                    dto,
                    harvestPermitCategory,
                    null,
                    null,
                    now.getYear(),
                    expectedPrice,
                    true);
        });
    }

    private void assertTypeDTO(final HarvestPermitApplicationTypeDTO dto,
                               final HarvestPermitCategory expectedCategory,
                               final LocalDateTime expectedBeginTime,
                               final LocalDateTime expectedEndTime,
                               final int expectedYear,
                               final BigDecimal expectedPrice,
                               final boolean expectedActive) {
        assertThat(dto.getCategory(), is(equalTo(expectedCategory)));
        assertThat(dto.getBegin(), is(equalTo(expectedBeginTime)));
        assertThat(dto.getEnd(), is(equalTo(expectedEndTime)));
        assertThat(dto.getHuntingYear(), is(equalTo(expectedYear)));
        assertThat(dto.getPrice(), is(equalTo(expectedPrice)));
        assertThat(dto.isActive(), is(equalTo(expectedActive)));
    }

    private HarvestPermitApplicationScheduleDTO createSchedule(final HarvestPermitCategory category, final boolean activeOverride) {
        return createSchedule(category, null, null, activeOverride);
    }

    private HarvestPermitApplicationScheduleDTO createSchedule(final HarvestPermitCategory category,
                                                               final LocalDateTime beginTime,
                                                               final LocalDateTime endTime) {
        return createSchedule(category, beginTime, endTime, null);
    }

    private HarvestPermitApplicationScheduleDTO createSchedule(final HarvestPermitCategory category,
                                                               final LocalDateTime beginTime,
                                                               final LocalDateTime endTime,
                                                               final Boolean activeOverride) {
        return HarvestPermitApplicationScheduleDTO.create(
                model().newHarvestPermitApplicationSchedule(
                        category,
                        toDateTimeNullSafe(beginTime),
                        toDateTimeNullSafe(endTime),
                        null,
                        null,
                        activeOverride));

    }
}
