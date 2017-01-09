package fi.riista.integration.luke;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameAge;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingMethod;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingAreaType;
import fi.riista.feature.huntingclub.permit.summary.TrendOfPopulationGrowth;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameAge;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameAntlersType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameFitnessClass;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameGender;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_GameMarking;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_HuntingMethod;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_MooseHuntingAreaType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservationType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservedGameAge;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_ObservedGameState;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_RestrictionType;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_Source;
import fi.riista.integration.luke_export.mooselikeharvests.LEM_TrendOfPopulationGrowth;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MooselikeHarvestsObjectFactoryTest {

    private static <A extends Enum<A>, B extends Enum<B>>
    void assertEnumValuesMatch(Class<A> first, Class<B> second) {
        assertEquals(first.getEnumConstants().length, second.getEnumConstants().length);

        for (A a : first.getEnumConstants()) {
            final B b = MooselikeHarvestsObjectFactory.convert(second, a);
            assertEquals(a.name(), b.name());
        }
    }

    @Test
    public void testConvertGroupHuntingMethodValues() {
        for (GroupHuntingMethod m : GroupHuntingMethod.values()) {
            final LEM_HuntingMethod e = MooselikeHarvestsObjectFactory.convertByEnumName(LEM_HuntingMethod.class, m.getExplicitName());
            assertEquals(m.getExplicitName(), e.name());
        }
    }

    @Test
    public void testConvertObservedGameAgeValues() {
        assertEnumValuesMatch(ObservedGameAge.class, LEM_ObservedGameAge.class);
    }

    @Test
    public void testConvertObservedGameStateValues() {
        assertEnumValuesMatch(ObservedGameState.class, LEM_ObservedGameState.class);
    }

    @Test
    public void testConvertPermitRestrictionTypeValues() {
        assertEnumValuesMatch(HarvestPermitSpeciesAmount.RestrictionType.class, LEM_RestrictionType.class);
    }

    @Test
    public void testConvertGameGenderValues() {
        assertEnumValuesMatch(GameGender.class, LEM_GameGender.class);
    }

    @Test
    public void testConvertGameAgeValues() {
        assertEnumValuesMatch(GameAge.class, LEM_GameAge.class);
    }

    @Test
    public void testConvertFitnessClassValues() {
        assertEnumValuesMatch(GameFitnessClass.class, LEM_GameFitnessClass.class);
    }

    @Test
    public void testConvertAntlersTypeValues() {
        assertEnumValuesMatch(GameAntlersType.class, LEM_GameAntlersType.class);
    }

    @Test
    @Ignore
    public void testConvertObservationTypeValues() {
        assertEnumValuesMatch(ObservationType.class, LEM_ObservationType.class);
    }

    @Test
    public void testConvertSourceValues() {
        assertEnumValuesMatch(GeoLocation.Source.class, LEM_Source.class);
    }

    @Test
    public void testConvertGameMarkingValues() {
        assertEnumValuesMatch(GameMarking.class, LEM_GameMarking.class);
    }

    @Test
    public void testConvertMooseHuntingAreaTypeValues() {
        assertEnumValuesMatch(MooseHuntingAreaType.class, LEM_MooseHuntingAreaType.class);
    }

    @Test
    public void testConvertTrendOfPopulationGrowthValues() {
        assertEnumValuesMatch(TrendOfPopulationGrowth.class, LEM_TrendOfPopulationGrowth.class);
    }
}
