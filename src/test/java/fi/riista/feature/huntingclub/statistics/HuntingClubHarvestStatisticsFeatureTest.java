package fi.riista.feature.huntingclub.statistics;

import com.google.common.collect.ImmutableMap;
import fi.riista.api.club.ClubHarvestSummaryRequestDTO;
import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Map;

import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class HuntingClubHarvestStatisticsFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubHarvestStatisticsFeature huntingClubHarvestSummaryFeature;

    private SystemUser createContactUserForClub(final HuntingClub club) {
        return createUser(model().newHuntingClubMember(club, OccupationType.SEURAN_YHDYSHENKILO).getPerson());
    }

    @Test
    public void testSmoke() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
        createHarvestWithLocation(location, hunter, species);
        final int currentYear = currentYear();
        assertHarvestCount(club, species, 1L,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testMatchHuntingClub() {
        withPerson(author -> withPerson(hunter -> {

            final HuntingClub club = model().newHuntingClub();

            model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

            final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
            Harvest harvest = model().newHarvest(species, author, hunter);
            harvest.setHuntingClub(club);

            final int currentYear = currentYear();
            assertHarvestCount(club, species, 1L,
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
        }));
    }

    @Test
    public void testMatchHunterOnly() {
        withPerson(author -> withPerson(hunter -> {
            final GeoLocation location = geoLocation();
            final HuntingClub club = createClubWithAreaAndZone(location);

            model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

            final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
            model().newHarvest(species, author, hunter).setGeoLocation(location);

            final int currentYear = currentYear();
            assertHarvestCount(club, species, 1L,
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
        }));
    }

    @Test
    public void testMatchAuthorOnly() {
        withPerson(author -> withPerson(hunter -> {
            final GeoLocation location = geoLocation();
            final HuntingClub club = createClubWithAreaAndZone(location);

            model().newOccupation(club, author, OccupationType.SEURAN_JASEN);

            final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
            model().newHarvest(species, author, hunter).setGeoLocation(location);

            final int currentYear = currentYear();
            assertHarvestCount(club, species, 1L,
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                    DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
        }));
    }

    @Test
    public void testOccupationRestriction() {
        final GeoLocation location = geoLocation();

        createHarvestWithLocation(location, model().newPerson(), model().newGameSpecies());

        final int currentYear = currentYear();
        assertNoHarvests(createClubWithAreaAndZone(location),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testOccupationNotValid() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        final Occupation occupation = model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);
        // Validity end set to yesterday
        occupation.setEndDate(today().minusDays(1));

        createHarvestWithLocation(location, hunter, model().newGameSpecies());

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));

    }

    @Test
    public void testOccupationSoftDeleted() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN).softDelete();

        createHarvestWithLocation(location, hunter, model().newGameSpecies());

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testMultipleOccupations() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        // Duplicate occupation, should not multiply results
        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
        createHarvestWithLocation(location, hunter, species);

        final int currentYear = currentYear();
        assertHarvestCount(club, species, 1L,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testHuntingYearRestriction() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
        final HuntingClubGroup group = model().newHuntingClubGroupWithAreaContaining(location, club, species);
        // Hunting year not current year
        group.getHuntingArea().setHuntingYear(group.getHuntingArea().getHuntingYear() - 1);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        createHarvestWithLocation(location, hunter, species);

        final int currentYear = currentYear();
        assertHarvestCount(club, species, 1L,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testAreaRestriction() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        createHarvestWithLocation(location.move(10, 10), hunter, model().newGameSpecies());

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testAreaRestriction_IgnoreMooselike() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.forEach(speciesCode -> {
            createHarvestWithLocation(location, hunter, model().newGameSpecies(speciesCode));
        });

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void testAreaDeactivated() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location, false);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        createHarvestWithLocation(location, hunter, model().newGameSpecies());

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    private void assertClubMemberHarvests(final HuntingClub club, final Map<Integer, Long> expectedCounts,
                                          final LocalDate begin, final LocalDate end) {
        final ClubHarvestSummaryRequestDTO dto = new ClubHarvestSummaryRequestDTO(club.getId(), begin, end);
        final HuntingClubHarvestStatisticsDTO summary = huntingClubHarvestSummaryFeature.getSummary(dto);

        assertNotNull(summary);
        assertThat(summary.getItems(), Matchers.hasSize(expectedCounts.size()));
        assertThat(getSpeciesCounts(summary), Matchers.equalTo(expectedCounts));
    }

    private static Map<Integer, Long> getSpeciesCounts(final HuntingClubHarvestStatisticsDTO dto) {
        return dto.getItems().stream().collect(toMap(
                row -> row.getSpecies().getCode(), row -> row.getCount()));
    }

    private HuntingClub createClubWithAreaAndZone(GeoLocation location) {
        return createClubWithAreaAndZone(location, true);
    }

    private HuntingClub createClubWithAreaAndZone(GeoLocation location, boolean active) {
        final HuntingClub club = model().newHuntingClub();
        final HuntingClubArea area = model().newHuntingClubArea(club, model().newGISZoneContaining(location));
        area.setActive(active);
        return club;
    }

    @Test
    public void whenHarvestReportRequired_onlyThoseWithApprovedHarvestReportCounted() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        final GameSpecies species = model().newGameSpeciesNotSubjectToClubHunting();
        createHarvestWithHarvestReportRequiredWithReport(location, hunter, species, null);

        for (HarvestReportState s : HarvestReportState.values()) {
            createHarvestWithHarvestReportRequiredWithReport(location, hunter, species, s);
        }

        final int currentYear = currentYear();
        assertHarvestCount(club, species, 1L,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    private void createHarvestWithHarvestReportRequiredWithReport(GeoLocation location, Person hunter, GameSpecies species, HarvestReportState state) {
        final Harvest harvest = model().newHarvest(species, hunter, hunter);
        harvest.setGeoLocation(location);
        harvest.setHarvestReportRequired(true);

        if (state != null) {
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());
        }
    }

    @Test
    public void mooselikeNotLinkedToGroup() {
        final GeoLocation location = geoLocation();
        final HuntingClub club = createClubWithAreaAndZone(location);

        final Person hunter = model().newPerson();
        model().newOccupation(club, hunter, OccupationType.SEURAN_JASEN);

        GameSpecies.MOOSE_AND_DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING.forEach(speciesCode -> {
            final GameSpecies species = model().newGameSpecies(speciesCode);
            createHarvestWithLocation(location, hunter, species);
            createHarvestWithLocation(location.move(100, 100), hunter, species);
        });

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void mooselikeLinkedToGroup() {
        final int huntingYear = DateUtil.huntingYear();

        final GeoLocation location = geoLocation();
        final GameSpecies species = model().newGameSpeciesMoose();
        final HuntingClub club = createClubWithAreaAndZone(location);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, species, huntingYear);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today());

        final Person hunter = model().newPerson();
        // note that hunter is not a member

        createHarvestWithLocationAndHuntingDay(location, hunter, species, huntingDay);
        createHarvestWithLocationAndHuntingDay(location.move(100, 100), hunter, species, huntingDay);

        final int currentYear = currentYear();
        assertHarvestCount(club, species, 2L,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void mooselikeLinkedToGroup_WrongCalendarYear() {
        final int huntingYear = DateUtil.huntingYear() - 1;

        final GeoLocation location = geoLocation();
        final GameSpecies species = model().newGameSpeciesMoose();
        final HuntingClub club = createClubWithAreaAndZone(location);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, species, huntingYear);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today().minusYears(1));

        // harvest is in wrong calendar year
        final Harvest h = createHarvestWithLocationAndHuntingDay(location, model().newPerson(), species, huntingDay);
        h.setPointOfTime(h.getPointOfTimeAsLocalDate().withYear(huntingYear).toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    @Test
    public void mooselikeLinkedToGroup_WrongHuntingYear() {
        // Group is for previous huntingYear not overlapping with current calendar year
        final int huntingYear = DateUtil.huntingYear() - 2;

        final GeoLocation location = geoLocation();
        final GameSpecies species = model().newGameSpeciesMoose();
        final HuntingClub club = createClubWithAreaAndZone(location);
        final HuntingClubGroup group = model().newHuntingClubGroup(club, species, huntingYear);
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today().minusYears(1));

        createHarvestWithLocationAndHuntingDay(location, model().newPerson(), species, huntingDay);

        final int currentYear = currentYear();
        assertNoHarvests(club,
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear)),
                DateUtil.toLocalDateNullSafe(DateUtil.beginOfCalendarYear(currentYear + 1).minusDays(1)));
    }

    private Harvest createHarvestWithLocation(GeoLocation location, Person hunter, GameSpecies species) {
        final Harvest harvest = model().newHarvest(species, hunter, hunter);
        harvest.setGeoLocation(location);
        return harvest;
    }

    private Harvest createHarvestWithLocationAndHuntingDay(GeoLocation location, Person hunter, GameSpecies species, GroupHuntingDay huntingDay) {
        final Harvest harvest = model().newHarvest(species, hunter, hunter);
        harvest.setGeoLocation(location);
        harvest.updateHuntingDayOfGroup(huntingDay, null);
        return harvest;
    }

    private void assertNoHarvests(HuntingClub club, final LocalDate begin, final LocalDate end) {
        onSavedAndAuthenticated(createContactUserForClub(club), () -> {
            assertClubMemberHarvests(club, ImmutableMap.of(), begin, end);
        });
    }

    private void assertHarvestCount(final HuntingClub club, final GameSpecies species, final long count,
                                    final LocalDate begin, final LocalDate end) {
        onSavedAndAuthenticated(createContactUserForClub(club), () -> {
            assertClubMemberHarvests(club, ImmutableMap.of(species.getOfficialCode(), count), begin, end);
        });
    }
}
