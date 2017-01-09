package fi.riista.feature.huntingclub.moosedatacard;

import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.DateUtil.toDateNullSafe;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;

import javaslang.Tuple;
import javaslang.Tuple3;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MooseDataCardImportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MooseDataCardImportFeature feature;

    @Resource
    private MooseDataCardImportRepository importRepo;

    @Resource
    private GroupHuntingDayRepository huntingDayRepo;

    @Resource
    private HarvestRepository harvestRepo;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepo;

    @Resource
    private ObservationRepository observationRepo;

    @Resource
    private ObservationSpecimenRepository observationSpecimenRepo;

    @Test
    public void testRevokeMooseDataCardImport() {
        final GameSpecies species = model().newGameSpecies();
        final Person author = model().newPerson();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final HuntingClub club = model().newHuntingClub(rhy);
        final HuntingClubGroup group1 = model().newHuntingClubGroup(club, species);
        group1.setFromMooseDataCard(true);

        final MooseDataCardImport import1 = model().newMooseDataCardImport(group1);
        final Tuple3<List<GroupHuntingDay>, List<Harvest>, List<Observation>> import1HuntingDayData =
                createHuntingDays(author, group1, import1, IntStream.range(0, 5));

        // Create another moose data card import for same group but with different hunting days.
        final MooseDataCardImport import2 = model().newMooseDataCardImport(group1);
        final Tuple3<List<GroupHuntingDay>, List<Harvest>, List<Observation>> import2HuntingDayData =
                createHuntingDays(author, group1, import2, IntStream.range(-5, 0));

        // Create another group with moose data card import for same club.
        final HuntingClubGroup group2 = model().newHuntingClubGroup(club, species);
        final MooseDataCardImport import3 = model().newMooseDataCardImport(group2);
        final Tuple3<List<GroupHuntingDay>, List<Harvest>, List<Observation>> group2HuntingDayData =
                createHuntingDays(model().newPerson(), group2, import3, IntStream.range(0, 5));

        // Add another group for different club and not created within moose data card import.
        final HuntingClubGroup group3 = model().newHuntingClubGroup(model().newHuntingClub(rhy), species);
        final Tuple3<List<GroupHuntingDay>, List<Harvest>, List<Observation>> group3HuntingDayData =
                createHuntingDays(model().newPerson(), group3, null, IntStream.range(0, 5));

        // Add a harvest and observation with specimens for same author that, however,
        // are not related to any hunting day/group/club.
        final Harvest harvest1 = model().newHarvest(species, author);
        model().newHarvestSpecimen(harvest1);
        final Observation observation1 = model().newObservation(species, author);
        final ObservationSpecimen specimen2 = model().newObservationSpecimen(observation1);

        // Add a harvest and observation with specimens for different author that are not related
        // to any hunting day/group/club.
        final Harvest harvest2 = model().newHarvest(species, model().newPerson());
        model().newHarvestSpecimen(harvest2);
        final Observation observation2 = model().newObservation(species, model().newPerson());
        final ObservationSpecimen specimen4 = model().newObservationSpecimen(observation2);

        onSavedAndAuthenticated(createNewModerator(), () -> {

            feature.revokeMooseDataCardImport(import1.getId());

            runInTransaction(() -> {
                assertImportData(import1.getId(), true, group1.getId());

                assertEmpty(huntingDayRepo.findAll(F.getUniqueIds(import1HuntingDayData._1)));
                assertEmpty(harvestRepo.findAll(F.getUniqueIds(import1HuntingDayData._2)));
                assertEmpty(observationRepo.findAll(F.getUniqueIds(import1HuntingDayData._3)));

                assertImportData(import2.getId(), false, group1.getId());
                assertImportData(import3.getId(), false, group2.getId());

                // Assert that hunting days, harvest and observations not related to import stay
                // unaffected (to be not deleted).
                assertUniqueIds(
                        F.concat(import2HuntingDayData._1, group2HuntingDayData._1, group3HuntingDayData._1),
                        huntingDayRepo.findAll());

                final List<Harvest> harvestsExpectedToExistAfterRevoke = F.concat(
                        import2HuntingDayData._2, group2HuntingDayData._2, group3HuntingDayData._2,
                        asList(harvest1, harvest2));

                assertUniqueIds(harvestsExpectedToExistAfterRevoke, harvestRepo.findAll());

                assertUniqueIds(
                        F.concat(import2HuntingDayData._3, group2HuntingDayData._3, group3HuntingDayData._3,
                                asList(observation1, observation2)),
                        observationRepo.findAll());

                assertUniqueIds(harvestsExpectedToExistAfterRevoke.stream()
                        .flatMap(harvest -> harvest.getSortedSpecimens().stream())
                        .collect(toList()),
                        harvestSpecimenRepo.findAll());

                assertUniqueIds(asList(specimen2, specimen4), observationSpecimenRepo.findAll());
            });
        });
    }

    private static Date toDate(final LocalDate date, final int hour) {
        return toDateNullSafe(date.toLocalDateTime(new LocalTime(hour, 0)));
    }

    private static <ID, T extends HasID<ID>> void assertUniqueIds(final List<T> list1, final List<T> list2) {
        assertEquals(F.getUniqueIds(list1), F.getUniqueIds(list2));
    }

    private Tuple3<List<GroupHuntingDay>, List<Harvest>, List<Observation>> createHuntingDays(
            final Person author,
            final HuntingClubGroup group,
            final MooseDataCardImport imp,
            final IntStream dayOffsetsFromCurrentDate) {

        final List<LocalDate> huntingDates =
                dayOffsetsFromCurrentDate.mapToObj(i -> DateUtil.today().plusDays(i)).collect(toList());

        final List<GroupHuntingDay> huntingDays = huntingDates.stream().map(date -> {

            final GroupHuntingDay day = model().newGroupHuntingDay(group, date);
            day.setMooseDataCardImport(imp);
            return day;

        }).collect(toList());

        final List<Harvest> harvests = Stream.of(9, 12, 15).flatMap(hour -> huntingDays.stream().map(day -> {

            final Harvest harvest = model().newHarvest(group.getSpecies(), author, day);
            harvest.setPointOfTime(toDate(day.getStartDate(), hour));

            model().newHarvestSpecimen(harvest);

            return harvest;

        })).collect(toList());

        final List<Observation> observations =
                Stream.of(9, 12, 15).flatMap(hour -> huntingDays.stream().map(day -> {

                    final Observation observation = model().newObservation(group.getSpecies(), author, day);
                    observation.setPointOfTime(toDate(day.getStartDate(), hour));

                    return observation;

                })).collect(toList());

        return Tuple.of(huntingDays, harvests, observations);
    }

    private void assertImportData(final long importId, final boolean expectSoftDeleted, final long expectedGroupId) {
        final MooseDataCardImport reloadedImport = importRepo.findOne(importId);
        assertNotNull(reloadedImport);
        assertEquals(expectSoftDeleted, reloadedImport.isDeleted());
        assertNotNull(reloadedImport.getGroup());
        assertTrue(Objects.equals(expectedGroupId, reloadedImport.getGroup().getId()));
    }

}
