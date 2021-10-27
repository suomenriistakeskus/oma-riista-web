package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupObservationDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupObservationDTOTransformer;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MobileGroupObservationDTOTransformerTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MobileGroupObservationDTOTransformer transformer;

    @Test
    public void testTransform() {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());

            final Observation observation = model().newObservation(f.species, f.groupMember, huntingDay);

            final ObservationSpecVersion specVersion = ObservationSpecVersion.MOST_RECENT;
            model().newObservationBaseFields(f.species, specVersion);

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                final List<MobileGroupObservationDTO> DTOs = transformer.apply(Collections.singletonList(observation), specVersion);
                assertThat(DTOs, hasSize(1));

                final MobileGroupObservationDTO observationDTO = DTOs.get(0);
                assertThat(observationDTO.getHuntingDayId(), is(equalTo(huntingDay.getId())));
                assertThat(observationDTO.getAuthorInfo().getId(), is(equalTo(f.groupMember.getId())));
                assertThat(observationDTO.getActorInfo().getId(), is(equalTo(f.groupMember.getId())));
            });
        });
    }
}
