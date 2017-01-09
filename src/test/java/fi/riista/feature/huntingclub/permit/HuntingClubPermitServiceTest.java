package fi.riista.feature.huntingclub.permit;

import static fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO.calculatePercentageBasedEffectiveArea;
import static fi.riista.util.Asserts.assertEqualMapsAfterValueTransformation;
import static fi.riista.util.TestUtils.pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.permit.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfo;
import fi.riista.feature.huntingclub.permit.summary.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.support.HuntingClubTestDataHelper;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple7;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class HuntingClubPermitServiceTest extends EmbeddedDatabaseTest {

    private enum SummaryType {
        MOOSE, BASIC
    }

    @Resource
    private HuntingClubPermitService service;

    private HuntingClubTestDataHelper testDataHelper = new HuntingClubTestDataHelper() {
        @Override
        protected EntitySupplier model() {
            return HuntingClubPermitServiceTest.this.model();
        }
    };

    @Test
    @Transactional
    public void testHasPartnerFinishedHunting_forMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpeciesMoose();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            final HuntingClub club3 = model().newHuntingClub(rhy);
            Stream.of(club1, club2, club3).forEach(permit.getPermitPartners()::add);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(permit, club1, true);
            model().newModeratedBasicHuntingSummary(speciesAmount, club2);
            model().newMooseHuntingSummary(permit, club3, false);

            persistInNewTransaction();

            final int speciesCode = species.getOfficialCode();
            assertTrue(service.hasPartnerFinishedHunting(permit, club1.getId(), speciesCode));
            assertTrue(service.hasPartnerFinishedHunting(permit, club2.getId(), speciesCode));
            assertFalse(service.hasPartnerFinishedHunting(permit, club3.getId(), speciesCode));
        });
    }

    @Test
    @Transactional
    public void testHasPartnerFinishedHunting_forNonMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE + 1);

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club1 = model().newHuntingClub(rhy);
            final HuntingClub club2 = model().newHuntingClub(rhy);
            Stream.of(club1, club2).forEach(permit.getPermitPartners()::add);

            model().newModeratedBasicHuntingSummary(speciesAmount, club1);
            model().newBasicHuntingSummary(speciesAmount, club2, false);

            persistInNewTransaction();

            final int speciesCode = species.getOfficialCode();
            assertTrue(service.hasPartnerFinishedHunting(permit, club1.getId(), speciesCode));
            assertFalse(service.hasPartnerFinishedHunting(permit, club2.getId(), speciesCode));
        });
    }

    @Test
    @Transactional
    public void testGetHarvestCountsGroupedByClubId() {
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
                    club3.getId(), club3Summary.getHarvestCounts(),
                    club4.getId(), club4HarvestCounts,
                    club5.getId(), HasHarvestCountsForPermit.zeros());

            final Map<Long, HuntingClubPermitCountDTO> result =
                    service.getHarvestCountsGroupedByClubId(permit, species.getOfficialCode());

            assertEquals(
                    Maps.transformValues(expected, HasHarvestCountsForPermit::asTuple),
                    Maps.transformValues(result, HasHarvestCountsForPermit::asTuple));
        });
    }

    private void createGroupAndHarvestsForClub(
            final HuntingClub club,
            final HarvestPermitSpeciesAmount speciesAmount,
            final HasHarvestCountsForPermit harvestCounts) {

        final HuntingClubGroup group = model().newHuntingClubGroup(club, speciesAmount.getGameSpecies());
        group.updateHarvestPermit(speciesAmount.getHarvestPermit());

        final Person author = model().newPerson();
        model().newOccupation(club, author, OccupationType.SEURAN_JASEN);
        model().newOccupation(group, author, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        testDataHelper.createHarvestsForHuntingGroup(group, author, harvestCounts);
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

    private void testAllPartnersFinishedHunting(
            @Nullable final Tuple2<SummaryType, Boolean> summaryTupleForClub1,
            @Nullable final Tuple2<SummaryType, Boolean> summaryTupleForClub2) {

        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies();

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

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_forMoose() {
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
            club1_mooseSummary.setEffectiveHuntingAreaPercentage(56.78f);

            final BasicClubHuntingSummary club2_basicSummary =
                    model().newModeratedBasicHuntingSummary(speciesAmount, club2);

            // Non-moderator-overridden basic summary should be ignored in case of moose species.
            model().newBasicHuntingSummary(speciesAmount, club3, true);

            // Creating basic summary for species not of interest.
            final HarvestPermitSpeciesAmount speciesAmount2 =
                    model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies());
            model().newModeratedBasicHuntingSummary(speciesAmount2, club4);

            // club5 does not have summary at all.

            persistInNewTransaction();

            final ClubHuntingSummaryBasicInfoDTO empty = new ClubHuntingSummaryBasicInfoDTO();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(
                            club1.getId(), createDTO(club1_mooseSummary.getBasicInfo(), false),
                            club2.getId(), createDTO(club2_basicSummary.getBasicInfo(), false),
                            club3.getId(), empty,
                            club4.getId(), empty,
                            club5.getId(), empty),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(permit, species.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_forNonMoose() {
        withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

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

            final ClubHuntingSummaryBasicInfoDTO empty = new ClubHuntingSummaryBasicInfoDTO();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(
                            club1.getId(), createDTO(club1Summary.getBasicInfo(), false),
                            club2.getId(), createDTO(club2Summary.getBasicInfo(), false),
                            club3.getId(), empty,
                            club4.getId(), empty),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(permit, species.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_resultForNonMooseNotAffectedByMooseSummaryWhenNoOtherSummaryPresent() {
        withMooseHuntingGroupFixture(f -> {

            final GameSpecies nonMoose = model().newGameSpecies();
            model().newHarvestPermitSpeciesAmount(f.permit, nonMoose);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            final ClubHuntingSummaryBasicInfoDTO empty = new ClubHuntingSummaryBasicInfoDTO();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(f.club.getId(), empty),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(f.permit, nonMoose.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_nonMooseSummaryNotAffectedByMooseSummaryOfOtherPermit() {
        withMooseHuntingGroupFixture(f -> {

            final GameSpecies nonMoose = model().newGameSpecies();
            final HarvestPermit nonMoosePermit = model().newHarvestPermit(f.rhy);
            final HarvestPermitSpeciesAmount nonMooseAmount =
                    model().newHarvestPermitSpeciesAmount(nonMoosePermit, nonMoose);

            nonMoosePermit.getPermitPartners().add(f.club);

            final BasicClubHuntingSummary nonMooseSummary =
                    model().newBasicHuntingSummary(nonMooseAmount, f.club, true);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(f.club.getId(), createDTO(nonMooseSummary.getBasicInfo(), false)),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(nonMoosePermit, nonMoose.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_checkModeratedBasicSummaryOverridesMooseSummary() {
        withMooseHuntingGroupFixture(f -> {

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated (mandated by foreign key constraint).
            persistInNewTransaction();

            model().newMooseHuntingSummary(f.permit, f.club, true);
            final BasicClubHuntingSummary basicSummary =
                    model().newModeratedBasicHuntingSummary(f.speciesAmount, f.club);

            persistInNewTransaction();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(f.club.getId(), createDTO(basicSummary.getBasicInfo(), false)),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(f.permit, f.species.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_forMooseWhenMooseDataCardGroupPresent() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            // Intermediate flush needed before persisting MooseHuntingSummary in order to have
            // harvest_permit_partners table populated mandated by foreign key constraint.
            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            persistInNewTransaction();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(f.club.getId(), createDTO(summary.getBasicInfo(), true)),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(f.permit, f.species.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    @Test
    @Transactional
    public void testGetHuntingSummaryBasicInfoGroupedByClubId_forNonMooseWhenMooseDataCardGroupPresent() {
        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(true);

            final GameSpecies nonMoose = model().newGameSpecies();
            final HarvestPermitSpeciesAmount nonMooseAmount = model().newHarvestPermitSpeciesAmount(f.permit, nonMoose);
            final BasicClubHuntingSummary nonMooseSummary =
                    model().newBasicHuntingSummary(nonMooseAmount, f.club, true);

            persistInNewTransaction();

            assertEqualMapsAfterValueTransformation(
                    ImmutableMap.of(f.club.getId(), createDTO(nonMooseSummary.getBasicInfo(), false)),
                    service.getHuntingSummaryBasicInfoGroupedByClubId(f.permit, nonMoose.getOfficialCode()),
                    HuntingClubPermitServiceTest::toTuple);
        });
    }

    private static ClubHuntingSummaryBasicInfoDTO createDTO(@Nonnull final ClubHuntingSummaryBasicInfo obj,
                                                            final boolean fromMooseDataCard) {
        Objects.requireNonNull(obj);

        final ClubHuntingSummaryBasicInfoDTO dto = new ClubHuntingSummaryBasicInfoDTO();

        dto.setClubId(obj.getClubId());
        dto.setGameSpeciesCode(obj.getGameSpeciesCode());

        dto.setHuntingFinished(obj.isHuntingFinished());
        dto.setHuntingEndDate(obj.getHuntingEndDate());
        dto.setFromMooseDataCard(fromMooseDataCard);

        final Integer totalArea = obj.getTotalHuntingArea();
        dto.setTotalHuntingArea(totalArea);

        dto.setEffectiveHuntingArea(Optional
                .ofNullable(obj.getEffectiveHuntingArea())
                .orElseGet(() -> {
                    return calculatePercentageBasedEffectiveArea(totalArea, obj.getEffectiveHuntingAreaPercentage());
                }));

        dto.setRemainingPopulationInTotalArea(obj.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(obj.getRemainingPopulationInEffectiveArea());

        return dto;
    }

    private static Tuple7<Boolean, LocalDate, Boolean, Integer, Integer, Integer, Integer> toTuple(
            final ClubHuntingSummaryBasicInfoDTO dto) {

        return Tuple.of(dto.isHuntingFinished(), dto.getHuntingEndDate(), dto.isFromMooseDataCard(),
                dto.getTotalHuntingArea(), dto.getEffectiveHuntingArea(), dto.getRemainingPopulationInTotalArea(),
                dto.getRemainingPopulationInEffectiveArea());
    }

}
