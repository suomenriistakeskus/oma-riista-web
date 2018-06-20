package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardPage7MooselikeValidator;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection81Validator;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection83Validator;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardSection84Validator;
import fi.riista.feature.huntingclub.permit.summary.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummaryRepository;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary_;
import fi.riista.feature.huntingclub.permit.summary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.summary.SpeciesEstimatedAppearanceWithPiglets;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertAppearance;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertTrendOfPopulationGrowth;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardExtractor.convertTrendOfPopulationGrowthOfFlyDeer;
import static fi.riista.util.jpa.JpaSpecs.equal;

@Component
public class MooseDataCardHuntingSummaryTransferer {

    @Resource
    private MooseHuntingSummaryRepository summaryRepo;

    // Return existing MooseHuntingSummary or newly-created one if any data was (available to be)
    // transferred.
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Try<Optional<MooseHuntingSummary>> upsertHuntingSummaryData(@Nonnull final MooseDataCard mooseDataCard,
                                                                       @Nonnull final HuntingClub club,
                                                                       @Nonnull final HarvestPermit permit) {

        Objects.requireNonNull(club, "club is null");
        Objects.requireNonNull(permit, "permit is null");

        final Specification<MooseHuntingSummary> summaryFindSpec = Specifications
                .where(equal(MooseHuntingSummary_.club, club))
                .and(equal(MooseHuntingSummary_.harvestPermit, permit));

        return Try.of(() -> summaryRepo.findOne(summaryFindSpec))
                .map(Optional::ofNullable)
                .filter(Optional::isPresent)
                .recover(noSuchElementEx -> {

                    // No existing MooseHuntingSummary found, create one if relevant data available.

                    return MooseDataCardExtractor.isSummaryDataPresent(mooseDataCard)
                            ? Optional.of(new MooseHuntingSummary(club, permit))
                            : Optional.empty();
                })
                .map(summaryOpt -> summaryOpt.map(summary -> {
                    transferSummaryData(mooseDataCard, summary);
                    return summary.isNew() ? summaryRepo.save(summary) : summary;
                }));
    }

    // Exposed publicly for isolated testing.
    public void transferSummaryData(@Nonnull final MooseDataCard mooseDataCard,
                                    @Nonnull final MooseHuntingSummary summary) {

        Objects.requireNonNull(mooseDataCard, "mooseDataCard is null");
        Objects.requireNonNull(summary, "summary is null");

        MooseDataCardExtractor.findFirstPage7ContainingHuntingSummaryData(mooseDataCard)
                .ifPresent(page7 -> transferMooselikeSummaryOfPage7(page7, summary));

        MooseDataCardExtractor.findFirstNonEmptyPage8(mooseDataCard)
                .ifPresent(page8 -> transferPage8(page8, summary));
    }

    private static void transferMooselikeSummaryOfPage7(final MooseDataCardPage7 page7,
                                                        final MooseHuntingSummary summary) {

        MooseDataCardPage7MooselikeValidator.validate(page7).peek(validPage7 -> {

            summary.setWhiteTailedDeerAppearance(constructSpeciesAppearance(
                    validPage7.getWhiteTailedDeerAppeared(),
                    validPage7.getTrendOfWhiteTailedDeerPopulationGrowth(),
                    validPage7.getEstimatedSpecimenAmountOfWhiteTailedDeer()));

            summary.setRoeDeerAppearance(constructSpeciesAppearance(
                    validPage7.getRoeDeerAppeared(),
                    validPage7.getTrendOfRoeDeerPopulationGrowth(),
                    validPage7.getEstimatedSpecimenAmountOfRoeDeer()));

            summary.setWildForestReindeerAppearance(constructSpeciesAppearance(
                    validPage7.getWildForestReindeerAppeared(),
                    validPage7.getTrendOfWildForestReindeerPopulationGrowth(),
                    validPage7.getEstimatedSpecimenAmountOfWildForestReindeer()));

            summary.setFallowDeerAppearance(constructSpeciesAppearance(
                    validPage7.getFallowDeerAppeared(),
                    validPage7.getTrendOfFallowDeerPopulationGrowth(),
                    validPage7.getEstimatedSpecimenAmountOfFallowDeer()));

            summary.setWildBoarAppearance(new SpeciesEstimatedAppearanceWithPiglets(
                    convertAppearance(validPage7.getWildBoarAppeared()),
                    convertTrendOfPopulationGrowth(validPage7.getTrendOfWildBoarPopulationGrowth()),
                    validPage7.getEstimatedSpecimenAmountOfWildBoar(),
                    validPage7.getEstimatedAmountOfSowsWithPiglets()));
        });
    }

    private static SpeciesEstimatedAppearance constructSpeciesAppearance(
            final MooseDataCardGameSpeciesAppearance speciesAppearance,
            final String trend,
            final Integer estimatedAmountOfSpecimens) {

        return new SpeciesEstimatedAppearance(
                convertAppearance(speciesAppearance),
                convertTrendOfPopulationGrowth(trend),
                estimatedAmountOfSpecimens);
    }

    private static void transferPage8(@Nonnull final MooseDataCardPage8 page8,
                                      @Nonnull final MooseHuntingSummary summary) {

        Stream.of(page8.getSection_8_1())
                .filter(Objects::nonNull)
                .map(MooseDataCardSection81Validator::validate)
                .filter(Validation::isValid)
                .map(Validation::get)
                .forEach(validSection81 -> transferSection(validSection81, summary));

        Stream.of(page8.getSection_8_3())
                .filter(Objects::nonNull)
                .map(MooseDataCardSection83Validator::validate)
                .filter(Validation::isValid)
                .map(Validation::get)
                .forEach(validSection83 -> transferSection(validSection83, summary));

        Stream.of(page8.getSection_8_4())
                .filter(Objects::nonNull)
                .map(MooseDataCardSection84Validator::validate)
                .filter(Validation::isValid)
                .map(Validation::get)
                .forEach(validSection84 -> transferSection(validSection84, summary));

        final LocalDate huntingEndDate = page8.getHuntingEndDate();
        summary.setHuntingEndDate(huntingEndDate);
        summary.setHuntingFinished(huntingEndDate != null);
    }

    private static void transferSection(@Nonnull final MooseDataCardSection_8_1 input,
                                        @Nonnull final MooseHuntingSummary summary) {

        final AreaSizeAndRemainingPopulation areaAndPopulation = new AreaSizeAndRemainingPopulation()
                .withTotalHuntingArea(
                        Optional.ofNullable(input.getTotalHuntingArea()).map(Number::intValue).orElse(null))
                .withEffectiveHuntingArea(
                        Optional.ofNullable(input.getEffectiveHuntingArea()).map(Number::intValue).orElse(null))
                .withRemainingPopulationInTotalArea(input.getMoosesRemainingInTotalHuntingArea())
                .withRemainingPopulationInEffectiveArea(input.getMoosesRemainingInEffectiveHuntingArea());

        summary.setAreaSizeAndPopulation(areaAndPopulation);

        if (areaAndPopulation.getEffectiveHuntingArea() == null) {
            summary.setEffectiveHuntingAreaPercentage(
                    Optional.ofNullable(input.getEffectiveHuntingAreaPercentage())
                            .map(Number::floatValue)
                            .orElse(null));
        } else {
            summary.setEffectiveHuntingAreaPercentage(null);
        }

        summary.setHuntingAreaType(MooseDataCardExtractor.convertMooseHuntingAreaType(input.getHuntingAreaType()));
    }

    private static void transferSection(@Nonnull final MooseDataCardSection_8_3 input,
                                        @Nonnull final MooseHuntingSummary summary) {

        summary.setNumberOfDrownedMooses(input.getNumberOfDrownedMooses());
        summary.setNumberOfMoosesKilledByBear(input.getNumberOfMoosesKilledByBear());
        summary.setNumberOfMoosesKilledByWolf(input.getNumberOfMoosesKilledByWolf());
        summary.setNumberOfMoosesKilledInTrafficAccident(input.getNumberOfMoosesKilledInTrafficAccident());
        summary.setNumberOfMoosesKilledByPoaching(input.getNumberOfMoosesKilledInPoaching());
        summary.setNumberOfMoosesKilledInRutFight(input.getNumberOfMoosesKilledInRutFight());
        summary.setNumberOfStarvedMooses(input.getNumberOfStarvedMooses());
        summary.setNumberOfMoosesDeceasedByOtherReason(input.getNumberOfMoosesDeceasedByOtherReason());
        summary.setCauseOfDeath(input.getExplanationForOtherReason());
    }

    private static void transferSection(@Nonnull final MooseDataCardSection_8_4 input,
                                        @Nonnull final MooseHuntingSummary summary) {

        summary.setMooseHeatBeginDate(input.getMooseHeatBeginDate());
        summary.setMooseHeatEndDate(input.getMooseHeatEndDate());
        summary.setMooseFawnBeginDate(input.getMooseFawnBeginDate());
        summary.setMooseFawnEndDate(input.getMooseFawnEndDate());

        summary.setDeerFliesAppeared(convertAppearance(input.getDeerFlyAppearead()));

        summary.setDateOfFirstDeerFlySeen(input.getDateOfFirstDeerFlySeen());
        summary.setDateOfLastDeerFlySeen(input.getDateOfLastDeerFlySeen());
        summary.setNumberOfAdultMoosesHavingFlies(input.getNumberOfAdultMoosesHavingFlies());
        summary.setNumberOfYoungMoosesHavingFlies(input.getNumberOfYoungMoosesHavingFlies());
        summary.setTrendOfDeerFlyPopulationGrowth(
                convertTrendOfPopulationGrowthOfFlyDeer(input.getTrendOfDeerFlyPopulationGrowth()));
    }
}
