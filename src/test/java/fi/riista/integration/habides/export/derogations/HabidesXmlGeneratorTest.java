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
    public void createMethodsReturnsNotRelevantIfMethodsNull() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, null);
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.SELECTIVE_OR_NOT_RELEVANT.label, result.getMethod().get(0));
    }

    @Test
    public void createMethodsReturnsNotRelevantIfMethodsIsEmpty() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, Collections.emptyList());
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.SELECTIVE_OR_NOT_RELEVANT.label, result.getMethod().get(0));
    }

    @Test
    public void createMethodsReturnsForbiddenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
                ForbiddenMethodType.POISON
        }));
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.FORBIDDEN.label, result.getMethod().get(0));
    }

    @Test
    public void createMethodsReturnsNonSelectiveIfSelectiveAndNonSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
                ForbiddenMethodType.OTHER_NON_SELECTIVE,
        }));
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.NON_SELECTIVE.label, result.getMethod().get(0));
    }

    @Test
    public void createMethodsReturnsNotRelevantIfOtherSelectiveTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.OTHER_SELECTIVE,
        }));
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.SELECTIVE_OR_NOT_RELEVANT.label, result.getMethod().get(0));
    }

    @Test
    public void createMethodsReturnsOneValueEvenIfMultipleTypesDefined() {
        final DERO_Methods result = HabidesXmlGenerator.createMethods(f, createForbiddenMethodList(new ForbiddenMethodType[]{
                ForbiddenMethodType.POISON,
                ForbiddenMethodType.ARTIFICIAL_LIGHT,
                ForbiddenMethodType.HOOKS
        }));
        assertEquals(1, result.getMethod().size());
        assertEquals(HabidesXmlGenerator.MethodsType.FORBIDDEN.label, result.getMethod().get(0));
    }

    @Test
    public void createReasonsFiltersDuplicateReasons() {
        final DERO_Reasons result = HabidesXmlGenerator.createReasons(f, createReasonsList(new PermitDecisionDerogationReasonType[]{
                PermitDecisionDerogationReasonType.REASON_FAUNA,
                PermitDecisionDerogationReasonType.REASON_FLORA
        }));
        assertEquals(1, result.getReason().size());
        assertEquals(HabidesXmlGenerator.ReasonsType.FLORA_AND_FAUNA.label, result.getReason().get(0));

    }

    @Test
    public void createReasonsReturnsAllReasonsInOrder() {
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
        }));
        assertEquals(6, result.getReason().size());
        assertEquals(HabidesXmlGenerator.ReasonsType.PUBLIC_HEALTH_AND_SAFETY.label, result.getReason().get(0));
        assertEquals(HabidesXmlGenerator.ReasonsType.AIR_SAFETY.label, result.getReason().get(1));
        assertEquals(HabidesXmlGenerator.ReasonsType.CROPS_LIVESTOCK_ETC.label, result.getReason().get(2));
        assertEquals(HabidesXmlGenerator.ReasonsType.FLORA_AND_FAUNA.label, result.getReason().get(3));
        assertEquals(HabidesXmlGenerator.ReasonsType.RESEARCH.label, result.getReason().get(4));
        assertEquals(HabidesXmlGenerator.ReasonsType.REPOPULATION.label, result.getReason().get(5));
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
