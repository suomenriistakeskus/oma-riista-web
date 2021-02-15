package fi.riista.integration.luke;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestSpecimenType;
import fi.riista.feature.gamediary.fixture.ObservationFixtureMixin;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.integration.luke_export.deerharvests.LED_Amount;
import fi.riista.integration.luke_export.deerharvests.LED_Club;
import fi.riista.integration.luke_export.deerharvests.LED_FemaleAndCalfs;
import fi.riista.integration.luke_export.deerharvests.LED_GeoLocation;
import fi.riista.integration.luke_export.deerharvests.LED_Group;
import fi.riista.integration.luke_export.deerharvests.LED_Harvest;
import fi.riista.integration.luke_export.deerharvests.LED_HuntingSummary;
import fi.riista.integration.luke_export.deerharvests.LED_Observation;
import fi.riista.integration.luke_export.deerharvests.LED_Overrides;
import fi.riista.integration.luke_export.deerharvests.LED_Permit;
import fi.riista.integration.luke_export.deerharvests.LED_Permits;
import fi.riista.integration.luke_export.deerharvests.LED_RestrictionType;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.DeerHuntingType.OTHER;
import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationType.NAKO;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@RunWith(Theories.class)
public class LukeExportWhiteTailedDeerFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private static final Logger LOG = LoggerFactory.getLogger(LukeExportWhiteTailedDeerFeatureTest.class);

    private static final int MAX_QUERY_COUNT_WITHOUT_BATCHES = 11;
    private static final int MAX_QUERY_COUNT_WITH_ONE_BATCH = MAX_QUERY_COUNT_WITHOUT_BATCHES + 2; // One harvest and one observation query

    @Resource
    private LukeExportWhiteTailedDeerFeature feature;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    private SystemUser apiUser;

    @Before
    public void setUp() {
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_MOOSE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccess_denied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportDeer(huntingYear()));
    }

    @Test
    public void testAccess_granted() {
        model().newGameSpeciesWhiteTailedDeer();
        onSavedAndAuthenticated(apiUser, () -> Asserts.assertEmpty(feature.exportDeer(huntingYear()).getPermits()));
    }

    @Theory
    public void testValidity_harvest(HarvestSpecimenType harvestSpecimenType) {
        withDeerHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            huntingDay.setStartTime(LocalTime.MIDNIGHT);
            final Harvest expectedHarvest = createHarvest(f, huntingDay, harvestSpecimenType);

            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    final LED_Group group = getOnlyGroup(permits);
                    assertThat(group.getHarvests(), hasSize(1));
                    final LED_Harvest harvest = group.getHarvests().get(0);

                    assertGeoLocation(expectedHarvest.getGeoLocation(), harvest.getGeoLocation());
                    assertDateEquals(expectedHarvest.getPointOfTime(), harvest.getPointOfTime());
                    assertDeerHuntingTypeEquals(expectedHarvest.getDeerHuntingType(), harvest.getHuntingType().getHuntingType());
                    assertThat(harvest.getHuntingType().getHuntingTypeDescription(), equalTo(expectedHarvest.getDeerHuntingOtherTypeDescription()));

                    final HarvestSpecimen expectedSpecimen = expectedHarvest.getSortedSpecimens().get(0);
                    assertEnumEquals(expectedSpecimen.getGender(), harvest.getGender());
                    assertEnumEquals(expectedSpecimen.getAge(), harvest.getAge());
                    assertThat(harvest.getWeightEstimated(), equalTo(expectedSpecimen.getWeightEstimated()));
                    assertThat(harvest.getWeightMeasured(), equalTo(expectedSpecimen.getWeightMeasured()));
                    assertThat(harvest.isAntlersLost(), equalTo(expectedSpecimen.getAntlersLost()));
                    assertEnumEquals(expectedSpecimen.getAntlersType(), harvest.getAntlersType());
                    assertThat(harvest.getAntlersWidth(), equalTo(expectedSpecimen.getAntlersWidth()));
                    assertThat(harvest.getAntlerPointsLeft(), equalTo(expectedSpecimen.getAntlerPointsLeft()));
                    assertThat(harvest.getAntlerPointsRight(), equalTo(expectedSpecimen.getAntlerPointsRight()));
                    assertThat(harvest.getAntlersGirth(), equalTo(expectedSpecimen.getAntlersGirth()));
                    assertThat(harvest.getAntlersLength(), equalTo(expectedSpecimen.getAntlersLength()));
                    assertThat(harvest.getAntlersInnerWidth(), equalTo(expectedSpecimen.getAntlersInnerWidth()));
                    assertThat(harvest.isNotEdible(), equalTo(expectedSpecimen.getNotEdible()));
                    assertThat(harvest.getAdditionalInfo(), equalTo(expectedSpecimen.getAdditionalInfo()));
                });
            });
        });
    }

    @Test
    public void testValidity_observation() {
        withDeerHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            huntingDay.setStartTime(LocalTime.MIDNIGHT);
            final Observation expected = createObservations(f, huntingDay, 1).get(0);

            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    final LED_Group group = getOnlyGroup(permits);
                    assertThat(group.getObservations(), hasSize(1));
                    final LED_Observation observation = group.getObservations().get(0);

                    assertGeoLocation(expected.getGeoLocation(), observation.getGeoLocation());
                    assertDateEquals(expected.getPointOfTime(), observation.getPointOfTime());
                    assertDeerHuntingTypeEquals(expected.getDeerHuntingType(), observation.getHuntingType().getHuntingType());
                    assertThat(observation.getHuntingType().getHuntingTypeDescription(), equalTo(expected.getDeerHuntingTypeDescription()));
                    assertEnumEquals(expected.getObservationType(), observation.getObservationType());
                    final GameSpecies expectedSpecies = expected.getSpecies();
                    assertThat(observation.getGameSpeciesCode(), equalTo(expectedSpecies.getOfficialCode()));
                    assertThat(observation.getGameSpeciesNameFinnish(), equalTo(expectedSpecies.getNameFinnish()));
                    assertThat(observation.getMooselikeMaleAmount(), equalTo(expected.getMooselikeMaleAmount()));
                    assertThat(observation.getMooselikeUnknownSpecimenAmount(), equalTo(expected.getMooselikeUnknownSpecimenAmount()));
                    assertThat(observation.getMooselikeSolitaryCalfAmount(), equalTo(expected.getMooselikeCalfAmount()));
                    assertThat(getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 0), equalTo(expected.getMooselikeFemaleAmount()));
                    assertThat(getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 1), equalTo(expected.getMooselikeFemale1CalfAmount()));
                    assertThat(getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 2), equalTo(expected.getMooselikeFemale2CalfsAmount()));
                    assertThat(getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 3), equalTo(expected.getMooselikeFemale3CalfsAmount()));
                    assertThat(getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 4), equalTo(expected.getMooselikeFemale4CalfsAmount()));
                });
            });
        });
    }

    @Test
    public void testValidity_permit() {
        withDeerHuntingGroupFixture(f -> {
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {

                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    assertThat(permits.getPermits(), hasSize(1));
                    final LED_Permit permit = permits.getPermits().get(0);
                    assertThat(permit.getPermitNumber(), equalTo(f.permit.getPermitNumber()));
                    assertThat(permit.getRhyOfficialCode(), equalTo(f.permit.getRhy().getOfficialCode()));

                    final LED_Amount amount = permit.getAmount();
                    assertThat(amount.getAmount(), equalTo(f.speciesAmount.getSpecimenAmount()));
                    assertThat(amount.getRestriction(), equalTo(f.speciesAmount.getRestrictionType()));
                    assertThat(amount.getRestrictedAmount(), equalTo(f.speciesAmount.getRestrictionAmount()));

                    assertThat(permit.getHuntingClubs(), hasSize(1));
                    final LED_Club club = permit.getHuntingClubs().get(0);
                    assertThat(club.getClubOfficialCode(), equalTo(f.club.getOfficialCode()));
                    assertThat(club.getNameFinnish(), equalTo(f.club.getNameFinnish()));
                    assertThat(club.getGeoLocation(), equalTo(f.club.getGeoLocation()));
                    assertGeoLocation(f.club.getGeoLocation(), club.getGeoLocation());
                    assertThat(club.getRhyOfficialCode(), equalTo(f.rhy.getOfficialCode()));

                    assertThat(club.getGroups(), hasSize(1));
                    final LED_Group group = club.getGroups().get((0));
                    assertThat(group.getNameFinnish(), equalTo(f.group.getNameFinnish()));
                    assertThat(group.getDataSource().name(), equalTo("WEB"));

                });
            });
        });
    }

    @Test
    public void testValidity_amendmentPermits() {
        withDeerHuntingGroupFixture(f -> {
            final HarvestPermit amendmentPermit = model().newHarvestPermit(f.permit);
            amendmentPermit.setPermitTypeCode(PermitTypeCode.MOOSELIKE_AMENDMENT);
            final HarvestPermitSpeciesAmount amount = model().newHarvestPermitSpeciesAmount(amendmentPermit, f.species, 1f);
            amount.setRestrictionType(HarvestPermitSpeciesAmount.RestrictionType.AE);
            amount.setRestrictionAmount(0.5f);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {

                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));
                    assertThat(permits.getPermits(), hasSize(1));

                    final List<LED_Amount> amendmentPermits = permits.getPermits().get(0).getAmendmentPermits();
                    assertThat(amendmentPermits, hasSize(1));
                    assertThat(amendmentPermits.get(0).getAmount(), equalTo(1.0f));
                    assertThat(amendmentPermits.get(0).getRestriction(), equalTo(LED_RestrictionType.AE));
                    assertThat(amendmentPermits.get(0).getRestrictedAmount(), equalTo(0.5f));

                });
            });
        });
    }

    @Test
    public void testValidity_huntingSummary() {
        withDeerHuntingGroupFixture(f -> {
            final BasicClubHuntingSummary expected = model().newBasicHuntingSummary(f.speciesAmount, f.club, true);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {

                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    assertThat(permits.getPermits(), hasSize(1));
                    assertThat(permits.getPermits().get(0).getHuntingClubs(), hasSize(1));
                    final LED_HuntingSummary summary = permits.getPermits().get(0).getHuntingClubs().get(0).getHuntingSummary();
                    assertThat(summary, is(notNullValue()));
                    assertThat(summary.getHuntingEndDate(), equalTo(expected.getHuntingEndDate()));
                    assertThat(summary.isHuntingFinished(), is(expected.isHuntingFinished()));
                    assertThat(summary.getTotalHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getTotalHuntingArea()));
                    assertThat(summary.getEffectiveHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getEffectiveHuntingArea()));
                    assertThat(summary.getPopulationRemainingInTotalHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea()));
                    assertThat(summary.getPopulationRemainingInEffectiveHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea()));
                });
            });
        });
    }

    @Test
    public void testValidity_overrides() {
        withDeerHuntingGroupFixture(f -> {
            final BasicClubHuntingSummary expected = model().newModeratedBasicHuntingSummary(f.speciesAmount, f.club);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {

                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    assertThat(permits.getPermits(), hasSize(1));
                    assertThat(permits.getPermits().get(0).getHuntingClubs(), hasSize(1));
                    final LED_Overrides override = permits.getPermits().get(0).getHuntingClubs().get(0).getOverrides();
                    assertThat(override, is(notNullValue()));
                    assertThat(override.getAdultMales(), equalTo(expected.getModeratedHarvestCounts().getNumberOfAdultMales()));
                    assertThat(override.getAdultFemales(), equalTo(expected.getModeratedHarvestCounts().getNumberOfAdultFemales()));
                    assertThat(override.getYoungMales(), equalTo(expected.getModeratedHarvestCounts().getNumberOfYoungMales()));
                    assertThat(override.getYoungFemales(), equalTo(expected.getModeratedHarvestCounts().getNumberOfYoungFemales()));
                    assertThat(override.getNonEdibleAdults(), equalTo(expected.getModeratedHarvestCounts().getNumberOfNonEdibleAdults()));
                    assertThat(override.getNonEdibleYoung(), equalTo(expected.getModeratedHarvestCounts().getNumberOfNonEdibleYoungs()));
                    assertThat(override.getTotalHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getTotalHuntingArea()));
                    assertThat(override.getEffectiveHuntingArea(), equalTo(expected.getAreaSizeAndPopulation().getEffectiveHuntingArea()));
                    assertThat(override.getRemainingPopulationInTotalArea(), equalTo(expected.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea()));
                    assertThat(override.getRemainingPopulationInEffectiveArea(), equalTo(expected.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea()));

                });
            });
        });
    }

    @Test
    public void testValidity_noHarvestsIfOverrides() {
        withDeerHuntingGroupFixture(f -> {
            createHuntingDaysWithEntries(f,1, 10, 10);
            model().newModeratedBasicHuntingSummary(f.speciesAmount, f.club);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {

                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));
                    assertThat(permits.getPermits(), hasSize(1));
                    assertThat(permits.getPermits().get(0).getHuntingClubs(), hasSize(1));
                    final LED_Overrides override = permits.getPermits().get(0).getHuntingClubs().get(0).getOverrides();
                    assertThat(override, is(notNullValue()));
                    assertThat(permits.getPermits().get(0).getHuntingClubs().get(0).getGroups(), hasSize(1));
                    final LED_Group group = permits.getPermits().get(0).getHuntingClubs().get(0).getGroups().get(0);
                    assertThat(group.getHarvests(), hasSize(0));
                    assertThat(group.getObservations(), hasSize(10));

                });
            });
        });
    }

    @Test
    public void testValidity_huntingGroupsAreListedOnlyWithinItsClub() {
        withDeerHuntingGroupFixture(f -> {
            withHuntingGroupFixture(f.species, partnerGroup -> {
                f.permit.getPermitPartners().add(partnerGroup.club);
                partnerGroup.group.updateHarvestPermit(f.permit);

                onSavedAndAuthenticated(apiUser, () -> {
                    assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                        final LED_Permits permits = feature.exportDeer(huntingYear());
                        //System.out.println(asJson(permits));

                        permits.getPermits().forEach(permit -> {
                            if (permit.getPermitNumber().equals(f.permit.getPermitNumber())) {
                                assertThat(permit.getHuntingClubs(), hasSize(2));

                                permit.getHuntingClubs().forEach(club -> {
                                    assertThat(club.getGroups(), hasSize(1));

                                    if (club.getClubOfficialCode().equals(f.club.getOfficialCode())) {
                                        assertThat(club.getGroups().get(0).getNameFinnish(), equalTo(f.group.getNameFinnish()));
                                    } else if (club.getClubOfficialCode().equals(partnerGroup.club.getOfficialCode())){
                                        assertThat(club.getGroups().get(0).getNameFinnish(), equalTo(partnerGroup.group.getNameFinnish()));
                                    } else {
                                        fail("Not a permit partner: " + club.getNameFinnish());
                                    }
                                });
                            }
                        });
                    });
                });
            });
        });
    }

    @Test
    public void testPerformance_noDbQueryIncreaseWithMoreEntries() {
        withDeerHuntingGroupFixture(f -> {
            createHuntingDaysWithEntries(f,10, 10, 10);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    assertAmountOfItems(permits, 10, 10, 10);
                });
            });
        });
    }

    @Ignore("Takes too long to be run in CI. Takes about a minute to run locally.")
    @Test
    public void testPerformance_hugeAmountOfEntriesWorks() {
        withDeerHuntingGroupFixture(f -> {
            createHuntingDaysWithEntries(f,100, 100, 100);
            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    LOG.info("Starting export"); // Log with timestamps
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    LOG.info("Export done");
                    assertAmountOfItems(permits, 100, 100, 100);
                });
            });
        });
    }

    @Test
    public void testPerformance_fewPermits() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();
        createPermits(species, 10);
        onSavedAndAuthenticated(apiUser, () -> {
            assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                final LED_Permits permits = feature.exportDeer(huntingYear());
                assertThat(permits.getPermits(), hasSize(10));

                // System.out.println(asJson(permits));
            });
        });
    }

    @Test
    public void testPerformance_batchHarvests() {
        withDeerHuntingGroupFixture(f -> {
            createHuntingDaysWithEntries(f,11, 10, 15);
            onSavedAndAuthenticated(apiUser, () -> {
                final int numOfBatches = 15; // 2 * ( numHuntingDays / batchSize ) + numberOfHarvests / batchSize
                                             // = 2 * 11 / 10 + 110 / 10 = 2 * 2 + 11 = 15
                assertMaxQueryCount(MAX_QUERY_COUNT_WITHOUT_BATCHES + numOfBatches, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear(), 10);
                    assertAmountOfItems(permits, 11, 10, 15);
                });
            });
        });
    }

    private List<HarvestPermit> createPermits(final GameSpecies species, final int numPermits) {
        return createList(numPermits, () -> {
            final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
            final HarvestPermit permit = model().newMooselikePermit(rhy);
            final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

            final HuntingClub club = model().newHuntingClub(rhy);
            model().newOccupation(club, model().newPerson(), OccupationType.SEURAN_YHDYSHENKILO);

            permit.getPermitPartners().add(club);
            permit.setPermitHolder(PermitHolder.createHolderForPerson(permit.getOriginalContactPerson()));

            final HuntingClubGroup group = model().newHuntingClubGroup(club, speciesAmount);
            group.updateHarvestPermit(permit);
            return permit;
        });

    }

    private void createHuntingDaysWithEntries(final HuntingGroupFixture fixture,
                                              final int numHuntingDays,
                                              final int numHarvestsPerDay,
                                              final int numObservationsPerDay) {
        for (int i = 0; i < numHuntingDays; i++) {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(fixture.group, today().minusDays(i));
            huntingDay.setStartTime(LocalTime.MIDNIGHT);
            createHarvests(fixture, huntingDay, numHarvestsPerDay);
            createObservations(fixture, huntingDay, numObservationsPerDay);
        }
    }

    private Harvest createHarvest(final HuntingGroupFixture fixture,
                                  final GroupHuntingDay huntingDay,
                                  final HarvestSpecimenType harvestSpecimenType) {
        final Harvest harvest = model().newHarvest(fixture.species, fixture.groupMember, huntingDay.getStartDate());
        harvest.updateHuntingDayOfGroup(huntingDay, fixture.groupLeader);
        model().newHarvestSpecimen(harvest, harvestSpecimenType);
        return harvest;
    }

    private List<Harvest> createHarvests(final HuntingGroupFixture fixture,
                                         final GroupHuntingDay huntingDay,
                                         final int numHarvests) {
        return createList(numHarvests, ()  -> {
            final Harvest harvest = model().newHarvest(fixture.species, fixture.groupMember, huntingDay.getStartDate());
            harvest.updateHuntingDayOfGroup(huntingDay, fixture.groupLeader);
            model().newHarvestSpecimen(harvest);
            return harvest;
        });
    }

    private List<Observation> createObservations(final HuntingGroupFixture fixture,
                                                 final GroupHuntingDay huntingDay,
                                                 final int numObservations) {
        return createList(numObservations, () -> {
            final Observation observation = model().newObservation(fixture.species, fixture.groupMember, DEER_HUNTING);
            observation.setPointOfTime(huntingDay.getStartDate().toDateTimeAtStartOfDay());
            observation.setGeoLocation(fixture.zoneCentroid);
            observation.setObservationType(NAKO);
            observation.setDeerHuntingType(OTHER);
            observation.setDeerHuntingTypeDescription("deerHuntingTypeDescription");
            observation.updateHuntingDayOfGroup(huntingDay, fixture.groupLeader);
            observation.setAmount(86);                          // sum of values below
            observation.setMooselikeMaleAmount(1);              // 1
            observation.setMooselikeFemaleAmount(2);            // 2
            observation.setMooselikeFemale1CalfAmount(3);       // 6
            observation.setMooselikeFemale2CalfsAmount(4);      // 12
            observation.setMooselikeFemale3CalfsAmount(5);      // 20
            observation.setMooselikeFemale4CalfsAmount(6);      // 30
            observation.setMooselikeCalfAmount(7);              // 7
            observation.setMooselikeUnknownSpecimenAmount(8);   // 8
            return observation;
        });
    }

    private static void assertGeoLocation(final GeoLocation expected, final LED_GeoLocation location) {
        if (isNull(expected)) {
            assertThat(location, is(nullValue()));
        } else {
            assertThat(location, is(notNullValue()));
            assertThat(location.getLatitude(), equalTo(expected.getLatitude()));
            assertThat(location.getLongitude(), equalTo(expected.getLongitude()));
            assertEnumEquals(expected.getSource(), location.getSource());
            assertThat(location.getAccuracy(), equalTo(expected.getAccuracy()));
            assertThat(location.getAltitude(), equalTo(expected.getAltitude()));
            assertThat(location.getAltitudeAccuracy(), equalTo(expected.getAltitudeAccuracy()));
        }
    }

    private static void assertDeerHuntingTypeEquals(final DeerHuntingType expected, final Enum<?> actual) {
        if (OTHER.equals(expected)) {
            assertThat(actual, is(notNullValue()));
            assertThat(actual.name(), equalTo("MUU"));
        } else {
            assertEnumEquals(expected, actual);
        }
    }

    private static void assertEnumEquals(final Enum<?> expected, final Enum<?> actual) {
        if (isNull(expected)) {
            assertThat(actual, is(nullValue()));
        } else {
            assertThat(actual, is(notNullValue()));
            assertThat(actual.name(), equalTo(expected.name()));
        }
    }

    private static void assertDateEquals(final DateTime expected, final LocalDateTime actual) {
        assertThat(actual, equalTo(toLocalDateTimeNullSafe(expected)));
    }

    private static void assertAmountOfItems(final LED_Permits permits,
                                            final int numDays,
                                            final int numHarvestsPerDay,
                                            final int numObservationsPerDay) {
        assertThat(permits.getPermits(), hasSize(1));
        permits.getPermits().forEach(permit -> {
            assertThat(permit.getHuntingClubs(), hasSize(1));
            permit.getHuntingClubs().forEach(club -> {
                assertThat(club.getGroups(), hasSize(1));
                club.getGroups().forEach(group -> {
                    assertThat(group.getHarvests(), hasSize(numDays * numHarvestsPerDay));
                    assertThat(group.getObservations(), hasSize(numDays * numObservationsPerDay));
                });
            });
        });
    }

    private static Integer getFemaleAndCalvesAmount(final List<LED_FemaleAndCalfs> femaleAndCalves, final int calves) {
        return femaleAndCalves.stream()
                .filter(Objects::nonNull)
                .filter(item -> calves == item.getCalfs())
                .map(LED_FemaleAndCalfs::getAmount)
                .findAny()
                .orElse(null);
    }

    private static LED_Group getOnlyGroup(final LED_Permits permits) {
        assertThat(permits.getPermits(), hasSize(1));
        final LED_Permit permit = permits.getPermits().get(0);

        assertThat(permit.getHuntingClubs(), hasSize(1));
        final LED_Club club = permit.getHuntingClubs().get(0);

        assertThat(club.getGroups(), hasSize(1));
        return club.getGroups().get(0);
    }

    private String asJson(final LED_Permits data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
