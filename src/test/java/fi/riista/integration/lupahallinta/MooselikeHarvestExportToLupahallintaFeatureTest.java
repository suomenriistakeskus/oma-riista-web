package fi.riista.integration.lupahallinta;

import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.integration.lupahallinta.club.LHMooselikeHarvestsCSVRow;
import fi.riista.test.EmbeddedDatabaseTest;
import io.vavr.Tuple;
import io.vavr.Tuple6;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MooselikeHarvestExportToLupahallintaFeatureTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private MooselikeHarvestExportToLupahallintaFeature feature;

    @Test(expected = AccessDeniedException.class)
    public void testNoPrivilege() {
        onSavedAndAuthenticated(createNewApiUser(), () -> feature.exportToCSV(0));
    }

    @Test
    public void testHasPrivilege() {
        withPersistedAndAuthenticatedRestUser(() -> feature.exportToCSV(0));
    }

    @Test
    public void testNoData() {
        withPersistedAndAuthenticatedRestUser(() -> assertThat(feature.exportToCSV(0), hasSize(0)));
    }

    @Test
    public void testNoHarvests() {
        withMooseHuntingGroupFixture(f -> {
            withPersistedAndAuthenticatedRestUser(() -> {
                final List<LHMooselikeHarvestsCSVRow> res = feature.exportToCSV(f.speciesAmount.resolveHuntingYear());
                assertThat(res, hasSize(1));
                assertRow(findRow(f.club, f.species, res), Tuple.of(0, 0, 0, 0, 0, 0));
            });
        });
    }

    @Test
    public void testWithHarvests() {
        withMooseHuntingGroupFixture(f -> {
            createHuntingDay(f.group, 1, 0, ADULT, MALE, f.species);
            withPersistedAndAuthenticatedRestUser(() -> {
                final List<LHMooselikeHarvestsCSVRow> res = feature.exportToCSV(f.speciesAmount.resolveHuntingYear());
                assertThat(res, hasSize(1));
                assertRow(findRow(f.club, f.species, res), Tuple.of(1, 0, 0, 0, 0, 0));
            });
        });
    }

    @Test
    public void testWithHarvestsMultiplePermitsAndSpeciesAmounts() {
        withMooseHuntingGroupFixture(f -> withHuntingGroupFixture(f.rhy, f.species, f2 -> {
            final GameSpecies moose = f.species;
            final GameSpecies deer = model().newDeerSubjectToClubHunting();

            final HarvestPermitSpeciesAmount deerAmount1 = model().newHarvestPermitSpeciesAmount(f.permit, deer);
            final HarvestPermitSpeciesAmount deerAmount2 = model().newHarvestPermitSpeciesAmount(f2.permit, deer);

            createHuntingDay(f.group, 1, 0, ADULT, MALE, moose);

            final HuntingClubGroup deerGroup = model().newHuntingClubGroup(f.club, deerAmount1);
            createHuntingDay(deerGroup, 1, 0, ADULT, FEMALE, deer);

            createHuntingDay(f2.group, 1, 0, YOUNG, MALE, moose);

            final HuntingClubGroup deerGroup2 = model().newHuntingClubGroup(f2.club, deerAmount2);
            createHuntingDay(deerGroup2, 1, 0, YOUNG, FEMALE, deer);

            withPersistedAndAuthenticatedRestUser(() -> {
                final List<LHMooselikeHarvestsCSVRow> res = feature.exportToCSV(f.speciesAmount.resolveHuntingYear());
                assertThat(res, hasSize(4));
                assertRow(findRow(f.club, moose, res), Tuple.of(1, 0, 0, 0, 0, 0));
                assertRow(findRow(f.club, deer, res), Tuple.of(0, 1, 0, 0, 0, 0));
                assertRow(findRow(f2.club, moose, res), Tuple.of(0, 0, 1, 0, 0, 0));
                assertRow(findRow(f2.club, deer, res), Tuple.of(0, 0, 0, 1, 0, 0));
            });
        }));
    }

    private static List<LHMooselikeHarvestsCSVRow> findRow(final HuntingClub club,
                                                           final GameSpecies species,
                                                           final List<LHMooselikeHarvestsCSVRow> res) {
        return res.stream()
                .filter(r -> r.getCustomerNumber().equals(club.getOfficialCode()) && r.getSpeciesCode() == species.getOfficialCode())
                .collect(toList());
    }

    private static void assertRow(final List<LHMooselikeHarvestsCSVRow> rows,
                                  final Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> expected) {

        assertEquals(1, rows.size());
        LHMooselikeHarvestsCSVRow row = rows.get(0);
        assertEquals(expected,
                Tuple.of(row.getAdultMales(), row.getAdultFemales(), row.getYoungMales(), row.getYoungFemales(),
                        row.getAdultsNonEdible(), row.getYoungNonEdible()));
    }

    private void withPersistedAndAuthenticatedRestUser(final Runnable cmd) {
        onSavedAndAuthenticated(createNewApiUser(SystemUserPrivilege.EXPORT_LUPAHALLINTA_MOOSELIKE_HARVESTS), cmd);
    }

    private void createHuntingDay(final HuntingClubGroup group, final int howManyHarvests, final int howManyNotEdible,
                                  final GameAge age, final GameGender gender, final GameSpecies species) {

        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today());
        huntingDay.setNumberOfHunters(1);

        for (int i = 0; i < howManyHarvests; i++) {
            createHarvest(i < howManyNotEdible, age, gender, huntingDay, species);
        }
    }

    private void createHarvest(final boolean notEdible, final GameAge age, final GameGender gender,
                               final GroupHuntingDay huntingDay, final GameSpecies species) {

        final Harvest harvest = model().newHarvest(species, model().newPerson(), huntingDay);

        final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, age, gender);
        specimen.setNotEdible(notEdible);
    }
}
