package fi.riista.feature.huntingclub.permit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountService;
import fi.riista.feature.huntingclub.support.HuntingClubTestDataHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static org.junit.Assert.assertEquals;

public class HarvestCountServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestCountService service;

    private HuntingClubTestDataHelper testDataHelper = new HuntingClubTestDataHelper() {
        @Override
        protected EntitySupplier model() {
            return HarvestCountServiceTest.this.model();
        }
    };

    @Test
    @Transactional
    public void testCountHarvestsGroupingByClubId() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            final HuntingClub club4 = model().newHuntingClub(rhy);
            final HuntingClub club5 = model().newHuntingClub(rhy);
            Stream.of(club1, club2, club3, club4, club5).forEach(permit.getPermitPartners()::add);

            // Persist in between to have IDs resolved for club entities.
            persistInNewTransaction();

            final HasHarvestCountsForPermit club1HarvestCounts = testDataHelper.generateHarvestCounts();
            createGroupAndHarvestsForClub(club1, speciesAmount, club1HarvestCounts);

            final HasHarvestCountsForPermit club2HarvestCounts = testDataHelper.generateHarvestCounts();
            createGroupAndHarvestsForClub(club2, speciesAmount, club2HarvestCounts);
            model().newBasicHuntingSummary(speciesAmount, club2, true);

            // Harvest amounts overridden by moderator for club3.
            createGroupAndHarvestsForClub(club3, speciesAmount, testDataHelper.generateHarvestCounts());
            final BasicClubHuntingSummary club3Summary = model().newModeratedBasicHuntingSummary(speciesAmount, club3);

            final HasHarvestCountsForPermit club4HarvestCounts = testDataHelper.generateHarvestCounts();
            createGroupAndHarvestsForClub(club4, speciesAmount, club4HarvestCounts);

            // Create harvests for another species (should not affect the result).
            final GameSpecies anotherSpecies = model().newGameSpecies();
            final HarvestPermitSpeciesAmount anotherSpeciesAmount =
                    model().newHarvestPermitSpeciesAmount(permit, anotherSpecies);
            createGroupAndHarvestsForClub(club4, anotherSpeciesAmount, testDataHelper.generateHarvestCounts());
            createGroupAndHarvestsForClub(club5, anotherSpeciesAmount, testDataHelper.generateHarvestCounts());

            persistInNewTransaction();

            final Map<Long, HasHarvestCountsForPermit> expected = ImmutableMap.of(
                    club1.getId(), club1HarvestCounts,
                    club2.getId(), club2HarvestCounts,
                    club3.getId(), club3Summary.getModeratedHarvestCounts(),
                    club4.getId(), club4HarvestCounts,
                    club5.getId(), HarvestCountDTO.ZEROS);

            final Map<Long, HarvestCountDTO> result =
                    service.countHarvestsGroupingByClubId(permit, species.getOfficialCode());

            assertEquals(
                    Maps.transformValues(expected, HasHarvestCountsForPermit::asTuple),
                    Maps.transformValues(result, HasHarvestCountsForPermit::asTuple));
        });
    }

    private void createGroupAndHarvestsForClub(final HuntingClub club,
                                               final HarvestPermitSpeciesAmount speciesAmount,
                                               final HasHarvestCountsForPermit harvestCounts) {

        final HuntingClubGroup group = model().newHuntingClubGroup(club, speciesAmount);

        final Person author = model().newPerson();
        model().newOccupation(club, author, SEURAN_JASEN);
        model().newOccupation(group, author, RYHMAN_METSASTYKSENJOHTAJA);

        testDataHelper.createHarvestsForHuntingGroup(group, author, harvestCounts);
    }
}
