package fi.riista.feature.huntingclub.moosedatacard.validation;

import static javaslang.control.Validation.valid;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.huntingclub.permit.summary.TrendOfPopulationGrowth;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;

import javaslang.control.Validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MooseDataCardPage7MooselikeValidator {

    @Nonnull
    public static Validation<List<String>, MooseDataCardPage7> validate(@Nonnull final MooseDataCardPage7 page7) {
        Objects.requireNonNull(page7);

        final MooseDataCardPage7 result = page7.createCopy();

        if (result.getWhiteTailedDeerAppeared() != MooseDataCardGameSpeciesAppearance.NO) {

            result.setEstimatedSpecimenAmountOfWhiteTailedDeer(
                    MooseDataCardSummaryField.ESTIMATED_AMOUNT_OF_WHITE_TAILED_DEERS.getValidOrNull(page7));

            result.setTrendOfWhiteTailedDeerPopulationGrowth(
                    getValidTrendOfPopulationGrowth(page7.getTrendOfWhiteTailedDeerPopulationGrowth()));

        } else {
            result.setEstimatedSpecimenAmountOfWhiteTailedDeer(null);
            result.setTrendOfWhiteTailedDeerPopulationGrowth(null);
        }

        if (result.getRoeDeerAppeared() != MooseDataCardGameSpeciesAppearance.NO) {

            result.setEstimatedSpecimenAmountOfRoeDeer(
                    MooseDataCardSummaryField.ESTIMATED_AMOUNT_OF_ROE_DEERS.getValidOrNull(page7));

            result.setTrendOfRoeDeerPopulationGrowth(
                    getValidTrendOfPopulationGrowth(page7.getTrendOfRoeDeerPopulationGrowth()));

        } else {
            result.setEstimatedSpecimenAmountOfRoeDeer(null);
            result.setTrendOfRoeDeerPopulationGrowth(null);
        }

        if (result.getWildForestReindeerAppeared() != MooseDataCardGameSpeciesAppearance.NO) {

            result.setEstimatedSpecimenAmountOfWildForestReindeer(
                    MooseDataCardSummaryField.ESTIMATED_AMOUNT_OF_WILD_FOREST_REINDEERS.getValidOrNull(page7));

            result.setTrendOfWildForestReindeerPopulationGrowth(
                    getValidTrendOfPopulationGrowth(page7.getTrendOfWildForestReindeerPopulationGrowth()));

        } else {
            result.setEstimatedSpecimenAmountOfWildForestReindeer(null);
            result.setTrendOfWildForestReindeerPopulationGrowth(null);
        }

        if (result.getFallowDeerAppeared() != MooseDataCardGameSpeciesAppearance.NO) {

            result.setEstimatedSpecimenAmountOfFallowDeer(
                    MooseDataCardSummaryField.ESTIMATED_AMOUNT_OF_FALLOW_DEERS.getValidOrNull(page7));

            result.setTrendOfFallowDeerPopulationGrowth(
                    getValidTrendOfPopulationGrowth(page7.getTrendOfFallowDeerPopulationGrowth()));

        } else {
            result.setEstimatedSpecimenAmountOfFallowDeer(null);
            result.setTrendOfFallowDeerPopulationGrowth(null);
        }

        return valid(result);
    }

    @Nullable
    private static String getValidTrendOfPopulationGrowth(@Nullable final String value) {
        return convertTrendOfPopulationGrowth(value)
                .map(TrendOfPopulationGrowth::getMooseDataCardEncoding)
                .orElse(null);
    }

    @Nonnull
    private static Optional<TrendOfPopulationGrowth> convertTrendOfPopulationGrowth(@Nullable final String value) {
        return HasMooseDataCardEncoding.enumOf(TrendOfPopulationGrowth.class, value)
                .fold(invalid -> Optional.empty(), Optional::of);
    }

}
