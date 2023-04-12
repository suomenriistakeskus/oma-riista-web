package fi.riista.feature.permit.application.schedule;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DtoUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.toDateTimeNullSafe;
import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class HarvestPermitApplicationScheduleCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitApplicationScheduleCrudFeature feature;

    @Resource
    private HarvestPermitApplicationScheduleRepository repository;

    private HarvestPermitApplicationSchedule schedule;

    @Before
    public void setup() {
        schedule = model().newHarvestPermitApplicationSchedule(HarvestPermitCategory.MOOSELIKE,
                new DateTime(2021, 10, 9, 0, 0, Constants.DEFAULT_TIMEZONE),
                new DateTime(2022, 1, 15, 0, 0, Constants.DEFAULT_TIMEZONE),
                "Ohjeet",
                "Anvisning",
                null);
    }

    @Test
    public void testList() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            final List<HarvestPermitApplicationScheduleDTO> list = feature.list();
            assertThat(list, hasSize(1));

            final HarvestPermitApplicationScheduleDTO dto = list.get(0);
            assertThat(dto.getCategory(), is(equalTo(schedule.getCategory())));
            assertThat(toDateTimeNullSafe(dto.getBeginTime()), is(equalTo(schedule.getBeginTime())));
            assertThat(toDateTimeNullSafe(dto.getEndTime()), is(equalTo(schedule.getEndTime())));

            final Map<String, String> instructions = dto.getInstructions();
            assertThat(instructions, is(notNullValue()));

            final LocalisedString localisedInstructions = LocalisedString.fromMap(instructions);
            assertThat(localisedInstructions.getFinnish(), is(equalTo(schedule.getInstructionsFi())));
            assertThat(localisedInstructions.getSwedish(), is(equalTo(schedule.getInstructionsSv())));

            assertThat(dto.getActiveOverride(), is(nullValue()));
        });
    }

    @Test
    public void testUpdate() {
        onSavedAndAuthenticated(createNewModerator(SystemUserPrivilege.MODERATE_APPLICATION_SCHEDULE), () -> {
            final HarvestPermitApplicationScheduleDTO updateDTO = new HarvestPermitApplicationScheduleDTO();
            DtoUtil.copyBaseFields(schedule, updateDTO);

            // Category should not update
            updateDTO.setCategory(HarvestPermitCategory.BIRD);

            updateDTO.setBeginTime(toLocalDateTimeNullSafe(schedule.getBeginTime().plusDays(1)));
            updateDTO.setEndTime(toLocalDateTimeNullSafe(schedule.getEndTime().plusDays(1)));
            updateDTO.setInstructions(LocalisedString.of(
                    schedule.getInstructionsFi() + "_modified",
                            schedule.getInstructionsSv() + "_modified")
                    .asMap());

            feature.update(updateDTO);

            runInTransaction(() -> {
                final HarvestPermitApplicationSchedule updatedSchedule = repository.getOne(schedule.getId());
                assertThat(updatedSchedule, is(not(nullValue())));

                assertThat(updatedSchedule.getCategory(), is(equalTo(schedule.getCategory())));

                assertThat(updatedSchedule.getBeginTime(), is(equalTo(toDateTimeNullSafe(updateDTO.getBeginTime()))));
                assertThat(updatedSchedule.getEndTime(), is(equalTo(toDateTimeNullSafe(updateDTO.getEndTime()))));

                final String finnish = updatedSchedule.getInstructionsFi();
                assertThat(finnish, is(not(nullValue())));
                final String swedish = updatedSchedule.getInstructionsSv();
                assertThat(swedish, is(not(nullValue())));
                assertThat(LocalisedString.of(finnish, swedish), is(equalTo(LocalisedString.fromMap(updateDTO.getInstructions()))));

                assertThat(updatedSchedule.getActiveOverride(), is(equalTo(updateDTO.getActiveOverride())));
            });
        });
    }
}
