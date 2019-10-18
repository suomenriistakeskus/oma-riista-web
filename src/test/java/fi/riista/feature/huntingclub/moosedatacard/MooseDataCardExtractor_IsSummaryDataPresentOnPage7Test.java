package fi.riista.feature.huntingclub.moosedatacard;

import com.google.common.collect.ObjectArrays;
import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.TrendOfPopulationGrowth;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MooseDataCardExtractor_IsSummaryDataPresentOnPage7Test {

    private static final int[] SPECIES_CODES = {
            OFFICIAL_CODE_FALLOW_DEER, OFFICIAL_CODE_ROE_DEER, OFFICIAL_CODE_WHITE_TAILED_DEER, OFFICIAL_CODE_WILD_BOAR,
            OFFICIAL_CODE_WILD_FOREST_REINDEER
    };

    @Parameters(name = "{index}: speciesCode={0}; appearance={1}, trend={2}, amount={3}, amountOfSowsWithPiglets={4}")
    public static Iterable<Object[]> data() {
        final MooseDataCardGameSpeciesAppearance[] appearanceOptions =
                ObjectArrays.<MooseDataCardGameSpeciesAppearance> concat(null, MooseDataCardGameSpeciesAppearance.values());

        final String[] trendOptions = {
                TrendOfPopulationGrowth.DECREASED.getMooseDataCardEncoding(),
                TrendOfPopulationGrowth.INCREASED.getMooseDataCardEncoding(),
                TrendOfPopulationGrowth.UNCHANGED.getMooseDataCardEncoding(),
                "INVALID",
                null
        };

        return IntStream.of(SPECIES_CODES).boxed()
                .flatMap(speciesCode -> {
                    return Arrays.stream(appearanceOptions)
                            .flatMap(appearance -> {
                                return Arrays.stream(trendOptions)
                                        .flatMap(trend -> {
                                            return Stream.of(null, 100)
                                                    .flatMap(amount -> Stream.of(null, 50)
                                                            .map(numPiglets -> new Object[] {
                                                                    speciesCode, appearance, trend, amount, numPiglets
                                            }));
                                        });
                            });
                })
                .collect(toList());
    }

    @Parameter(0)
    public int gameSpeciesCode;

    @Parameter(1)
    public MooseDataCardGameSpeciesAppearance appearance;

    @Parameter(2)
    public String trend;

    @Parameter(3)
    public Integer amount;

    @Parameter(4)
    public Integer amountOfSowsWithPiglets;

    private MooseDataCardPage7 page7;

    @Before
    public void setup() {
        page7 = new MooseDataCardPage7();

        switch (gameSpeciesCode) {
            case OFFICIAL_CODE_FALLOW_DEER:
                page7.setFallowDeerAppeared(appearance);
                page7.setTrendOfFallowDeerPopulationGrowth(trend);
                page7.setEstimatedSpecimenAmountOfFallowDeer(amount);
                break;
            case OFFICIAL_CODE_ROE_DEER:
                page7.setRoeDeerAppeared(appearance);
                page7.setTrendOfRoeDeerPopulationGrowth(trend);
                page7.setEstimatedSpecimenAmountOfRoeDeer(amount);
                break;
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                page7.setWhiteTailedDeerAppeared(appearance);
                page7.setTrendOfWhiteTailedDeerPopulationGrowth(trend);
                page7.setEstimatedSpecimenAmountOfWhiteTailedDeer(amount);
                break;
            case OFFICIAL_CODE_WILD_BOAR:
                page7.setWildBoarAppeared(appearance);
                page7.setTrendOfWildBoarPopulationGrowth(trend);
                page7.setEstimatedSpecimenAmountOfWildBoar(amount);
                break;
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                page7.setWildForestReindeerAppeared(appearance);
                page7.setTrendOfWildForestReindeerPopulationGrowth(trend);
                page7.setEstimatedSpecimenAmountOfWildForestReindeer(amount);
                break;
            default:
                throw new IllegalStateException("Unexpected game species code");
        }

        page7.setEstimatedAmountOfSowsWithPiglets(amountOfSowsWithPiglets);
    }

    @Test
    public void testIsSummaryDataPresent() {
        final boolean appearancePresent =
                appearance != null && appearance != MooseDataCardGameSpeciesAppearance.UNDEFINED;

        final boolean trendPresent =
                HasMooseDataCardEncoding.findEnum(TrendOfPopulationGrowth.class, trend).isPresent();

        final boolean amountPresent = amount != null;
        final boolean amountOfSowsWithPigletsPresent = amountOfSowsWithPiglets != null;

        final boolean expected = appearancePresent || trendPresent || amountPresent || amountOfSowsWithPigletsPresent;

        assertEquals(expected, MooseDataCardExtractor.isSummaryDataPresent(page7));
    }
}
