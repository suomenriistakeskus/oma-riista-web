package fi.riista.integration.habides.export.derogations;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class HabidesXmlGenerator {

    private static final ImmutableMap<String, MethodsType> methodMap = ImmutableMap.<String, MethodsType>builder()
            .put("SNARES", MethodsType.FORBIDDEN)
            .put("LIVE_ANIMAL_DECOY", MethodsType.FORBIDDEN)
            .put("TAPE_RECORDERS", MethodsType.FORBIDDEN)
            .put("ELECTRICAL_DEVICE", MethodsType.FORBIDDEN)
            .put("ARTIFICIAL_LIGHT", MethodsType.FORBIDDEN)
            .put("MIRRORS", MethodsType.FORBIDDEN)
            .put("ILLUMINATION_DEVICE", MethodsType.FORBIDDEN)
            .put("NIGHT_SHOOTING_DEVICE", MethodsType.FORBIDDEN)
            .put("EXPLOSIVES", MethodsType.FORBIDDEN)
            .put("NETS", MethodsType.FORBIDDEN)
            .put("TRAPS", MethodsType.FORBIDDEN)
            .put("POISON", MethodsType.FORBIDDEN)
            .put("GASSING", MethodsType.FORBIDDEN)
            .put("AUTOMATIC_WEAPON", MethodsType.FORBIDDEN)
            .put("LIMES", MethodsType.FORBIDDEN)
            .put("HOOKS", MethodsType.FORBIDDEN)
            .put("OTHER_NON_SELECTIVE", MethodsType.NON_SELECTIVE)
            .put("LEGHOLD_TRAP", MethodsType.NON_SELECTIVE)
            .put("CONCEALED_WEAPON", MethodsType.NON_SELECTIVE)      // From Finnish hunting law
            .put("OTHER_SELECTIVE", MethodsType.SELECTIVE_OR_NOT_RELEVANT)
            .put("CROSSBOWS", MethodsType.SELECTIVE_OR_NOT_RELEVANT) // From Finnish hunting law
            .put("SPEAR", MethodsType.SELECTIVE_OR_NOT_RELEVANT)     // From Finnish hunting law
            .put("BLOWPIPE", MethodsType.SELECTIVE_OR_NOT_RELEVANT)  // From Finnish hunting law
            .build();

    private static final ImmutableMap<String, ReasonsType> reasonMap = ImmutableMap.<String, ReasonsType>builder()
            .put("REASON_PUBLIC_HEALTH", ReasonsType.PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_PUBLIC_SAFETY", ReasonsType.PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_AVIATION_SAFETY", ReasonsType.AIR_SAFETY)
            .put("REASON_CROPS_DAMAMGE", ReasonsType.CROPS_LIVESTOCK_ETC)
            .put("REASON_DOMESTIC_PETS", ReasonsType.CROPS_LIVESTOCK_ETC)
            .put("REASON_FOREST_DAMAGE", ReasonsType.CROPS_LIVESTOCK_ETC)
            .put("REASON_FISHING", ReasonsType.CROPS_LIVESTOCK_ETC)
            .put("REASON_WATER_SYSTEM", ReasonsType.CROPS_LIVESTOCK_ETC)
            .put("REASON_FLORA", ReasonsType.FLORA_AND_FAUNA)
            .put("REASON_FAUNA", ReasonsType.FLORA_AND_FAUNA)
            .put("REASON_RESEARCH", ReasonsType.RESEARCH)
            .put("REASON_POPULATION_PRESERVATION", ReasonsType.REPOPULATION)
            .build();

    private enum DerogationJustificationsType {
        NOT_HUNTABLE("1"),
        REPRODUCTIVE_SEASON("2"),
        TRANSPORT_MODES("3"),
        TYPE_OTHER("4");

        public final String label;

        DerogationJustificationsType(String label) {
            this.label = label;
        }
    }

    // Package private for testing
    /* package */ enum ReasonsType {
        PUBLIC_HEALTH_AND_SAFETY("10"),
        AIR_SAFETY("20"),
        CROPS_LIVESTOCK_ETC("30"),
        FLORA_AND_FAUNA("40"),
        RESEARCH("50"),
        REPOPULATION("55");

        public final String label;

        ReasonsType(String label) {
            this.label = label;
        }
    }

    private enum ActivitiesType {
        KILLING("1"),
        CAPTURE_FOR_CAPTIVITY("2"),
        CAPTURE_AND_IMMEDIATE_RELEASE("3"),
        DESTROY_NESTS_OR_EGGS("4"),
        TAKE_EGGS("5"),
        DISTURB("6"),
        KEEP_PROHIBITED_SPECIES("7"),
        SALE("8");

        public final String label;

        ActivitiesType(String label) {
            this.label = label;
        }
    }

    // Package private for testing
    /* package */ enum MethodsType {    // Order is meaningful:
        FORBIDDEN("2"),                 // The highest priority
        NON_SELECTIVE("1"),
        SELECTIVE_OR_NOT_RELEVANT("0"); // The lowest priority

        public final String label;

        MethodsType(String label) {
            this.label = label;
        }
    }

    private enum ModesOfTransportsType {
        NOT_RELEVANT("0"),
        AIRCRAFT("1"),
        MOTOR_VEHICLES("2"),
        BOATS_ON_FRESHWATER_OR_COASTAL("3"),
        MOTOR_BOATS_ON_OPEN_SEA("4");

        public final String label;

        ModesOfTransportsType(String label) {
            this.label = label;
        }
    }

    private HabidesXmlGenerator() {} // private constructor, mimic static class

    public static DERO_Derogations generateBirdsXml(
            final GameSpecies species,
            final List<HarvestPermitSpeciesAmount> amounts,
            final Map<Long, Integer> harvestAmounts,
            final Map<Long, List<PermitDecisionDerogationReason>> reasons,
            final Map<Long, List<PermitDecisionForbiddenMethod>> methods,
            final Map<Long, String> locations,
            final Map<String, String> nutsAreas,
            final Map<Long, String> authorities) {

        final ObjectFactory f = new ObjectFactory();
        final DERO_Derogations derogations = f.createDERO_Derogations()
                .withCountry("FI")
                .withLabelLanguage("en")
                .withLang("en")
                .withUserIdentity("FIMMM");

        for (HarvestPermitSpeciesAmount amount : amounts) {
            final Long permitId = amount.getHarvestPermit().getId();
            final Long decisionId = amount.getHarvestPermit().getPermitDecision().getId();
            final Long applicationId = amount.getHarvestPermit().getPermitDecision().getApplication().getId();
            final float harvestAmount = harvestAmounts.getOrDefault(permitId, 0);
            derogations.withDerogation(
                    createDerogation(f,
                            species,
                            amount,
                            harvestAmount,
                            reasons.get(decisionId),
                            methods.get(decisionId),
                            locations.get(applicationId),
                            nutsAreas,
                            authorities));
        }

        return derogations;
    }

    private static DERO_Derogation createDerogation(
            final ObjectFactory f,
            final GameSpecies species,
            final HarvestPermitSpeciesAmount amount,
            final float harvestedAmount,
            final List<PermitDecisionDerogationReason> reasons,
            final List<PermitDecisionForbiddenMethod> methods,
            final String location,
            final Map<String, String> nutsAreas,
            final Map<Long, String> authorities) {

        final DERO_Derogation derogation = f.createDERO_Derogation();

        final HarvestPermit permit = amount.getHarvestPermit();
        final PermitDecision decision = permit.getPermitDecision();
        final String authorityName = authorities.get(decision.getRhy().getRiistakeskuksenAlue().getId());

        return derogation
                // attributes
                .withCountry("FI")
                .withUserDerogationRef(permit.getPermitNumber())
                .withUserIdentity("FIMMM")
                .withDerogationReference("FIMMM-B-1-" + permit.getPermitNumber() + "-" + species.getOfficialCode())
                .withDirective(DERO_DirectiveType.HTTP_ROD_EIONET_EUROPA_EU_OBLIGATIONS_276)
                .withStatus("complete")
                // elements
                .withSpecies(species.getScientificName())
                .withSpeciesGroup("")
                .withCoversAllSpecies("")
                .withSensitive("false")
                .withLicenseValidFrom(amount.getBeginDate().toString())
                .withLicenseValidUntil(
                        amount.getEndDate2() == null
                                ? amount.getEndDate().toString()
                                : amount.getEndDate2().toString())
                .withLicensingAuthority(authorityName)
                .withRegions(f.createDERO_Regions().withRegion(nutsAreas.get(permit.getRhy().getOfficialCode())))
                .withLocation(location)
                .withDerogationJustifications(createDerogationJustificationsType(f, decision))
                .withReasons(createReasons(f, reasons))
                .withDerogationJustificationDetails("")
                .withStrictlySupervisedConditions("")
                .withSelectiveBasis("")
                .withSmallNumberIndividuals("")
                .withActivities(f.createDERO_Activities().withActivity(ActivitiesType.KILLING.label)) // Only option for now
                .withAdditionalActivities(f.createDERO_AdditionalActivities())
                .withActivitiesFurtherDetails("")
                .withMethods(createMethods(f, methods))
                .withFurtherDetails(methodsAsString(methods))
                .withModesOfTransport(createModesOfTransport(f, decision))
                .withLicensed(createDerogationEntity(f, amount.getAmount()))
                .withActuallyTaken(createDerogationEntity(f, harvestedAmount))
                .withAllMeasuresTaken("true")
                .withEUAllMeasuresTaken("true")
                .withDetrimentalToPopulation("")
                .withAlternativeToDerogation("true")
                .withAlternativesAssessed("")   // TODO FOR HABITATS: Add separate field to harvest decision for this
                .withSupervisoryMeasure(authorityName)
                .withComments(validDateRangesAsString(amount));
    }

    private static DERO_DerogationJustificationsType createDerogationJustificationsType(
            final ObjectFactory f,
            final PermitDecision decision) {
        DERO_DerogationJustificationsType justifications = f.createDERO_DerogationJustificationsType()
                .withDerogationJustification(DerogationJustificationsType.REPRODUCTIVE_SEASON.label);

        if (decision.isLegalSection32()) {
            justifications.withDerogationJustification(DerogationJustificationsType.TRANSPORT_MODES.label);
        }

        return justifications;
    }

    // Package private for testing
    /* package */ static DERO_Reasons createReasons(
            final ObjectFactory f,
            final List<PermitDecisionDerogationReason> derogationReasons) {
        final DERO_Reasons reasons = f.createDERO_Reasons();

        if (derogationReasons != null) {
            reasons.withReason(
                    derogationReasons
                            .stream()
                            .map(value -> mapReason(value.getReasonType().toString()))
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList()));
        }
        return reasons;
    }

    private static String mapReason(final String derogationReason) {
        if (!reasonMap.containsKey(derogationReason)) {
            return null;
        }
        return reasonMap.get(derogationReason).label;
    }

    // Package private for testing
    /* package */ static DERO_Methods createMethods(
            final ObjectFactory f,
            final List<PermitDecisionForbiddenMethod> forbiddenMethods) {

        final DERO_Methods methods = f.createDERO_Methods();

        if (forbiddenMethods != null && !forbiddenMethods.isEmpty()) {
            forbiddenMethods
                    .stream()
                    .map(value -> mapMethod(value.getMethod().toString()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .map(value -> value.label)
                    .findFirst()
                    .ifPresent(method -> methods.withMethod(method));
        } else {
            methods.withMethod(MethodsType.SELECTIVE_OR_NOT_RELEVANT.label);
        }

        return methods;
    }

    private static MethodsType mapMethod(final String forbiddenMethod) {
        if (!methodMap.containsKey(forbiddenMethod)) {
            return null;
        }
        return methodMap.get(forbiddenMethod);
    }

    private static String methodsAsString(final List<PermitDecisionForbiddenMethod> forbiddenMethods) {
        return forbiddenMethods == null ? "" : String.join(", ",
                forbiddenMethods
                        .stream()
                        .map(m -> m.getMethod().toString().replace('_', ' ').toLowerCase())
                        .sorted()
                        .collect(Collectors.toList()));
    }

    private static DERO_ModesOfTransportType createModesOfTransport(
            final ObjectFactory f,
            final PermitDecision decision) {
        final DERO_ModesOfTransportType transport = f.createDERO_ModesOfTransportType();
        if (decision.isLegalSection32()) {
            transport.withModeOfTransport(ModesOfTransportsType.MOTOR_VEHICLES.label);
        } else {
            transport.withModeOfTransport(ModesOfTransportsType.NOT_RELEVANT.label);
        }
        return transport;
    }

    private static DERO_DerogationEntity createDerogationEntity(
            final ObjectFactory f,
            final float individuals) {
        return f.createDERO_DerogationEntity()
                .withIndividuals(BigDecimal.valueOf(individuals).setScale(0, BigDecimal.ROUND_DOWN).toString())
                .withEggs("")
                .withNests("")
                .withBreeding("")
                .withOtherType("")
                .withNoFigureProvided("false")
                .withLicensedJustification("");
    }

    // Package private for testing
    /* package */ static String validDateRangesAsString(final HarvestPermitSpeciesAmount amount) {
        String range = amount.getBeginDate().toString() + " - " + amount.getEndDate().toString();
        if (amount.getBeginDate2() != null && amount.getEndDate2() != null) {
            range += ", " + amount.getBeginDate2().toString() + " - " + amount.getEndDate2().toString();
        }
        return range;
    }

}
