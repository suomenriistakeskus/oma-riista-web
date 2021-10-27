package fi.riista.feature.huntingclub.hunting.mobile;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestDTO;
import fi.riista.feature.huntingclub.hunting.mobile.MobileGroupHarvestDTOTransformer;
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

public class MobileGroupHarvestDTOTransformerTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MobileGroupHarvestDTOTransformer transformer;

    @Test
    public void testTransform() {
        withMooseHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, DateUtil.today());
            final Harvest harvest = model().newMobileHarvest(f.species, f.groupMember, huntingDay);

            onSavedAndAuthenticated(createUser(f.groupMember), () -> {
                final HarvestSpecVersion specVersion = HarvestSpecVersion.MOST_RECENT;
                final List<MobileGroupHarvestDTO> DTOs = transformer.apply(Collections.singletonList(harvest), specVersion);
                assertThat(DTOs, hasSize(1));

                final MobileGroupHarvestDTO harvestDTO = DTOs.get(0);
                assertThat(harvestDTO.getHuntingDayId(), is(equalTo(huntingDay.getId())));
                assertThat(harvestDTO.getAuthorInfo().getId(), is(equalTo(f.groupMember.getId())));
                assertThat(harvestDTO.getActorInfo().getId(), is(equalTo(f.groupMember.getId())));
            });
        });
    }
}
