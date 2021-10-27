package fi.riista.feature.harvestpermit.report.jhtarchive;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestLocationType;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Localiser;
import io.vavr.collection.Stream;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.currentYear;
import static fi.riista.util.Locales.FI;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isOneOf;

@RunWith(Theories.class)
public class JhtArchiveFeatureTest extends EmbeddedDatabaseTest implements MooselikeFixtureMixin, DerogationFixtureMixin {

    @Resource
    private MessageSource messageSource;

    @Resource
    private JhtArchiveFeature feature;
    private Localiser localiser;

    @Before
    public void setUp() {
        localiser = new EnumLocaliser(messageSource, FI);
    }

    /**
     * immaterialPermitDataForExcel tests
     */

    @Test(expected = AccessDeniedException.class)
    public void immaterialPermitDataForExcel_userHasNoAccess() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.immaterialPermitDataForExcel(null, currentYear());
        });
    }

    @Test
    public void immaterialPermitDataForExcel_moderatorHasAccess() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.immaterialPermitDataForExcel(null, currentYear());
        });
    }

    @Test
    public void immaterialPermitDataForExcel_adminHasAccess() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.immaterialPermitDataForExcel(null, currentYear());
        });
    }

    @Test
    public void immaterialPermitDataForExcel_noResults() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<JhtArchiveExcelDTO> result = feature.immaterialPermitDataForExcel(null, currentYear());
            assertThat(result, hasSize(0));
        });
    }

    @Test
    public void immaterialPermitDataForExcel_gotData() {
        withPermitCategory(HarvestPermitCategory.WEAPON_TRANSPORTATION, wt ->
            withPermitCategory(HarvestPermitCategory.DISABILITY, d ->
                withPermitCategory(HarvestPermitCategory.DOG_DISTURBANCE, dd ->
                    withPermitCategory(HarvestPermitCategory.DOG_UNLEASH, du ->
                        withPermitCategory(HarvestPermitCategory.NEST_REMOVAL, notIncluded ->
                            onSavedAndAuthenticated(createNewModerator(), () -> {
                                final List<JhtArchiveExcelDTO> results = feature.immaterialPermitDataForExcel(null, currentYear());
                                assertThat(results, hasSize(4));
                                results.forEach(actual -> {
                                    assertThat(actual.getPermitNumber(),
                                               isOneOf(wt.permit.getPermitNumber(),
                                                       d.permit.getPermitNumber(),
                                                       dd.permit.getPermitNumber(),
                                                       du.permit.getPermitNumber()));
                                    assertThat(actual.getPermitType().getTranslation(FI),
                                               isOneOf(PermitTypeCode.getDecisionName(wt.permit.getPermitTypeCode()).getTranslation(FI),
                                                       PermitTypeCode.getDecisionName(d.permit.getPermitTypeCode()).getTranslation(FI),
                                                       PermitTypeCode.getDecisionName(dd.permit.getPermitTypeCode()).getTranslation(FI),
                                                       PermitTypeCode.getDecisionName(du.permit.getPermitTypeCode()).getTranslation(FI)));
                                    assertThat(actual.getRka().getTranslation(FI),
                                               isOneOf(wt.rka.getNameFinnish(),
                                                       d.rka.getNameFinnish(),
                                                       dd.rka.getNameFinnish(),
                                                       du.rka.getNameFinnish()));
                                });
                            }))))));
    }

    @Test
    public void immaterialPermitDataForExcel_queriesAreNonLinear() {
        final int NUMBER_OF_PERMITS = 100;
        final int NUMBER_OF_QUERIES = 4;

        final EntitySupplier es = model();
        final List<GameSpecies> species = Stream.range(0, 10).map((i) -> es.newGameSpecies()).collect(toList());
        final List<RiistakeskuksenAlue> rkas = Stream.range(0, 10).map((i) -> es.newRiistakeskuksenAlue()).collect(toList());
        final List<HarvestPermitCategory> categories = Arrays.asList(HarvestPermitCategory.WEAPON_TRANSPORTATION,
                                                                     HarvestPermitCategory.DISABILITY,
                                                                     HarvestPermitCategory.DOG_DISTURBANCE,
                                                                     HarvestPermitCategory.DOG_UNLEASH);
        Stream.range(0, NUMBER_OF_PERMITS).forEach((i) -> {
            final RiistakeskuksenAlue rka = some(rkas);
            final Riistanhoitoyhdistys rhy = es.newRiistanhoitoyhdistys(rka);
            final HuntingClub club = es.newHuntingClub(rhy);
            new DerogationFixture(es, rka, rhy, club, some(species), some(categories));
        });

        assertMaxQueryCount(NUMBER_OF_QUERIES, () -> {
            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> results = feature.immaterialPermitDataForExcel(null, currentYear());
                assertThat(results, hasSize(NUMBER_OF_PERMITS));
            });
        });
    }

    /**
     * permitDataForExcel tests
     */

    @Test(expected = AccessDeniedException.class)
    public void permitDataForExcel_userHasNoAccess() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.permitDataForExcel(null, null, currentYear());
        });
    }

    @Test
    public void permitDataForExcel_moderatorHasAccess() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.permitDataForExcel(null, null, currentYear());
        });
    }

    @Test
    public void permitDataForExcel_adminHasAccess() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.permitDataForExcel(null, null, currentYear());
        });
    }

    @Test
    public void permitDataForExcel_noResults() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<JhtArchiveExcelDTO> result = feature.permitDataForExcel(null, null, 2016);
            assertThat(result, hasSize(0));
        });
    }

    @Test
    public void permitDataForExcel_getMooselikeHarvestAmounts() {
        withMooselikeHarvestFixture(f -> {
            f.setMooseSpeciesAmounts(10, 9, 9);
            f.addMooseHarvests(Arrays.asList(GameAge.ADULT, GameAge.YOUNG));

            f.setDeerSpeciesAmounts(15, 14, 14);
            f.addDeerHarvests(Arrays.asList(GameAge.ADULT));

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> result = feature.permitDataForExcel(null, f.moose.getOfficialCode(), currentYear());
                assertThat(result, hasSize(1));

                final JhtArchiveExcelDTO actual = result.get(0);
                assertThatDataMatch(actual, f.permit, f.moose, f.rka, f.rhy);
                assertThatApplicationAmountsMatch(actual, 10f, null, null, null);
                assertThatPermitAmountsMatch(actual, 9f, null, null, null);
                assertThatHarvestAmountsMatch(actual, 2f, null, null, null);
            });
        });
    }

    @Test
    public void permitDataForExcel_getMooselikeSummaryAmounts() {
        withMooselikeHarvestFixture(f -> {
            f.setMooseSpeciesAmounts(10f, 9f, 9f);
            f.addMooseModeratorOverrideValues(5, 4, 3, 2, 1, 2);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> result = feature.permitDataForExcel(null, f.moose.getOfficialCode(), currentYear());
                assertThat(result, hasSize(1));

                final JhtArchiveExcelDTO actual = result.get(0);
                assertThatDataMatch(actual, f.permit, f.moose, f.rka, f.rhy);
                assertThatApplicationAmountsMatch(actual, 10f, null, null, null);
                assertThatPermitAmountsMatch(actual, 9f, null, null, null);
                assertThatHarvestAmountsMatch(actual, 14f, null, null, null);
            });
        });

    }

    @Test
    public void permitDataForExcel_getNestRemovalAmounts() {
        final EntitySupplier es = model();
        final GameSpecies species = es.newGameSpecies();
        withPermitCategory(species, HarvestPermitCategory.NEST_REMOVAL, f -> {
            f.applicationAmount.setSpecimenAmount(null);
            f.applicationAmount.setEggAmount(10);
            f.applicationAmount.setNestAmount(11);
            f.applicationAmount.setConstructionAmount(12);

            f.decisionAmount.setSpecimenAmount(null);
            f.decisionAmount.setEggAmount(9);
            f.decisionAmount.setNestAmount(8);
            f.decisionAmount.setConstructionAmount(7);

            f.permitAmount.setSpecimenAmount(null);
            f.permitAmount.setEggAmount(9);
            f.permitAmount.setNestAmount(8);
            f.permitAmount.setConstructionAmount(7);

            es.newHarvestPermitNestRemovalUsage(f.permitAmount, 6, 5, 4,
                                                new GeoLocation(123456, 654321, GeoLocation.Source.MANUAL),
                                                HarvestPermitNestLocationType.NEST);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> results = feature.permitDataForExcel(null, null, currentYear());
                assertThat(results, hasSize(1));

                final JhtArchiveExcelDTO actual = results.get(0);
                assertThatDataMatch(actual, f.permit, species, f.rka, f.rhy);
                assertThatApplicationAmountsMatch(actual, null, 10, 11, 12);
                assertThatPermitAmountsMatch(actual, null, 9, 8, 7);
                assertThatHarvestAmountsMatch(actual, null, 5, 6, 4);
           });
        });
    }

    @Theory
    public void permitDataForExcel_getDerogationAmounts(final HarvestPermitCategory category) {
        final EntitySupplier es = model();
        final GameSpecies species = es.newGameSpecies();
        final List<HarvestPermitCategory> validCategories = Arrays.asList(
                HarvestPermitCategory.MOOSELIKE,
                HarvestPermitCategory.MOOSELIKE_NEW,
                HarvestPermitCategory.BIRD,
                HarvestPermitCategory.LARGE_CARNIVORE_BEAR,
                HarvestPermitCategory.LARGE_CARNIVORE_LYNX,
                HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO,
                HarvestPermitCategory.LARGE_CARNIVORE_WOLF,
                HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO,
                HarvestPermitCategory.MAMMAL,
                HarvestPermitCategory.NEST_REMOVAL,
                HarvestPermitCategory.LAW_SECTION_TEN,
                HarvestPermitCategory.IMPORTING,
                HarvestPermitCategory.DEPORTATION,
                HarvestPermitCategory.RESEARCH,
                HarvestPermitCategory.GAME_MANAGEMENT);

        withPermitCategory(species, category, f -> {

            f.applicationAmount.setSpecimenAmount(10f);
            f.decisionAmount.setSpecimenAmount(9f);
            f.permitAmount.setSpecimenAmount(8f);

            createAcceptedHarvest(f.permit, species, 1);
            createAcceptedHarvest(f.permit, species, 1);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> results = feature.permitDataForExcel(null, null, currentYear());
                if (validCategories.contains(category)) {
                    assertThat(results, hasSize(1));

                    final JhtArchiveExcelDTO actual = results.get(0);
                    assertThatDataMatch(actual, f.permit, species, f.rka, f.rhy);
                    assertThatApplicationAmountsMatch(actual, 10f, null, null, null);
                    assertThatPermitAmountsMatch(actual, 8f, null, null, null);
                    assertThatHarvestAmountsMatch(actual, 2f, null, null, null);
                } else {
                    assertThat(results, hasSize(0));
                }
            });
        });
    }

    @Test
    public void permitDataForExcel_queriesAreNonLinear() {
        final int NUMBER_OF_PERMITS = 100;
        final int NUMBER_OF_QUERIES = 9;

        final EntitySupplier es = model();
        final List<GameSpecies> species = Stream.range(0, 10).map((i) -> es.newGameSpecies()).collect(toList());
        final List<RiistakeskuksenAlue> rkas = Stream.range(0, 10).map((i) -> es.newRiistakeskuksenAlue()).collect(toList());
        final List<HarvestPermitCategory> categories = Arrays.asList(
                HarvestPermitCategory.BIRD,
                HarvestPermitCategory.MOOSELIKE,
                HarvestPermitCategory.GAME_MANAGEMENT,
                HarvestPermitCategory.LARGE_CARNIVORE_WOLF,
                HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO);

        Stream.range(0, NUMBER_OF_PERMITS).forEach((i) -> {
            final RiistakeskuksenAlue rka = some(rkas);
            final Riistanhoitoyhdistys rhy = es.newRiistanhoitoyhdistys(rka);
            final HuntingClub club = es.newHuntingClub(rhy);
            new DerogationFixture(es, rka, rhy, club, some(species), some(categories));
        });

        assertMaxQueryCount(NUMBER_OF_QUERIES, () -> {
            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<JhtArchiveExcelDTO> results = feature.permitDataForExcel(null, null, currentYear());
                assertThat(results, hasSize(NUMBER_OF_PERMITS));
            });
        });
    }

    /**
     * searchParametersAsString tests
     */

    @Test(expected = AccessDeniedException.class)
    public void searchParametersAsString_userHasNoAccess() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.searchParametersAsString(null, null, null, 2021);
        });
    }

    @Test
    public void searchParametersAsString_moderatorHasAccess() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.searchParametersAsString(null, null, null, 2021);
        });
    }

    @Test
    public void searchParametersAsString_adminHasAccess() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.searchParametersAsString(null, null, null, 2021);
        });
    }

    @Test
    public void searchParametersAsString_onlyCalendarYearGiven() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final String result = feature.searchParametersAsString(localiser, null, null, 2021);
            assertThat(result, equalTo("_2021"));
        });
    }

    @Test
    public void searchParametersAsString_speciesAndCalendarYearGiven() {
        model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE, GameCategory.GAME_MAMMAL, "hirvi", "älk", "moose");
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final String result = feature.searchParametersAsString(localiser, null, GameSpecies.OFFICIAL_CODE_MOOSE, 2021);
            assertThat(result, equalTo("_hirvi_2021"));
        });
    }

    @Test
    public void searchParametersAsString_permitTypeAndCalendarYearGiven() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final String result = feature.searchParametersAsString(localiser, "100", null, 2021);
            assertThat(result, equalTo("_100_2021"));
        });
    }

    @Test
    public void searchParametersAsString_permitTypeAndSpeciesAndCalendarYearGiven() {
        model().newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE, GameCategory.GAME_MAMMAL, "hirvi", "älk", "moose");
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final String result = feature.searchParametersAsString(localiser, "100", GameSpecies.OFFICIAL_CODE_MOOSE, 2021);
            assertThat(result, equalTo("_100_hirvi_2021"));
        });
    }

    /**
     *
     * Helper functions
     *
     */

    private void createAcceptedHarvest(final HarvestPermit permit,
                                       final GameSpecies species,
                                       final int amount) {
        final EntitySupplier es = model();
        final Person reportAuthor = es.newPerson();
        final Harvest harvest = es.newHarvest(permit, species);
        harvest.setAmount(amount);
        harvest.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportDate(DateTime.now());
        harvest.setHarvestReportAuthor(reportAuthor);

    }

    private void assertThatApplicationAmountsMatch(final JhtArchiveExcelDTO actual,
                                                   final Float expectedSpecimenAmount,
                                                   final Integer expectedEggAmount,
                                                   final Integer expectedNestAmount,
                                                   final Integer expectedConstructionAmount) {
        assertThat(actual.getApplicationSpecimenAmount(), equalTo(expectedSpecimenAmount));
        assertThat(actual.getApplicationEggAmount(), equalTo(expectedEggAmount));
        assertThat(actual.getApplicationNestAmount(), equalTo(expectedNestAmount));
        assertThat(actual.getApplicationConstructionAmount(), equalTo(expectedConstructionAmount));
    }

    private void assertThatPermitAmountsMatch(final JhtArchiveExcelDTO actual,
                                              final Float expectedSpecimenAmount,
                                              final Integer expectedEggAmount,
                                              final Integer expectedNestAmount,
                                              final Integer expectedConstructionAmount) {
        assertThat(actual.getPermitSpecimenAmount(), equalTo(expectedSpecimenAmount));
        assertThat(actual.getPermitEggAmount(), equalTo(expectedEggAmount));
        assertThat(actual.getPermitNestAmount(), equalTo(expectedNestAmount));
        assertThat(actual.getPermitConstructionAmount(), equalTo(expectedConstructionAmount));
    }

    private void assertThatHarvestAmountsMatch(final JhtArchiveExcelDTO actual,
                                               final Float expectedSpecimenAmount,
                                               final Integer expectedEggAmount,
                                               final Integer expectedNestAmount,
                                               final Integer expectedConstructionAmount) {
        assertThat(actual.getHarvestSpecimenAmount(), equalTo(expectedSpecimenAmount));
        assertThat(actual.getHarvestEggAmount(), equalTo(expectedEggAmount));
        assertThat(actual.getHarvestNestAmount(), equalTo(expectedNestAmount));
        assertThat(actual.getHarvestConstructionAmount(), equalTo(expectedConstructionAmount));
    }

    private void assertThatSpeciesMatch(final JhtArchiveExcelDTO actual,
                                        final GameSpecies expected) {
        assertThat(actual.getSpecies().getTranslation(FI), equalTo(expected.getNameFinnish()));
    }

    private void assertThatPermitMatch(final JhtArchiveExcelDTO actual,
                                       final HarvestPermit expected) {
        assertThat(actual.getPermitType().getTranslation(FI),
                   equalTo(PermitTypeCode.getDecisionName(expected.getPermitTypeCode()).getTranslation(FI)));
        assertThat(actual.getPermitNumber(), equalTo(expected.getPermitNumber()));
    }

    private void assertThatOrgMatch(final JhtArchiveExcelDTO actual,
                                    final RiistakeskuksenAlue expectedRka,
                                    final Riistanhoitoyhdistys expectedRhy) {
        assertThat(actual.getRka().getTranslation(FI), equalTo(expectedRka.getNameFinnish()));
        assertThat(actual.getRhy().getTranslation(FI), equalTo(expectedRhy.getNameFinnish()));

    }

    private void assertThatDataMatch(final JhtArchiveExcelDTO actual,
                                     final HarvestPermit expectedPermit,
                                     final GameSpecies expectedSpecies,
                                     final RiistakeskuksenAlue expectedRka,
                                     final Riistanhoitoyhdistys expectedRhy) {
        assertThatSpeciesMatch(actual, expectedSpecies);
        assertThatPermitMatch(actual, expectedPermit);
        assertThatOrgMatch(actual, expectedRka, expectedRhy);
    }

}