package fi.riista.feature.huntingclub.moosedatacard.validation;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.Has2BeginEndDatesDTO;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCard;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardHuntingDay;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardLargeCarnivoreObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardObservation;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage2;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardPage3;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_2_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_3_1;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardSection_7_1;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import io.vavr.collection.List;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.huntingDayStartDateNotWithinPermittedSeason;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationAbandonedBecauseOfMissingDate;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImportMessages.observationTypeOfLargeCarnivoreContainsIllegalCharacters;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newHuntingDay;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newLargeCarnivoreObservation;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseCalf;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseDataCardWithoutSummary;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseFemale;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseMale;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseObservation;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newPage7;
import static fi.riista.test.Asserts.assertValid;
import static org.junit.Assert.assertEquals;

public class MooseDataCardValidatorTest implements ValueGeneratorMixin {

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void testCalculateHarvestAmounts() {
        final LocalDate date = DateUtil.today();
        final Consumer<MooseDataCardHarvest> notEdibleSetter = harvest -> harvest.setNotEdible(true);

        final List<MooseDataCardMooseMale> edibleAdultMales = List.fill(3, () -> newMooseMale(date));
        final List<MooseDataCardMooseMale> nonEdibleAdultMales = List.fill(5, () -> newMooseMale(date));
        nonEdibleAdultMales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseFemale> edibleAdultFemales = List.fill(7, () -> newMooseFemale(date));
        final List<MooseDataCardMooseFemale> nonEdibleAdultFemales = List.fill(11, () -> newMooseFemale(date));
        nonEdibleAdultFemales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseCalf> edibleYoungMales = List.fill(13, () -> newMooseCalf(date));
        edibleYoungMales.forEach(calf -> calf.setGender(GameGender.MALE.getMooseDataCardEncoding()));

        final List<MooseDataCardMooseCalf> nonEdibleYoungMales = List.fill(17, () -> newMooseCalf(date));
        nonEdibleYoungMales.forEach(calf -> calf.setGender(GameGender.MALE.getMooseDataCardEncoding()));
        nonEdibleYoungMales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseCalf> edibleYoungFemales = List.fill(19, () -> newMooseCalf(date));
        edibleYoungFemales.forEach(calf -> calf.setGender(GameGender.FEMALE.getMooseDataCardEncoding()));

        final List<MooseDataCardMooseCalf> nonEdibleYoungFemales = List.fill(23, () -> newMooseCalf(date));
        nonEdibleYoungFemales.forEach(calf -> calf.setGender(GameGender.FEMALE.getMooseDataCardEncoding()));
        nonEdibleYoungFemales.forEach(notEdibleSetter);

        assertEquals(
                new MooseDataCardCalculatedHarvestAmounts(
                        edibleAdultMales.size() + nonEdibleAdultMales.size(),
                        edibleAdultFemales.size() + nonEdibleAdultFemales.size(),
                        edibleYoungMales.size() + nonEdibleYoungMales.size(),
                        edibleYoungFemales.size() + nonEdibleYoungFemales.size(),
                        nonEdibleAdultMales.size() + nonEdibleAdultFemales.size(),
                        nonEdibleYoungMales.size() + nonEdibleYoungFemales.size()).asTuple6(),
                MooseDataCardValidator.calculateHarvestAmounts(
                        edibleAdultMales.appendAll(nonEdibleAdultMales),
                        edibleAdultFemales.appendAll(nonEdibleAdultFemales),
                        edibleYoungMales
                                .appendAll(nonEdibleYoungMales)
                                .appendAll(edibleYoungFemales)
                                .appendAll(nonEdibleYoungFemales))
                        .asTuple6());
    }

    @Test
    public void testCollectingOfAbandonReasons() {
        final LocalDate today = DateUtil.today();
        final Has2BeginEndDates permitSeason = new Has2BeginEndDatesDTO(today, today);

        final MooseDataCardHuntingDay abandonedHuntingDay = newHuntingDay(today.plusDays(1));
        final MooseDataCardObservation mooseObservation = newMooseObservation(null);
        final MooseDataCardLargeCarnivoreObservation carnivore =
                newLargeCarnivoreObservation(today).withObservationType("INVALID");

        final MooseDataCard card = newMooseDataCardWithoutSummary()
                .withPage2(new MooseDataCardPage2()
                        .withSection_2_1(new MooseDataCardSection_2_1()
                                .withHuntingDays(abandonedHuntingDay, newHuntingDay(today))))
                .withPage3(new MooseDataCardPage3()
                        .withSection_3_1(new MooseDataCardSection_3_1()
                                .withMooseObservations(mooseObservation)))
                .withPage7(newPage7()
                        .withSection_7_1(new MooseDataCardSection_7_1()
                                .withLargeCarnivoreObservations(carnivore)));

        final MooseDataCardValidator validator = new MooseDataCardValidator(permitSeason, geoLocation());

        assertValid(validator.validate(card), resultTuple -> assertEquals(
                Arrays.asList(
                        huntingDayStartDateNotWithinPermittedSeason(abandonedHuntingDay.getStartDate(), permitSeason),
                        observationAbandonedBecauseOfMissingDate(mooseObservation),
                        observationTypeOfLargeCarnivoreContainsIllegalCharacters(carnivore)),
                resultTuple._2));
    }

}
