package fi.riista.feature.huntingclub.permit.endofhunting;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.test.EmbeddedDatabaseTest;
import io.vavr.Tuple2;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.stream.Stream;

import static fi.riista.test.TestUtils.pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HuntingFinishingServiceTest extends EmbeddedDatabaseTest {

    private enum SummaryType {
        MOOSE, BASIC
    }

    @Resource
    private HuntingFinishingService service;

    @Test
    @Transactional
    public void testHasPermitPartnerFinishedHunting_forMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpeciesMoose();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            Stream.of(club1, club2, club3).forEach(permit.getPermitPartners()::add);

            final HuntingClubGroup club1Group = model().newHuntingClubGroup(club1, speciesAmount);
            final HuntingClubGroup club2Group = model().newHuntingClubGroup(club2, speciesAmount);
            final HuntingClubGroup club3Group = model().newHuntingClubGroup(club3, speciesAmount);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(permit, club1, true);
            model().newModeratedBasicHuntingSummary(speciesAmount, club2);
            model().newMooseHuntingSummary(permit, club3, false);

            persistInNewTransaction();

            assertTrue(service.hasPermitPartnerFinishedHunting(club1Group));
            assertTrue(service.hasPermitPartnerFinishedHunting(club2Group));
            assertFalse(service.hasPermitPartnerFinishedHunting(club3Group));
        });
    }

    @Test
    @Transactional
    public void testHasPermitPartnerFinishedHunting_forNonMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newDeerSubjectToClubHunting();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            Stream.of(club1, club2).forEach(permit.getPermitPartners()::add);

            final HuntingClubGroup club1Group = model().newHuntingClubGroup(club1, speciesAmount);
            final HuntingClubGroup club2Group = model().newHuntingClubGroup(club2, speciesAmount);

            model().newModeratedBasicHuntingSummary(speciesAmount, club1);
            model().newBasicHuntingSummary(speciesAmount, club2, false);

            persistInNewTransaction();

            assertTrue(service.hasPermitPartnerFinishedHunting(club1Group));
            assertFalse(service.hasPermitPartnerFinishedHunting(club2Group));
        });
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_withOnlyMooseSummaries_whenHuntingFinishedForAll() {
        testAllPartnersFinishedHunting(pair(SummaryType.MOOSE, true), pair(SummaryType.MOOSE, true));
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_withOnlyMooseSummaries_whenHuntingFinishedForSome() {
        testAllPartnersFinishedHunting(pair(SummaryType.MOOSE, true), pair(SummaryType.MOOSE, false));
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_withOnlyBasicSummaries_whenHuntingFinishedForAll() {
        testAllPartnersFinishedHunting(pair(SummaryType.BASIC, true), pair(SummaryType.BASIC, true));
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_withOnlyBasicSummaries_whenHuntingFinishedForSome() {
        testAllPartnersFinishedHunting(pair(SummaryType.BASIC, false), pair(SummaryType.BASIC, true));
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_withMixedSummaryTypes() {
        testAllPartnersFinishedHunting(pair(SummaryType.MOOSE, true), pair(SummaryType.BASIC, true));
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_whenNotAllSummariesAreAvailable() {
        testAllPartnersFinishedHunting(pair(SummaryType.MOOSE, true), null);
        testAllPartnersFinishedHunting(null, pair(SummaryType.BASIC, true));
    }

    private void testAllPartnersFinishedHunting(@Nullable final Tuple2<SummaryType, Boolean> summaryTupleForClub1,
                                                @Nullable final Tuple2<SummaryType, Boolean> summaryTupleForClub2) {
        withRhy(rhy -> {
            final GameSpecies species = model().newDeerSubjectToClubHunting();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            Stream.of(club1, club2).forEach(permit.getPermitPartners()::add);

            final SummaryType summaryTypeForClub1 = Optional.ofNullable(summaryTupleForClub1).map(Tuple2::_1).orElse(null);
            final SummaryType summaryTypeForClub2 = Optional.ofNullable(summaryTupleForClub2).map(Tuple2::_1).orElse(null);

            if (summaryTypeForClub1 == SummaryType.MOOSE || summaryTypeForClub2 == SummaryType.MOOSE) {
                species.setOfficialCode(GameSpecies.OFFICIAL_CODE_MOOSE);

                // Intermediate flush needed before persisting MooseHuntingSummary in order to have
                // harvest_permit_partners table populated mandated by foreign key constraint.
                persistInNewTransaction();
            }

            final boolean huntingFinishedForClub1 = Optional.ofNullable(summaryTupleForClub1).map(Tuple2::_2).orElse(false);
            final boolean huntingFinishedForClub2 = Optional.ofNullable(summaryTupleForClub2).map(Tuple2::_2).orElse(false);

            createHuntingSummary(club1, speciesAmount, summaryTypeForClub1, huntingFinishedForClub1);
            createHuntingSummary(club2, speciesAmount, summaryTypeForClub2, huntingFinishedForClub2);

            persistInNewTransaction();

            assertEquals(
                    huntingFinishedForClub1 && huntingFinishedForClub2,
                    service.allPartnersFinishedHunting(permit, species.getOfficialCode()));
        });
    }

    private void createHuntingSummary(final HuntingClub club, final HarvestPermitSpeciesAmount speciesAmount,
                                      final SummaryType summaryType, final boolean huntingFinished) {

        final boolean isMooseSpecies = speciesAmount.getGameSpecies().isMoose();

        if (summaryType == SummaryType.MOOSE) {

            // Check integrity of test parameters.
            assertTrue(isMooseSpecies);

            model().newMooseHuntingSummary(speciesAmount.getHarvestPermit(), club, huntingFinished);
        } else if (summaryType == SummaryType.BASIC) {

            // Check integrity of test parameters.
            assertTrue(huntingFinished || !isMooseSpecies);

            if (isMooseSpecies) {
                model().newModeratedBasicHuntingSummary(speciesAmount, club);
            } else {
                model().newBasicHuntingSummary(speciesAmount, club, huntingFinished);
            }
        }
    }

    @Test
    @Transactional
    public void testAllPartnersFinishedHunting_forBasicSummaryOverridingMooseSummary() {
        final GameSpecies species = model().newGameSpeciesMoose();

        final HarvestPermit permit = model().newHarvestPermit();
        final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

        final HuntingClub club = model().newHuntingClub(permit.getRhy());
        permit.getPermitPartners().add(club);

        // Intermediate flush needed before persisting MooseHuntingSummary in order to have
        // harvest_permit_partners table populated mandated by foreign key constraint.
        persistInNewTransaction();

        // huntingFinished differs between moose and basic summary, the latter must override.
        model().newMooseHuntingSummary(permit, club, false);
        model().newModeratedBasicHuntingSummary(speciesAmount, club);

        persistInNewTransaction();

        assertTrue(service.allPartnersFinishedHunting(permit, species.getOfficialCode()));
    }
}
