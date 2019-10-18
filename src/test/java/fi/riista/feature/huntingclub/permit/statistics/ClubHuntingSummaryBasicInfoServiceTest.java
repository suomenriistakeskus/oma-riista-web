package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.test.EmbeddedDatabaseTest;
import io.vavr.Tuple;
import io.vavr.Tuple7;
import io.vavr.collection.HashMap;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO.calculatePercentageBasedEffectiveArea;
import static org.junit.Assert.assertEquals;

public class ClubHuntingSummaryBasicInfoServiceTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private ClubHuntingSummaryBasicInfoService service;

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_forMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpeciesMoose();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            final HuntingClub club4 = model().newHuntingClub(rhy);
            final HuntingClub club5 = model().newHuntingClub(rhy);
            Stream.of(club1, club2, club3, club4, club5).forEach(permit.getPermitPartners()::add);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            final MooseHuntingSummary club1_mooseSummary = model().newMooseHuntingSummary(permit, club1, true);
            // Add effective area as percentage share to extend test coverage.
            club1_mooseSummary.getAreaSizeAndPopulation().setEffectiveHuntingArea(null);
            club1_mooseSummary.setEffectiveHuntingAreaPercentage(56.78);

            final BasicClubHuntingSummary club2_basicSummary =
                    model().newModeratedBasicHuntingSummary(speciesAmount, club2);

            // Non-moderator-overridden basic summary should be ignored in case of moose species.
            model().newBasicHuntingSummary(speciesAmount, club3, true);

            // Creating basic summary for species not of interest.
            final HarvestPermitSpeciesAmount speciesAmount2 =
                    model().newHarvestPermitSpeciesAmount(permit, model().newDeerSubjectToClubHunting());
            model().newModeratedBasicHuntingSummary(speciesAmount2, club4);

            // club5 does not have summary at all.

            persistInNewTransaction();

            assertEquals(HashMap.of(
                    club1.getId(), toTuple(club1_mooseSummary),
                    club2.getId(), toTuple(club2_basicSummary),
                    club3.getId(), emptyTuple(),
                    club4.getId(), emptyTuple(),
                    club5.getId(), emptyTuple()),
                    toTuple(service.getHuntingSummariesGroupedByClubId(permit, species.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_forNonMoose() {
        withRhy(rhy -> {
            final GameSpecies deer = model().newDeerSubjectToClubHunting();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, deer);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            final HuntingClub club4 = model().newHuntingClub(rhy);
            Stream.of(club1, club2, club3, club4).forEach(permit.getPermitPartners()::add);

            final BasicClubHuntingSummary club1Summary = model().newModeratedBasicHuntingSummary(speciesAmount, club1);
            final BasicClubHuntingSummary club2Summary = model().newBasicHuntingSummary(speciesAmount, club2, true);

            // Creating basic summary for species not of interest.
            final HarvestPermitSpeciesAmount speciesAmount2 =
                    model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies());
            model().newModeratedBasicHuntingSummary(speciesAmount2, club3);

            // club4 does not have summary at all.

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(
                            club1.getId(), toTuple(club1Summary),
                            club2.getId(), toTuple(club2Summary),
                            club3.getId(), emptyTuple(),
                            club4.getId(), emptyTuple()),
                    toTuple(service.getHuntingSummariesGroupedByClubId(permit, deer.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_resultForNonMooseNotAffectedByMooseSummaryWhenNoOtherSummaryPresent() {
        withMooseHuntingGroupFixture(f -> {

            final GameSpecies deer = model().newDeerSubjectToClubHunting();
            model().newHarvestPermitSpeciesAmount(f.permit, deer);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(f.club.getId(), emptyTuple()),
                    toTuple(service.getHuntingSummariesGroupedByClubId(f.permit, deer.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_nonMooseSummaryNotAffectedByMooseSummaryOfOtherPermit() {
        withMooseHuntingGroupFixture(f -> {

            final GameSpecies deer = model().newDeerSubjectToClubHunting();
            final HarvestPermit deerPermit = model().newHarvestPermit(f.rhy);
            final HarvestPermitSpeciesAmount deerAmount =
                    model().newHarvestPermitSpeciesAmount(deerPermit, deer);

            deerPermit.getPermitPartners().add(f.club);

            final BasicClubHuntingSummary deerSummary =
                    model().newBasicHuntingSummary(deerAmount, f.club, true);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(f.club.getId(), toTuple(deerSummary)),
                    toTuple(service.getHuntingSummariesGroupedByClubId(deerPermit, deer.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_checkModeratedBasicSummaryOverridesMooseSummary() {
        withMooseHuntingGroupFixture(f -> {

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated (mandated by foreign key constraint).
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);
            final BasicClubHuntingSummary basicSummary =
                    model().newModeratedBasicHuntingSummary(f.speciesAmount, f.club);

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(f.club.getId(), toTuple(basicSummary)),
                    toTuple(service.getHuntingSummariesGroupedByClubId(f.permit, f.species.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_forMooseWhenMooseDataCardGroupPresent() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(f.club.getId(), toTuple(summary, true)),
                    toTuple(service.getHuntingSummariesGroupedByClubId(f.permit, f.species.getOfficialCode())));
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummariesGroupedByClubId_forNonMooseWhenMooseDataCardGroupPresent() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final GameSpecies deer = model().newDeerSubjectToClubHunting();
            final HarvestPermitSpeciesAmount deerAmount = model().newHarvestPermitSpeciesAmount(f.permit, deer);
            final BasicClubHuntingSummary deerSummary = model().newBasicHuntingSummary(deerAmount, f.club, true);

            persistInNewTransaction();

            assertEquals(
                    HashMap.of(f.club.getId(), toTuple(deerSummary)),
                    toTuple(service.getHuntingSummariesGroupedByClubId(f.permit, deer.getOfficialCode())));
        });
    }

    private static HashMap<Long, Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer>> toTuple(
            final Map<Long, ClubHuntingSummaryBasicInfoDTO> dtos) {
        return HashMap.ofAll(dtos).mapValues(dto -> Tuple.of(
                dto.isHuntingFinished(), dto.getHuntingEndDate(),
                dto.isFromMooseDataCard(),
                dto.getTotalHuntingArea(),
                dto.getEffectiveHuntingArea(),
                dto.getRemainingPopulationInTotalArea(),
                dto.getRemainingPopulationInEffectiveArea()));
    }

    private static Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer> toTuple(final MooseHuntingSummary summary) {
        return toTuple(summary, false);
    }

    private static Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer> toTuple(final MooseHuntingSummary summary,
                                                                                                   final boolean fromMooseDataCard) {
        final Integer totalHuntingArea = summary.getAreaSizeAndPopulation().getTotalHuntingArea();
        final Integer effectiveHuntingArea = summary.getAreaSizeAndPopulation().getEffectiveHuntingArea();

        final Integer expectedEffectiveHuntingArea = effectiveHuntingArea != null
                ? effectiveHuntingArea
                : calculatePercentageBasedEffectiveArea(totalHuntingArea, summary.getEffectiveHuntingAreaPercentage());

        return Tuple.of(summary.isHuntingFinished(), summary.getHuntingEndDate(), fromMooseDataCard,
                totalHuntingArea,
                expectedEffectiveHuntingArea,
                summary.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea(),
                summary.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea());
    }

    private static Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer> toTuple(final BasicClubHuntingSummary summary) {
        return Tuple.of(summary.isHuntingFinished(), summary.getHuntingEndDate(), false,
                summary.getAreaSizeAndPopulation().getTotalHuntingArea(),
                summary.getAreaSizeAndPopulation().getEffectiveHuntingArea(),
                summary.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea(),
                summary.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea());
    }

    private static Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer> emptyTuple() {
        return Tuple.of(false, null, false, null, null, null, null);
    }
}
