package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MobileGroupHuntingDiaryFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MobileGroupHuntingDiaryFeature feature;

    @Test
    public void testGetDiaryOfGroupMembers() {
        withMooseHuntingGroupFixture(f -> {
            final LocalDate today = DateUtil.today();
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today);

            model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            final Harvest rejectedHarvest = model().newMobileHarvest(f.species, f.groupMember, today);
            model().newHarvestRejection(f.group, rejectedHarvest);

            model().newObservation(f.species, f.groupMember, huntingDay);
            final ObservationSpecVersion specVersion = ObservationSpecVersion.MOST_RECENT;
            model().newObservationBaseFields(f.species, specVersion);

            final Observation rejectedObservation = model().newObservation(f.species, f.groupMember, today);
            model().newObservationRejection(f.group, rejectedObservation);

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                final MobileGroupHuntingDiaryDTO diary = feature.getDiaryOfGroupMembers(f.group.getId());

                final List<MobileGroupHarvestDTO> harvests = diary.getHarvests();
                assertThat(harvests, hasSize(1));

                final MobileGroupHarvestDTO harvest = harvests.get(0);
                assertThat(harvest.getType(), is(equalTo(GameDiaryEntryType.HARVEST)));
                assertThat(harvest.getHuntingDayId(), is(equalTo(huntingDay.getId())));

                final List<MobileGroupHarvestDTO> rejectedHarvests = diary.getRejectedHarvests();
                assertThat(rejectedHarvests, hasSize(1));
                assertThat(rejectedHarvests.get(0).getId(), is(equalTo(rejectedHarvest.getId())));

                final List<MobileGroupObservationDTO> observations = diary.getObservations();
                assertThat(observations, hasSize(1));

                final MobileGroupObservationDTO observation = observations.get(0);
                assertThat(observation.getType(), is(equalTo(GameDiaryEntryType.OBSERVATION)));
                assertThat(observation.getHuntingDayId(), is(equalTo(huntingDay.getId())));

                final List<MobileGroupObservationDTO> rejectedObservations = diary.getRejectedObservations();
                assertThat(rejectedObservations, hasSize(1));
                assertThat(rejectedObservations.get(0).getId(), is(equalTo(rejectedObservation.getId())));
            });
        });

    }
}
