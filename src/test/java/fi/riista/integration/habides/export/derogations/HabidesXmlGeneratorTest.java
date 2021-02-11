package fi.riista.integration.habides.export.derogations;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class HabidesXmlGeneratorTest implements DefaultEntitySupplierProvider {

    private EntitySupplier model;
    private ObjectFactory f = null;

    @Before
    public void setUp() {
        model = getEntitySupplier();
        f = new ObjectFactory();
    }

    @Test
    public void createMethodsForBirdsReturnsNotRelevantIfMethodsNull() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, null, OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsNotRelevantIfMethodsNull() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, null, OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForBirdsReturnsNotRelevantIfMethodsIsEmpty() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, Collections.emptyList(), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsNotRelevantIfMethodsIsEmpty() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, Collections.emptyList(), OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForBirdsReturnsForbiddenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
                ForbiddenMethodType.POISON
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.FORBIDDEN.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsForbiddenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
                ForbiddenMethodType.POISON
        }), OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.MAMMALS_FORBIDDEN.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForBirdsReturnsNonSelectiveIfSelectiveAndNonSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.NON_SELECTIVE.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsIndiscriminateIfSelectiveAndNonSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
        }), OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.INDISCRIMINATE.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForBirdsReturnsNotRelevantIfOtherSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsNotRelevantIfOtherSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
        }), OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForBirdsReturnsOneValueEvenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.POISON,
                ForbiddenMethodType.ARTIFICIAL_LIGHT,
                ForbiddenMethodType.HOOKS
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.BirdMethodsType.FORBIDDEN.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createMethodsForHabitatsReturnsOneValueEvenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.POISON,
                ForbiddenMethodType.ARTIFICIAL_LIGHT,
                ForbiddenMethodType.ELECTRICAL_DEVICE
        }), OFFICIAL_CODE_ROE_DEER);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.HabitatsMethodsType.MAMMALS_FORBIDDEN.getLabel(), result.getMethod().get(0));
    }

    @Test
    public void createReasonsFiltersDuplicateReasons() {
        final DERO_Reasons result = HabidesXmlGenerator.createReasons(f, createReasonsList(new PermitDecisionDerogationReasonType[]{
                PermitDecisionDerogationReasonType.REASON_FAUNA,
                PermitDecisionDerogationReasonType.REASON_FLORA
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(1, result.getReason().size());
        assertEquals(HabidesXmlGenerator.ReasonsType.FLORA_AND_FAUNA.label, result.getReason().get(0));

    }

    @Test
    public void createReasonsReturnsAllBirdReasonsInOrder() {
        final DERO_Reasons result = HabidesXmlGenerator.createReasons(f, createReasonsList(new PermitDecisionDerogationReasonType[]{
                PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION,
                PermitDecisionDerogationReasonType.REASON_AVIATION_SAFETY,
                PermitDecisionDerogationReasonType.REASON_CROPS_DAMAMGE,
                PermitDecisionDerogationReasonType.REASON_DOMESTIC_PETS,
                PermitDecisionDerogationReasonType.REASON_FISHING,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_HEALTH,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_SAFETY,
                PermitDecisionDerogationReasonType.REASON_FAUNA,
                PermitDecisionDerogationReasonType.REASON_FLORA,
                PermitDecisionDerogationReasonType.REASON_RESEARCH,
                PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION,
                PermitDecisionDerogationReasonType.REASON_WATER_SYSTEM
        }), OFFICIAL_CODE_PARTRIDGE);
        assertEquals(6, result.getReason().size());
        assertEquals(HabidesXmlGenerator.ReasonsType.PUBLIC_HEALTH_AND_SAFETY.label, result.getReason().get(0));
        assertEquals(HabidesXmlGenerator.ReasonsType.AIR_SAFETY.label, result.getReason().get(1));
        assertEquals(HabidesXmlGenerator.ReasonsType.CROPS_LIVESTOCK_ETC.label, result.getReason().get(2));
        assertEquals(HabidesXmlGenerator.ReasonsType.FLORA_AND_FAUNA.label, result.getReason().get(3));
        assertEquals(HabidesXmlGenerator.ReasonsType.RESEARCH.label, result.getReason().get(4));
        assertEquals(HabidesXmlGenerator.ReasonsType.REPOPULATION.label, result.getReason().get(5));
    }

    @Test
    public void createReasonsReturnsAllHabitatsReasonsInOrder() {
        final DERO_Reasons result = HabidesXmlGenerator.createReasons(f, createReasonsList(new PermitDecisionDerogationReasonType[]{
                PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION,
                PermitDecisionDerogationReasonType.REASON_AVIATION_SAFETY,
                PermitDecisionDerogationReasonType.REASON_CROPS_DAMAMGE,
                PermitDecisionDerogationReasonType.REASON_DOMESTIC_PETS,
                PermitDecisionDerogationReasonType.REASON_FISHING,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_HEALTH,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_SAFETY,
                PermitDecisionDerogationReasonType.REASON_FAUNA,
                PermitDecisionDerogationReasonType.REASON_FLORA,
                PermitDecisionDerogationReasonType.REASON_RESEARCH,
                PermitDecisionDerogationReasonType.REASON_POPULATION_PRESERVATION,
                PermitDecisionDerogationReasonType.REASON_WATER_SYSTEM,

                PermitDecisionDerogationReasonType.REASON_RESEARCH_41A,
                PermitDecisionDerogationReasonType.REASON_OTHER_COMMON_INTEREST_41A,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_SAFETY_41A,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_HEALTH_41A,
                PermitDecisionDerogationReasonType.REASON_OTHER_PROPERTY_DAMAGE_41A,
                PermitDecisionDerogationReasonType.REASON_WATER_SYSTEM_41A,
                PermitDecisionDerogationReasonType.REASON_REINDEER_HUSBANDRY_41A,
                PermitDecisionDerogationReasonType.REASON_FISHING_41A,
                PermitDecisionDerogationReasonType.REASON_FOREST_DAMAGE_41A,
                PermitDecisionDerogationReasonType.REASON_CATTLE_DAMAGE_41A,
                PermitDecisionDerogationReasonType.REASON_CROPS_DAMAGE_41A,
                PermitDecisionDerogationReasonType.REASON_FAUNA_41A,
                PermitDecisionDerogationReasonType.REASON_FLORA_41A,

                PermitDecisionDerogationReasonType.REASON_RESEARCH_41C,
                PermitDecisionDerogationReasonType.REASON_OTHER_COMMON_INTEREST_41C,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_SAFETY_41C,
                PermitDecisionDerogationReasonType.REASON_PUBLIC_HEALTH_41C,
                PermitDecisionDerogationReasonType.REASON_OTHER_PROPERTY_DAMAGE_41C,
                PermitDecisionDerogationReasonType.REASON_WATER_SYSTEM_41C,
                PermitDecisionDerogationReasonType.REASON_GAME_HUSBANDRY_41C,
                PermitDecisionDerogationReasonType.REASON_REINDEER_HUSBANDRY_41C,
                PermitDecisionDerogationReasonType.REASON_FISHING_41C,
                PermitDecisionDerogationReasonType.REASON_FOREST_DAMAGE_41C,
                PermitDecisionDerogationReasonType.REASON_CATTLE_DAMAGE_41C,
                PermitDecisionDerogationReasonType.REASON_CROPS_DAMAGE_41C,
                PermitDecisionDerogationReasonType.REASON_FAUNA_41C,
                PermitDecisionDerogationReasonType.REASON_FLORA_41C
        }), OFFICIAL_CODE_ROE_DEER);
        assertEquals(5, result.getReason().size());
        assertEquals(HabidesXmlGenerator.ReasonsType.HABITATS_FLORA_AND_FAUNA.label, result.getReason().get(0));
        assertEquals(HabidesXmlGenerator.ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC.label, result.getReason().get(1));
        assertEquals(HabidesXmlGenerator.ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY.label, result.getReason().get(2));
        assertEquals(HabidesXmlGenerator.ReasonsType.HABITATS_RESEARCH.label, result.getReason().get(3));
        assertEquals(HabidesXmlGenerator.ReasonsType.HABITATS_REPOPULATION.label, result.getReason().get(4));
    }

    @Test
    public void activityForKillingBird() {
        final DERO_Activities activities = HabidesXmlGenerator.createActivities(f, OFFICIAL_CODE_PARTRIDGE, FOWL_AND_UNPROTECTED_BIRD);
        assertEquals(1, activities.getActivity().size());
        assertEquals(HabidesXmlGenerator.ActivitiesType.KILLING.label, activities.getActivity().get(0));
    }

    @Test
    public void activityForNestRemovalBird() {
        final DERO_Activities activities = HabidesXmlGenerator.createActivities(f, OFFICIAL_CODE_PARTRIDGE, NEST_REMOVAL_BASED);
        assertEquals(1, activities.getActivity().size());
        assertEquals(HabidesXmlGenerator.ActivitiesType.DESTROY_NESTS_OR_EGGS.label, activities.getActivity().get(0));
    }

    @Test
    public void activityForKillingMammal() {
        final DERO_Activities activities = HabidesXmlGenerator.createActivities(f, OFFICIAL_CODE_ROE_DEER, MAMMAL_DAMAGE_BASED);
        assertEquals(1, activities.getActivity().size());
        assertEquals(HabidesXmlGenerator.ActivitiesType.HABITATS_ANIMALS_KILLING.label, activities.getActivity().get(0));
    }

    @Test
    public void activityForNestRemovalMammal() {
        final DERO_Activities activities = HabidesXmlGenerator.createActivities(f, OFFICIAL_CODE_ROE_DEER, NEST_REMOVAL_BASED);
        assertEquals(1, activities.getActivity().size());
        assertEquals(HabidesXmlGenerator.ActivitiesType.HABITATS_ANIMALS_DESTROY_BREEDING_SITES.label, activities.getActivity().get(0));
    }

    @Test
    public void validDateRangesAsStringReturnsOneRangeIf2ndDateNotDefined() {
        final HarvestPermitSpeciesAmount input = new HarvestPermitSpeciesAmount();
        input.setBeginDate(new LocalDate(2018, 1, 2));
        input.setEndDate(new LocalDate(2019, 3,4));
        assertEquals("2018-01-02 - 2019-03-04", HabidesXmlGenerator.validDateRangesAsString(input));
    }

    @Test
    public void validDateRangesAsStringReturnsTwoRangesIf2ndDateDefined() {
        final HarvestPermitSpeciesAmount input = new HarvestPermitSpeciesAmount();
        input.setBeginDate(new LocalDate(2018, 1, 2));
        input.setEndDate(new LocalDate(2019, 3,4));
        input.setBeginDate2(new LocalDate(2020, 5, 6));
        input.setEndDate2(new LocalDate(2021, 7,8));
        assertEquals("2018-01-02 - 2019-03-04, 2020-05-06 - 2021-07-08",
                HabidesXmlGenerator.validDateRangesAsString(input));
    }

    /*

        Helper functions

     */

    private static List<PermitDecisionForbiddenMethod> createForbiddenMethodList(final ForbiddenMethodType[] methodTypes) {
        return Arrays.stream(methodTypes)
                .map(methodType -> {
                    PermitDecisionForbiddenMethod method = new PermitDecisionForbiddenMethod();
                    method.setMethod(methodType);
                    return method;
                })
                .collect(toList());
    }

    private static List<PermitDecisionDerogationReason> createReasonsList(final PermitDecisionDerogationReasonType[] reasonTypes) {
        return Arrays.stream(reasonTypes)
                .map(reasonType -> {
                    PermitDecisionDerogationReason reason = new PermitDecisionDerogationReason();
                    reason.setReasonType(reasonType);
                    return reason;
                })
                .collect(toList());
    }

}
