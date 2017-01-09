package fi.riista.feature.huntingclub.moosedatacard.validation;

import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseCalf;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseFemale;
import static fi.riista.feature.huntingclub.moosedatacard.MooseDataCardObjectFactory.newMooseMale;
import static org.junit.Assert.assertEquals;

import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardHarvest;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseCalf;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseFemale;
import fi.riista.integration.luke_import.model.v1_0.MooseDataCardMooseMale;

import javaslang.collection.List;

import org.junit.Test;

import java.util.function.Consumer;

public class MooseDataCardValidatorTest {

    @Test
    public void testCalculateHarvestAmounts() {
        final Consumer<MooseDataCardHarvest> notEdibleSetter = harvest -> harvest.setNotEdible(true);

        final List<MooseDataCardMooseMale> edibleAdultMales = List.fill(3, () -> newMooseMale());
        final List<MooseDataCardMooseMale> nonEdibleAdultMales = List.fill(5, () -> newMooseMale());
        nonEdibleAdultMales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseFemale> edibleAdultFemales = List.fill(7, () -> newMooseFemale());
        final List<MooseDataCardMooseFemale> nonEdibleAdultFemales = List.fill(11, () -> newMooseFemale());
        nonEdibleAdultFemales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseCalf> edibleYoungMales = List.fill(13, () -> newMooseCalf());
        edibleYoungMales.forEach(calf -> calf.setGender(GameGender.MALE.getMooseDataCardEncoding()));

        final List<MooseDataCardMooseCalf> nonEdibleYoungMales = List.fill(17, () -> newMooseCalf());
        nonEdibleYoungMales.forEach(calf -> calf.setGender(GameGender.MALE.getMooseDataCardEncoding()));
        nonEdibleYoungMales.forEach(notEdibleSetter);

        final List<MooseDataCardMooseCalf> edibleYoungFemales = List.fill(19, () -> newMooseCalf());
        edibleYoungFemales.forEach(calf -> calf.setGender(GameGender.FEMALE.getMooseDataCardEncoding()));

        final List<MooseDataCardMooseCalf> nonEdibleYoungFemales = List.fill(23, () -> newMooseCalf());
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

}
