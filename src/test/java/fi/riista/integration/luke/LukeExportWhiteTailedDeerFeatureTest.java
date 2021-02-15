package fi.riista.integration.luke;

import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
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
import fi.riista.feature.organization.person.Person;
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
import static fi.riista.test.TestUtils.createList;
import static fi.riista.util.DateUtil.huntingYear;
import static fi.riista.util.DateUtil.toLocalDateTimeNullSafe;
import static fi.riista.util.DateUtil.today;
import static java.util.Objects.isNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class LukeExportWhiteTailedDeerFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin, ObservationFixtureMixin {

    private static final Logger LOG = LoggerFactory.getLogger(LukeExportWhiteTailedDeerFeatureTest.class);

    private static final int MAX_QUERY_COUNT_WITHOUT_BATCHES = 12;
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

    @Test
    public void testValidity_harvest() {
        withDeerHuntingGroupFixture(f -> {
            final GroupHuntingDay huntingDay = model().newGroupHuntingDay(f.group, today());
            huntingDay.setStartTime(LocalTime.MIDNIGHT);
            final Harvest expectedHarvest = createHarvests(f, huntingDay, 1).get(0);

            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    // System.out.println(asJson(permits));

                    final LED_Group group = getOnlyGroup(permits);
                    assertEquals(1, group.getHarvests().size());
                    final LED_Harvest harvest = group.getHarvests().get(0);

                    assertGeoLocation(expectedHarvest.getGeoLocation(), harvest.getGeoLocation());
                    assertDateEquals(expectedHarvest.getPointOfTime(), harvest.getPointOfTime());
                    assertDeerHuntingTypeEquals(expectedHarvest.getDeerHuntingType(), harvest.getHuntingType().getHuntingType());
                    assertEquals(expectedHarvest.getDeerHuntingOtherTypeDescription(), harvest.getHuntingType().getHuntingTypeDescription());

                    final HarvestSpecimen expectedSpecimen = expectedHarvest.getSortedSpecimens().get(0);
                    assertEnumEquals(expectedSpecimen.getGender(), harvest.getGender());
                    assertEnumEquals(expectedSpecimen.getAge(), harvest.getAge());
                    assertEquals(expectedSpecimen.getWeightEstimated(), harvest.getWeightEstimated());
                    assertEquals(expectedSpecimen.getWeightMeasured(), harvest.getWeightMeasured());
                    assertEnumEquals(expectedSpecimen.getAntlersType(), harvest.getAntlersType());
                    assertEquals(expectedSpecimen.getAntlersWidth(), harvest.getAntlersWidth());
                    assertEquals(expectedSpecimen.getAntlerPointsLeft(), harvest.getAntlerPointsLeft());
                    assertEquals(expectedSpecimen.getAntlerPointsRight(), harvest.getAntlerPointsRight());
                    assertEquals(expectedSpecimen.getNotEdible(), harvest.isNotEdible());
                    assertEquals(expectedSpecimen.getAdditionalInfo(), harvest.getAdditionalInfo());
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
                    assertEquals(1, group.getObservations().size());
                    final LED_Observation observation = group.getObservations().get(0);

                    assertGeoLocation(expected.getGeoLocation(), observation.getGeoLocation());
                    assertDateEquals(expected.getPointOfTime(), observation.getPointOfTime());
                    assertDeerHuntingTypeEquals(expected.getDeerHuntingType(), observation.getHuntingType().getHuntingType());
                    assertEquals(expected.getDeerHuntingTypeDescription(), observation.getHuntingType().getHuntingTypeDescription());
                    assertEnumEquals(expected.getObservationType(), observation.getObservationType());
                    final GameSpecies expectedSpecies = expected.getSpecies();
                    assertEquals(expectedSpecies.getOfficialCode(), observation.getGameSpeciesCode());
                    assertEquals(expectedSpecies.getNameFinnish(), observation.getGameSpeciesNameFinnish());
                    assertEquals(expected.getMooselikeMaleAmount(), observation.getMooselikeMaleAmount());
                    assertEquals(expected.getMooselikeUnknownSpecimenAmount(), observation.getMooselikeUnknownSpecimenAmount());
                    assertEquals(expected.getMooselikeCalfAmount(), observation.getMooselikeSolitaryCalfAmount());
                    assertEquals(expected.getMooselikeFemaleAmount(), getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 0));
                    assertEquals(expected.getMooselikeFemale1CalfAmount(), getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 1));
                    assertEquals(expected.getMooselikeFemale2CalfsAmount(), getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 2));
                    assertEquals(expected.getMooselikeFemale3CalfsAmount(), getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 3));
                    assertEquals(expected.getMooselikeFemale4CalfsAmount(), getFemaleAndCalvesAmount(observation.getMooseLikeFemaleAndCalfs(), 4));
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

                    assertEquals(1, permits.getPermits().size());
                    final LED_Permit permit = permits.getPermits().get(0);
                    assertEquals(f.permit.getPermitNumber(), permit.getPermitNumber());
                    assertEquals(f.permit.getRhy().getOfficialCode(), permit.getRhyOfficialCode());
                    assertEquals(null, permit.getContactPerson());

                    final LED_Amount amount = permit.getAmount();
                    assertEquals(f.speciesAmount.getSpecimenAmount(), amount.getAmount(), 0);
                    assertEquals(f.speciesAmount.getRestrictionType(), amount.getRestriction());
                    assertEquals(f.speciesAmount.getRestrictionAmount(), amount.getRestrictedAmount());

                    assertEquals(1, permit.getHuntingClubs().size());
                    final LED_Club club = permit.getHuntingClubs().get(0);
                    assertEquals(f.club.getOfficialCode(), club.getClubOfficialCode());
                    assertEquals(f.club.getNameFinnish(), club.getNameFinnish());
                    assertGeoLocation(f.club.getGeoLocation(), club.getGeoLocation());
                    assertEquals(null, club.getContactPerson());
                    assertEquals(f.rhy.getOfficialCode(), club.getRhyOfficialCode());

                    assertEquals(1, club.getGroups().size());
                    final LED_Group group = club.getGroups().get((0));
                    assertEquals(f.group.getNameFinnish(), group.getNameFinnish());
                    assertEquals("WEB", group.getDataSource().name()); // MOOSE_DATA_CARD is not used with wtd

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

                    assertEquals(1, permits.getPermits().size());

                    final List<LED_Amount> amendmentPermits = permits.getPermits().get(0).getAmendmentPermits();
                    assertEquals(1, amendmentPermits.size());
                    assertEquals(1.0, amendmentPermits.get(0).getAmount(), 0);
                    assertEquals(LED_RestrictionType.AE, amendmentPermits.get(0).getRestriction());
                    assertEquals(0.5, amendmentPermits.get(0).getRestrictedAmount(), 0);

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

                    assertEquals(1, permits.getPermits().size());
                    assertEquals(1, permits.getPermits().get(0).getHuntingClubs().size());
                    final LED_HuntingSummary summary = permits.getPermits().get(0).getHuntingClubs().get(0).getHuntingSummary();
                    assertNotNull(summary);
                    assertEquals(expected.getHuntingEndDate(), summary.getHuntingEndDate());
                    assertEquals(expected.isHuntingFinished(), summary.isHuntingFinished());
                    assertEquals(expected.getAreaSizeAndPopulation().getTotalHuntingArea(), summary.getTotalHuntingArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getEffectiveHuntingArea(), summary.getEffectiveHuntingArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea(), summary.getPopulationRemainingInTotalHuntingArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea(), summary.getPopulationRemainingInEffectiveHuntingArea());
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

                    assertEquals(1, permits.getPermits().size());
                    assertEquals(1, permits.getPermits().get(0).getHuntingClubs().size());
                    final LED_Overrides override = permits.getPermits().get(0).getHuntingClubs().get(0).getOverrides();
                    assertNotNull(override);

                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfAdultMales()), override.getAdultMales());
                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfAdultFemales()), override.getAdultFemales());
                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfYoungMales()), override.getYoungMales());
                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfYoungFemales()), override.getYoungFemales());
                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfNonEdibleAdults()), override.getNonEdibleAdults());
                    assertEquals(Integer.valueOf(expected.getModeratedHarvestCounts().getNumberOfNonEdibleYoungs()), override.getNonEdibleYoung());
                    assertEquals(expected.getAreaSizeAndPopulation().getTotalHuntingArea(), override.getTotalHuntingArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getEffectiveHuntingArea(), override.getEffectiveHuntingArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getRemainingPopulationInTotalArea(), override.getRemainingPopulationInTotalArea());
                    assertEquals(expected.getAreaSizeAndPopulation().getRemainingPopulationInEffectiveArea(), override.getRemainingPopulationInEffectiveArea());

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

                    assertEquals(1, permits.getPermits().size());
                    assertEquals(1, permits.getPermits().get(0).getHuntingClubs().size());
                    final LED_Overrides override = permits.getPermits().get(0).getHuntingClubs().get(0).getOverrides();
                    assertNotNull(override);

                    assertEquals(1, permits.getPermits().get(0).getHuntingClubs().get(0).getGroups().size());
                    final LED_Group group = permits.getPermits().get(0).getHuntingClubs().get(0).getGroups().get(0);
                    assertEquals(0, group.getHarvests().size());
                    assertEquals(10, group.getObservations().size());

                });
            });
        });
    }

    @Test
    public void testValidity_samePersonIsPermitAndClubContact() {
        withDeerHuntingGroupFixture(f -> {
            // Create a new club which contact is permit's contact...
            final Person expected = f.permit.getOriginalContactPerson();
            final HuntingClub newClub = model().newHuntingClub(f.rhy);
            model().newOccupation(newClub, expected, OccupationType.SEURAN_YHDYSHENKILO);

            // ... and add it as permit partner.
            f.permit.getPermitPartners().add(newClub);
            model().newHuntingClubGroup(newClub, f.speciesAmount)
                    .updateHarvestPermit(f.permit);

            f.permit.getPermitPartners().remove(f.club); // Just make checking of results easier.

            onSavedAndAuthenticated(apiUser, () -> {
                assertMaxQueryCount(MAX_QUERY_COUNT_WITH_ONE_BATCH, () -> {
                    final LED_Permits permits = feature.exportDeer(huntingYear());
                    //System.out.println(asJson(permits));

                    assertEquals(1, permits.getPermits().size());

                    final LED_Permit permit = permits.getPermits().get(0);
                    assertEquals(null, permit.getContactPerson());
                    assertEquals(1, permit.getHuntingClubs().size());

                    final LED_Club club = permit.getHuntingClubs().get(0);
                    assertEquals(null, club.getContactPerson());

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
                                assertEquals(2, permit.getHuntingClubs().size());

                                permit.getHuntingClubs().forEach(club -> {
                                    assertEquals(1, club.getGroups().size());

                                    if (club.getClubOfficialCode().equals(f.club.getOfficialCode())) {
                                        assertEquals(f.group.getNameFinnish(), club.getGroups().get(0).getNameFinnish());
                                    } else if (club.getClubOfficialCode().equals(partnerGroup.club.getOfficialCode())){
                                        assertEquals(partnerGroup.group.getNameFinnish(), club.getGroups().get(0).getNameFinnish());
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
                assertEquals(10, permits.getPermits().size());

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
            assertNull(location);
        } else {
            assertNotNull(location);
            assertEquals(expected.getLatitude(), location.getLatitude());
            assertEquals(expected.getLongitude(), location.getLongitude());
            assertEnumEquals(expected.getSource(), location.getSource());
            assertEquals(expected.getAccuracy(), location.getAccuracy());
            assertEquals(expected.getAltitude(), location.getAltitude());
            assertEquals(expected.getAltitudeAccuracy(), location.getAltitudeAccuracy());
        }
    }

    private static void assertDeerHuntingTypeEquals(final DeerHuntingType expected, final Enum<?> actual) {
        if (OTHER.equals(expected)) {
            assertNotNull(actual);
            assertEquals("MUU", actual.name());
        } else {
            assertEnumEquals(expected, actual);
        }
    }

    private static void assertEnumEquals(final Enum<?> expected, final Enum<?> actual) {
        if (isNull(expected)) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertEquals(expected.name(), actual.name());
        }
    }

    private static void assertDateEquals(final DateTime expected, final LocalDateTime actual) {
        assertEquals(toLocalDateTimeNullSafe(expected), actual);
    }

    private static void assertAmountOfItems(final LED_Permits permits,
                                            final int numDays,
                                            final int numHarvestsPerDay,
                                            final int numObservationsPerDay) {
        assertEquals(1, permits.getPermits().size());
        permits.getPermits().forEach(permit -> {
            assertEquals(1, permit.getHuntingClubs().size());
            permit.getHuntingClubs().forEach(club -> {
                assertEquals(1, club.getGroups().size());
                club.getGroups().forEach(group -> {
                    assertEquals(numDays * numHarvestsPerDay, group.getHarvests().size());
                    assertEquals(numDays * numObservationsPerDay, group.getObservations().size());
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
        assertEquals(1, permits.getPermits().size());
        final LED_Permit permit = permits.getPermits().get(0);

        assertEquals(1, permit.getHuntingClubs().size());
        final LED_Club club = permit.getHuntingClubs().get(0);

        assertEquals(1, club.getGroups().size());
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
