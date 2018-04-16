package fi.riista.feature.huntingclub.moosedatacard;

import static fi.riista.util.DateUtil.today;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.huntingclub.permit.summary.TrendOfPopulationGrowth;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardGameSpeciesAppearance;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHarvestAreaType;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage7;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage8;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_2;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_8_4;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardTrendOfPopulationGrowth;
import fi.riista.feature.huntingclub.moosedatacard.validation.MooseDataCardCalculatedHarvestAmounts;

import org.joda.time.LocalDate;

public class MooseDataCardObjectFactory {

    private static final GeoLocation DEFAULT_LOCATION = new GeoLocation(7085524, 480547);

    public static MooseDataCard newMooseDataCard() {
        return newMooseDataCard(newPage7(), newPage8());
    }

    public static MooseDataCard newMooseDataCard(final MooseDataCardPage7 page7) {
        return newMooseDataCard(page7, newPage8());
    }

    public static MooseDataCard newMooseDataCard(final MooseDataCardPage8 page8) {
        return newMooseDataCard(newPage7(), page8);
    }

    public static MooseDataCard newMooseDataCard(final MooseDataCardPage7 page7, final MooseDataCardPage8 page8) {
        return newMooseDataCardInternal()
                .withPage7(page7)
                .withPage8(page8);
    }

    public static MooseDataCard newMooseDataCardWithoutSummary() {
        return newMooseDataCardInternal();
    }

    private static MooseDataCard newMooseDataCardInternal() {
        return new MooseDataCard()
                .withPage1(new MooseDataCardPage1()
                        .withReportingPeriodBeginDate(today())
                        .withReportingPeriodEndDate(today().plusMonths(2)));
    }

    public static MooseDataCardHuntingDay newHuntingDay(final LocalDate date) {
        return new MooseDataCardHuntingDay()
                .withStartDate(date)
                .withHuntingTime(10.0f)
                .withSnowDepth(20)
                .withHuntingMethod(1)
                .withNumberOfHunters(10)
                .withNumberOfHounds(1);
    }

    public static MooseDataCardObservation newMooseObservation() {
        return withDateAndLocationSet(new MooseDataCardObservation()
                .withAU(6)
                .withN0(5)
                .withN1(4)
                .withN2(3)
                .withN3(2)
                .withT(1));
    }

    public static MooseDataCardMooseMale newMooseMale() {
        return withDateAndLocationSet(new MooseDataCardMooseMale()
                .withWeightEstimated(250.0)
                .withWeightMeasured(234.0)

                .withAntlersType(GameAntlersType.HANKO.getMooseDataCardEncoding())
                .withAntlersWidth(100)
                .withAntlerPointsLeft(5)
                .withAntlerPointsRight(6)

                .withFitnessClass(GameFitnessClass.ERINOMAINEN.getMooseDataCardEncoding())
                .withAdditionalInfo("text")
                .withNotEdible(false));
    }

    public static MooseDataCardMooseFemale newMooseFemale() {
        return withDateAndLocationSet(new MooseDataCardMooseFemale()
                .withWeightEstimated(250.0)
                .withWeightMeasured(234.0)
                .withFitnessClass(GameFitnessClass.ERINOMAINEN.getMooseDataCardEncoding())
                .withAdditionalInfo("text")
                .withNotEdible(false));
    }

    public static MooseDataCardMooseCalf newMooseCalf() {
        return withDateAndLocationSet(new MooseDataCardMooseCalf()
                .withGender(GameGender.MALE.getMooseDataCardEncoding())
                .withWeightEstimated(250.0)
                .withWeightMeasured(234.0)
                .withFitnessClass(GameFitnessClass.ERINOMAINEN.getMooseDataCardEncoding())
                .withAdditionalInfo("text")
                .withNotEdible(false));
    }

    public static MooseDataCardLargeCarnivoreObservation newLargeCarnivoreObservation() {
        return new MooseDataCardLargeCarnivoreObservation()
                .withDate(today())

                .withLatitude(String.valueOf(DEFAULT_LOCATION.getLatitude()))
                .withLongitude(String.valueOf(DEFAULT_LOCATION.getLongitude()))

                .withObservationType(ObservationType.NAKO.getMooseDataCardEncoding())
                .withNumberOfWolves(1)
                .withNumberOfBears(2)
                .withNumberOfLynxes(3)
                .withNumberOfWolverines(4)
                .withAdditionalInfo("text");
    }

    private static <T extends DateAndLocation> T withDateAndLocationSet(final T object) {
        object.setDate(today());
        object.setLatitude(String.valueOf(DEFAULT_LOCATION.getLatitude()));
        object.setLongitude(String.valueOf(DEFAULT_LOCATION.getLongitude()));
        return object;
    }

    public static MooseDataCardPage7 newPage7() {
        return new MooseDataCardPage7()
                .withWhiteTailedDeerAppeared(MooseDataCardGameSpeciesAppearance.YES)
                .withEstimatedSpecimenAmountOfWhiteTailedDeer(10)
                .withTrendOfWhiteTailedDeerPopulationGrowth(
                        TrendOfPopulationGrowth.INCREASED.getMooseDataCardEncoding())

                .withRoeDeerAppeared(MooseDataCardGameSpeciesAppearance.YES)
                .withEstimatedSpecimenAmountOfRoeDeer(20)
                .withTrendOfRoeDeerPopulationGrowth(TrendOfPopulationGrowth.DECREASED.getMooseDataCardEncoding())

                .withWildForestReindeerAppeared(MooseDataCardGameSpeciesAppearance.YES)
                .withEstimatedSpecimenAmountOfWildForestReindeer(30)
                .withTrendOfWildForestReindeerPopulationGrowth(
                        TrendOfPopulationGrowth.UNCHANGED.getMooseDataCardEncoding())

                .withFallowDeerAppeared(MooseDataCardGameSpeciesAppearance.YES)
                .withEstimatedSpecimenAmountOfFallowDeer(40)
                .withTrendOfFallowDeerPopulationGrowth(TrendOfPopulationGrowth.INCREASED.getMooseDataCardEncoding());
    }

    public static MooseDataCardPage8 newPage8() {
        return new MooseDataCardPage8()
                .withSection_8_1(newSection81())
                .withSection_8_2(newSection82())
                .withSection_8_3(newSection83())
                .withSection_8_4(newSection84())
                .withHuntingEndDate(today());
    }

    public static MooseDataCardSection_8_1 newSection81() {
        return new MooseDataCardSection_8_1()
                .withTotalHuntingArea(10000.0)
                .withEffectiveHuntingArea(5000.0)
                .withMoosesRemainingInTotalHuntingArea(100)
                .withMoosesRemainingInEffectiveHuntingArea(50)
                .withHuntingAreaType(MooseDataCardHarvestAreaType.BOTH);
    }

    public static MooseDataCardSection_8_2 newSection82() {
        return newSection82(newHarvestAmounts());
    }

    public static MooseDataCardSection_8_2 newSection82(final MooseDataCardCalculatedHarvestAmounts amounts) {
        return new MooseDataCardSection_8_2()
                .withNumberOfAdultMales(amounts.numberOfAdultMales)
                .withNumberOfAdultFemales(amounts.numberOfAdultFemales)
                .withNumberOfYoungMales(amounts.numberOfYoungMales)
                .withNumberOfYoungFemales(amounts.numberOfYoungFemales)
                .withTotalNumberOfNonEdibleAdults(amounts.totalNumberOfNonEdibleAdults)
                .withTotalNumberOfNonEdibleYoungs(amounts.totalNumberOfNonEdibleYoungs);
    }

    public static MooseDataCardCalculatedHarvestAmounts newHarvestAmounts() {
        return new MooseDataCardCalculatedHarvestAmounts(1, 3, 5, 7, 11, 13);
    }

    public static MooseDataCardCalculatedHarvestAmounts newHarvestAmounts(final MooseDataCardSection_8_2 section) {
        return new MooseDataCardCalculatedHarvestAmounts(
                section.getNumberOfAdultMales(),
                section.getNumberOfAdultFemales(),
                section.getNumberOfYoungMales(),
                section.getNumberOfYoungFemales(),
                section.getTotalNumberOfNonEdibleAdults(),
                section.getTotalNumberOfNonEdibleYoungs());
    }

    public static MooseDataCardSection_8_3 newSection83() {
        return new MooseDataCardSection_8_3()
                .withNumberOfDrownedMooses(1)
                .withNumberOfMoosesKilledByBear(3)
                .withNumberOfMoosesKilledByWolf(5)
                .withNumberOfMoosesKilledInTrafficAccident(7)
                .withNumberOfMoosesKilledInPoaching(11)
                .withNumberOfMoosesKilledInRutFight(13)
                .withNumberOfStarvedMooses(17)
                .withNumberOfMoosesDeceasedByOtherReason(19)
                .withExplanationForOtherReason("test");
    }

    public static MooseDataCardSection_8_4 newSection84() {
        final LocalDate today = today();

        return new MooseDataCardSection_8_4()
                .withMooseHeatBeginDate(today)
                .withMooseHeatEndDate(today.plusDays(1))
                .withMooseFawnBeginDate(today.plusDays(2))
                .withMooseFawnEndDate(today.plusDays(3))

                .withDeerFlyAppearead(MooseDataCardGameSpeciesAppearance.YES)
                .withDateOfFirstDeerFlySeen(today.plusDays(4))
                .withDateOfLastDeerFlySeen(today.plusDays(5))
                .withTrendOfDeerFlyPopulationGrowth(MooseDataCardTrendOfPopulationGrowth.INCREASED)
                .withNumberOfAdultMoosesHavingFlies(123)
                .withNumberOfYoungMoosesHavingFlies(456);
    }

}
