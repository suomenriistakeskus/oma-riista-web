package fi.riista.feature.organization.rhy.subsidy;

import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.rhy.MergedRhyMappingTestHelper;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsTestDataPopulator;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfo;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.feature.organization.rhy.MergedRhyMapping.NEW_TAMPERE_382;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TAMPERE_376;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.OLD_TEISKO_378;
import static fi.riista.feature.organization.rhy.MergedRhyMapping.RhyMerge.create;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.currentYear;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class RhySubsidyAllocationFeatureTest
        extends EmbeddedDatabaseTest
        implements RhyAnnualStatisticsTestDataPopulator, OrganisationFixtureMixin {

    @Resource
    private RhySubsidyAllocationFeature feature;

    @After
    public void tearDown() {
        MergedRhyMappingTestHelper.reset();
    }

    @Test
    public void testCalculateSubsidyAllocations_2020() {
        final int year = 2020;
        final SubsidyAllocationInputDTO dto = new SubsidyAllocationInputDTO();
        dto.setSubsidyYear(year);
        dto.setTotalSubsidyAmount(new BigDecimal("1000"));
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        MergedRhyMappingTestHelper.assignMerges(emptyList());

        withRhy("007", rka, rhy -> {
            model().newRhySubsidy(rhy, 2019, 500, 500);
            final RhyAnnualStatistics annualStatistics = populateStatistics(rhy, year - 1, 5);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final AllSubsidyAllocationInfoDTO allocationInfoDTO = feature.calculateSubsidyAllocations(dto);

                assertThat(allocationInfoDTO.getCalculatedRhyAllocations(), hasSize(1));
                final RhySubsidyStage5DTO rhySubsidyStage5DTO =
                        allocationInfoDTO.getCalculatedRhyAllocations().get(0);

                assertThat(rhySubsidyStage5DTO.getCalculation().getSubsidyOfBatch1().intValue(), equalTo(500));
                assertThat(rhySubsidyStage5DTO.getCalculation().getSubsidyOfBatch2().intValue(), equalTo(500));
                assertMemberSumsMatch(rhySubsidyStage5DTO.getCalculation().getCalculatedShares(), annualStatistics);
            });
        });
    }

    @Test
    public void testCalculateSubsidyAllocations_2021() {
        final int year = 2021;
        testWithCombinedTwoYearStatistics(year);
    }

    @Test
    public void testCalculateSubsidyAllocations_nextYear() {
        final int subsidyYear = currentYear() + 1;
        testWithCombinedTwoYearStatistics(subsidyYear);
    }

    @Test
    public void testCalculateSubsidyAllocations_2021_rhyMergeOn2020() {
        final int year = 2021;
        final SubsidyAllocationInputDTO dto = new SubsidyAllocationInputDTO();
        dto.setSubsidyYear(year);
        dto.setTotalSubsidyAmount(new BigDecimal("20000"));
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        MergedRhyMappingTestHelper.assignMerges(asList(
                create(2020, OLD_TAMPERE_376, NEW_TAMPERE_382),
                create(2020, OLD_TEISKO_378, NEW_TAMPERE_382)));

        withRhy(OLD_TAMPERE_376, rka, oldTampere -> {
            final RhyAnnualStatistics oldTampereStats2019 = populateStatistics(oldTampere, 2019, 5);

            withRhy(OLD_TEISKO_378, rka, oldTeisko -> {
                final RhyAnnualStatistics oldTeiskoStats2019 = populateStatistics(oldTeisko, 2019, 15);

                withRhy(NEW_TAMPERE_382, rka, newTampere -> {
                    model().newRhySubsidy(newTampere, 2020, 1000, 1000);
                    final RhyAnnualStatistics newTampereStats = populateStatistics(newTampere, 2020, 1);

                    onSavedAndAuthenticated(createNewModerator(), () -> {
                        final AllSubsidyAllocationInfoDTO allocationInfoDTO = feature.calculateSubsidyAllocations(dto);
                        assertThat(allocationInfoDTO.getCalculatedRhyAllocations(), hasSize(1));

                        final RhySubsidyStage5DTO subsidyStage5DTO = allocationInfoDTO.getCalculatedRhyAllocations().get(0);
                        final SubsidyCalculationStage5DTO calculation = subsidyStage5DTO.getCalculation();
                        final StatisticsBasedSubsidyShareDTO shares = calculation.getCalculatedShares();

                        assertThat(subsidyStage5DTO.getRhy().getOfficialCode(), equalTo(newTampere.getOfficialCode()));
                        assertThat(calculation.getSubsidyOfBatch1().intValue(), equalTo(10000));
                        assertThat(calculation.getSubsidyOfBatch2().intValue(), equalTo(10000));
                        assertMemberSumsMatch(shares, oldTampereStats2019, oldTeiskoStats2019, newTampereStats);

                    });
                });
            });
        });
    }

    @Test
    public void testCalculateSubsidyAllocations_2021_rhyMergeOn2021() {
        final int year = 2021;
        final SubsidyAllocationInputDTO dto = new SubsidyAllocationInputDTO();
        dto.setSubsidyYear(year);
        dto.setTotalSubsidyAmount(new BigDecimal("20000"));
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        MergedRhyMappingTestHelper.assignMerges(asList(
                create(2021, "001", "003"),
                create(2021, "002", "003")));

        withRhy("001", rka, ts1 -> {
            model().newRhySubsidy(ts1, 2020, 9000, 9000);
            final RhyAnnualStatistics ts1Stats2020 = populateStatistics(ts1, 2020, 5);
            final RhyAnnualStatistics ts1Stats2019 = populateStatistics(ts1, 2019, 6);

            withRhy("002", rka, ts2 -> {
                model().newRhySubsidy(ts2, 2020, 1000, 1000);
                final RhyAnnualStatistics ts2Stats2020 = populateStatistics(ts2, 2020, 15);
                final RhyAnnualStatistics ts2Stats2019 = populateStatistics(ts2, 2019, 16);

                withRhy("003", rka, ts3 ->
                        onSavedAndAuthenticated(createNewModerator(), () -> {
                            final AllSubsidyAllocationInfoDTO allocationInfoDTO =
                                    feature.calculateSubsidyAllocations(dto);
                            assertThat(allocationInfoDTO.getCalculatedRhyAllocations(), hasSize(1));

                            final RhySubsidyStage5DTO subsidyStage5DTO =
                                    allocationInfoDTO.getCalculatedRhyAllocations().get(0);
                            final SubsidyCalculationStage5DTO calculation = subsidyStage5DTO.getCalculation();
                            final StatisticsBasedSubsidyShareDTO shares = calculation.getCalculatedShares();

                            assertThat(subsidyStage5DTO.getRhy().getOfficialCode(), equalTo(ts3.getOfficialCode()));
                            assertThat(calculation.getSubsidyOfBatch1().intValue(), equalTo(10000));
                            assertThat(calculation.getSubsidyOfBatch2().intValue(), equalTo(10000));
                            assertMemberSumsMatch(shares, ts1Stats2020, ts1Stats2019, ts2Stats2020, ts2Stats2019);

                        }));
            });
        });
    }

    @Test
    public void testCalculateSubsidyAllocations_2021_rhyMergeOn2020And2021() {
        final int year = 2021;
        final SubsidyAllocationInputDTO dto = new SubsidyAllocationInputDTO();
        dto.setSubsidyYear(year);
        dto.setTotalSubsidyAmount(new BigDecimal("20000"));
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        //     2020           2021
        //  A01--+-B01---------+--C01
        //  A02-/             /
        //  B02--------------/
        MergedRhyMappingTestHelper.assignMerges(asList(
                create(2020, "A01", "B01"),
                create(2020, "A02", "B01"),
                create(2021, "B01", "C01"),
                create(2021, "B02", "C01")));

        withRhy("A01", rka, a01 -> {
            final RhyAnnualStatistics a01Stats2019 = populateStatistics(a01, 2019, 5);

            withRhy("A02", rka, a02 -> {
                final RhyAnnualStatistics a02Stats2019 = populateStatistics(a02, 2019, 10);

                withRhy("B01", rka, b01 -> {
                    model().newRhySubsidy(b01, 2020, 100, 100);
                    final RhyAnnualStatistics b01Stats2020 = populateStatistics(b01, 2020, 20);

                    withRhy("B02", rka, b02 -> {
                        model().newRhySubsidy(b02, 2020, 100, 100);
                        final RhyAnnualStatistics b02Stats2019 = populateStatistics(b02, 2019, 50);
                        final RhyAnnualStatistics b02Stats2020 = populateStatistics(b02, 2020, 100);


                        withRhy("C01", rka, c01 ->
                                onSavedAndAuthenticated(createNewModerator(), () -> {

                                    final AllSubsidyAllocationInfoDTO allocationInfoDTO =
                                            feature.calculateSubsidyAllocations(dto);
                                    assertThat(allocationInfoDTO.getCalculatedRhyAllocations(), hasSize(1));

                                    final RhySubsidyStage5DTO subsidyStage5DTO =
                                            allocationInfoDTO.getCalculatedRhyAllocations().get(0);
                                    final SubsidyCalculationStage5DTO calculation = subsidyStage5DTO.getCalculation();
                                    final StatisticsBasedSubsidyShareDTO shares = calculation.getCalculatedShares();

                                    assertThat(subsidyStage5DTO.getRhy().getOfficialCode(),
                                            equalTo(c01.getOfficialCode()));
                                    assertThat(calculation.getSubsidyOfBatch1().intValue(), equalTo(10000));
                                    assertThat(calculation.getSubsidyOfBatch2().intValue(), equalTo(10000));
                                    assertMemberSumsMatch(shares,
                                            a01Stats2019, a02Stats2019, b01Stats2020, b02Stats2019, b02Stats2020);

                                }));
                    });
                });
            });
        });
    }

    private void testWithCombinedTwoYearStatistics(final int subsidyYear) {
        final SubsidyAllocationInputDTO dto = new SubsidyAllocationInputDTO();
        dto.setSubsidyYear(subsidyYear);
        dto.setTotalSubsidyAmount(new BigDecimal("20000"));
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        MergedRhyMappingTestHelper.assignMerges(emptyList());

        withRhy("rh1", rka, bigRhy -> {
            model().newRhySubsidy(bigRhy, subsidyYear - 1, 9000, 9000);
            final RhyAnnualStatistics rh1Stats = populateStatistics(bigRhy, subsidyYear - 1, 5);

            final RhyAnnualStatistics rh1Stats2 = populateStatistics(bigRhy, subsidyYear - 2, 15);

            withRhy("rh2", rka, rhy -> {
                model().newRhySubsidy(rhy, subsidyYear - 1, 1000, 1000);
                final RhyAnnualStatistics rh2Stats1 = populateStatistics(rhy, subsidyYear - 1, 1);
                final RhyAnnualStatistics rh2Stats2 = populateStatistics(rhy, subsidyYear - 2, 1);

                onSavedAndAuthenticated(createNewModerator(), () -> {
                    final AllSubsidyAllocationInfoDTO allocationInfoDTO = feature.calculateSubsidyAllocations(dto);
                    final Map<String, RhySubsidyStage5DTO> index =
                            F.index(allocationInfoDTO.getCalculatedRhyAllocations(),
                                    info -> info.getRhy().getOfficialCode());
                    assertThat(index.size(), equalTo(2));

                    final RhySubsidyStage5DTO bigRhySubsidy = index.get(bigRhy.getOfficialCode());
                    final RhySubsidyStage5DTO rhySubsidy = index.get(rhy.getOfficialCode());
                    assertThat(bigRhySubsidy, is(notNullValue()));
                    assertThat(rhySubsidy, is(notNullValue()));
                    final StatisticsBasedSubsidyShareDTO bigRhyShares =
                            bigRhySubsidy.getCalculation().getCalculatedShares();
                    final StatisticsBasedSubsidyShareDTO rhyShares = rhySubsidy.getCalculation().getCalculatedShares();

                    assertThat(bigRhyShares.getRhyMembers().getQuantity(),
                            equalTo(rh1Stats.getOrCreateBasicInfo().getRhyMembers() + rh1Stats2.getOrCreateBasicInfo().getRhyMembers()));
                    assertMemberSumsMatch(rhyShares, rh2Stats1, rh2Stats2);

                });
            });
        });
    }

    private RhyAnnualStatistics populateStatistics(final Riistanhoitoyhdistys rhy, final int i, final int i2) {
        final RhyAnnualStatistics statistics = model().newRhyAnnualStatistics(rhy, i);
        populateAllWithMatchingSubsidyTotalQuantities(statistics, i2);
        statistics.setState(APPROVED);
        return statistics;
    }

    private static void assertMemberSumsMatch(final StatisticsBasedSubsidyShareDTO shares,
                                              final RhyAnnualStatistics... stats) {
        final int members = Stream.of(stats)
                .map(RhyAnnualStatistics::getOrCreateBasicInfo)
                .map(RhyBasicInfo::getRhyMembers)
                .reduce(0, NumberUtils::nullableIntSum);

        assertThat(shares.getRhyMembers().getQuantity(), equalTo(members));
    }
}
