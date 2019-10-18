package fi.riista.feature.harvestpermit.statistics;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

public class MoosePermitStatisticsFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MoosePermitStatisticsFeature feature;

    private int dayCounter = 0;

    final int permitAreaSize1 = 10000;
    final int permitAreaSize2 = 5000;

    final int effectiveAreaSize1 = permitAreaSize1;
    final int effectiveAreaSize2 = permitAreaSize2 / 2;

    final int remainingPopulationInTotalArea = 20;
    final int remainingPopulationInEffectiveArea = 10;

    @Test
    public void testCalculate_lockedStatus() {
        withRhy(rhy -> {

            final GameSpecies species = model().newGameSpecies(false);

            withHuntingGroupFixture(rhy, species, f1 -> withHuntingGroupFixture(rhy, species, f2 -> {

                model().newBasicHuntingSummary(f1.speciesAmount, f1.club, true);
                model().newBasicHuntingSummary(f2.speciesAmount, f2.club, true);

                // Hunting finished for the first permit.
                f1.speciesAmount.setMooselikeHuntingFinished(true);

                onSavedAndAuthenticated(createNewModerator(), () -> {

                    final int speciesCode = species.getOfficialCode();
                    final int huntingYear = f1.speciesAmount.resolveHuntingYear();

                    final List<MoosePermitStatisticsDTO> stats = feature.calculate(
                            Locales.FI, MoosePermitStatisticsReportType.BY_PERMIT,
                            MoosePermitStatisticsGroupBy.RHY_PERMIT, true, speciesCode, huntingYear,
                            MoosePermitStatisticsOrganisationType.RHY, rhy.getOfficialCode());

                    // Total stats included in the count.
                    assertEquals(3, stats.size());

                    final Map<String, Boolean> transformedResult = stats.stream()
                            .filter(stat -> stat.getPermitNumber() != null)
                            .collect(toMap(
                                    MoosePermitStatisticsDTO::getPermitNumber,
                                    dto -> dto.getPermitAmount().isMooselikeHuntingFinished()));

                    assertEquals(
                            ImmutableMap.of(f1.permit.getPermitNumber(), true, f2.permit.getPermitNumber(), false),
                            transformedResult);
                });
            }));
        });
    }

    @Test
    public void testCalculate_onlyHarvests() {
        withMooseHuntingGroupFixture(fixture -> {
            fixture.permit.setPermitAreaSize(permitAreaSize1);
            fixture.speciesAmount.setAmount(100f);
            createHarvests(fixture);
            persistInNewTransaction();

            // these area sizes should be capped to permitAreaSize1
            createSummary(fixture, permitAreaSize1 * 3, permitAreaSize1 * 2);

            withHuntingGroupFixture(fixture.rhy, fixture.species, fixture2 -> {
                fixture2.permit.setPermitAreaSize(permitAreaSize2);
                fixture2.speciesAmount.setAmount(100f);
                createHarvests(fixture2);
                persistInNewTransaction();
                createSummary(fixture2, permitAreaSize2 - 10, effectiveAreaSize2);
            });

            persistInNewTransaction();
            authenticate(createNewModerator());

            final List<MoosePermitStatisticsDTO> stats = feature.calculate(
                    Locales.FI, MoosePermitStatisticsReportType.BY_PERMIT,
                    MoosePermitStatisticsGroupBy.RHY_PERMIT, true,
                    fixture.species.getOfficialCode(), fixture.speciesAmount.resolveHuntingYear(),
                    MoosePermitStatisticsOrganisationType.RHY, fixture.rhy.getOfficialCode());

            assertEquals(3, stats.size());

            final MoosePermitStatisticsDTO total = stats.get(0);
            assertEquals(100F * 2, total.getPermitAmount().getTotal(), 0);

            assertEquals(17 * 2, total.getHarvestCount().getTotal());
            assertEquals(1 * 2, total.getHarvestCount().getNumberOfAdultMales());
            assertEquals(2 * 2, total.getHarvestCount().getNumberOfAdultFemales());
            assertEquals(6 * 2, total.getHarvestCount().getNumberOfYoungMales());
            assertEquals(8 * 2, total.getHarvestCount().getNumberOfYoungFemales());

            assertEquals(1 / 3.0 * 100, total.getHarvestCount().getAdultMalePercentage(), 0.001);
            assertEquals(14 / 17.0 * 100, total.getHarvestCount().getYoungPercentage(), 0.001);

            assertEquals(permitAreaSize1 + permitAreaSize2, total.getAreaAndPopulation().getTotalAreaSize());

            double expectedInTotalPer1000ha = remainingPopulationInTotalArea * 2 / ((permitAreaSize1 + permitAreaSize2) / 1000.0);
            assertEquals(expectedInTotalPer1000ha, total.getAreaAndPopulation().getRemainingPopulationInTotalAreaPer1000ha(), 0.001);

            double expectedInEffectivePer1000ha = remainingPopulationInEffectiveArea * 2 / ((effectiveAreaSize1 + effectiveAreaSize2) / 1000.0);
            assertEquals(expectedInEffectivePer1000ha, total.getAreaAndPopulation().getRemainingPopulationInEffectiveAreaPer1000ha(), 0.001);

            assertEquals(permitAreaSize1, stats.get(1).getAreaAndPopulation().getTotalAreaSize());
            assertEquals(permitAreaSize2, stats.get(2).getAreaAndPopulation().getTotalAreaSize());
        });

    }

    private void createSummary(final HuntingGroupFixture fixture,
                               final Integer totalArea,
                               final Integer effectiveArea) {
        final MooseHuntingSummary summary = model().newMooseHuntingSummary(fixture.permit, fixture.club, true);
        summary.setAreaSizeAndPopulation(new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(totalArea)
                .withEffectiveHuntingArea(effectiveArea)
                .withRemainingPopulationInTotalArea(remainingPopulationInTotalArea)
                .withRemainingPopulationInEffectiveArea(remainingPopulationInEffectiveArea));
    }

    private void createHarvests(final HuntingGroupFixture fixture) {
        final int year = fixture.speciesAmount.resolveHuntingYear();
        createHarvests(fixture.group, fixture.species, 1, ADULT, MALE, year);
        createHarvests(fixture.group, fixture.species, 2, ADULT, FEMALE, year);
        createHarvests(fixture.group, fixture.species, 6, YOUNG, MALE, year);
        createHarvests(fixture.group, fixture.species, 8, YOUNG, FEMALE, year);
    }

    private void createHarvests(final HuntingClubGroup group,
                                final GameSpecies species,
                                final int amount,
                                final GameAge age,
                                final GameGender gender,
                                final int year) {

        final LocalDate beginDate = DateUtil.huntingYearBeginDate(year);
        final GroupHuntingDay day = model().newGroupHuntingDay(group, beginDate.plusDays(dayCounter++));
        for (int i = 0; i < amount; i++) {
            final Harvest harvest = model().newHarvest(species);
            harvest.updateHuntingDayOfGroup(day, null);
            model().newHarvestSpecimen(harvest, age, gender);
        }
    }
}
