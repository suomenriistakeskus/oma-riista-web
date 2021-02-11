package fi.riista.integration.habides.export.derogations;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.methods.PermitDecisionForbiddenMethod;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class HabidesXmlGenerator {

    private static final ImmutableMap<String, BirdMethodsType> birdMethodMap = ImmutableMap.<String, BirdMethodsType>builder()
            .put("SNARES", BirdMethodsType.FORBIDDEN)
            .put("LIVE_ANIMAL_DECOY", BirdMethodsType.FORBIDDEN)
            .put("TAPE_RECORDERS", BirdMethodsType.FORBIDDEN)
            .put("ELECTRICAL_DEVICE", BirdMethodsType.FORBIDDEN)
            .put("ARTIFICIAL_LIGHT", BirdMethodsType.FORBIDDEN)
            .put("MIRRORS", BirdMethodsType.FORBIDDEN)
            .put("ILLUMINATION_DEVICE", BirdMethodsType.FORBIDDEN)
            .put("NIGHT_SHOOTING_DEVICE", BirdMethodsType.FORBIDDEN)
            .put("EXPLOSIVES", BirdMethodsType.FORBIDDEN)
            .put("NETS", BirdMethodsType.FORBIDDEN)
            .put("TRAPS", BirdMethodsType.FORBIDDEN)
            .put("POISON", BirdMethodsType.FORBIDDEN)
            .put("GASSING", BirdMethodsType.FORBIDDEN)
            .put("AUTOMATIC_WEAPON", BirdMethodsType.FORBIDDEN)
            .put("LIMES", BirdMethodsType.FORBIDDEN)
            .put("HOOKS", BirdMethodsType.FORBIDDEN)
            .put("OTHER_NON_SELECTIVE", BirdMethodsType.NON_SELECTIVE)
            .put("LEGHOLD_TRAP", BirdMethodsType.NON_SELECTIVE)
            .put("CONCEALED_WEAPON", BirdMethodsType.NON_SELECTIVE)      // From Finnish hunting law
            .put("OTHER_SELECTIVE", BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT)
            .put("CROSSBOWS", BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT) // From Finnish hunting law
            .put("SPEAR", BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT)     // From Finnish hunting law
            .put("BLOWPIPE", BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT)  // From Finnish hunting law
            .build();

    private static final ImmutableMap<String, HabitatsMethodsType> habitatsMethodMap = ImmutableMap.<String, HabitatsMethodsType>builder()
            .put("LIVE_ANIMAL_DECOY", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("TAPE_RECORDERS", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("ELECTRICAL_DEVICE", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("ARTIFICIAL_LIGHT", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("MIRRORS", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("ILLUMINATION_DEVICE", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("NIGHT_SHOOTING_DEVICE", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("EXPLOSIVES", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("NETS", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("TRAPS", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("CROSSBOWS", HabitatsMethodsType.MAMMALS_FORBIDDEN) // From Finnish hunting law
            .put("POISON", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("GASSING", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("AUTOMATIC_WEAPON", HabitatsMethodsType.MAMMALS_FORBIDDEN)
            .put("SNARES", HabitatsMethodsType.INDISCRIMINATE)
            .put("LIMES", HabitatsMethodsType.INDISCRIMINATE)
            .put("HOOKS", HabitatsMethodsType.INDISCRIMINATE)
            .put("OTHER_NON_SELECTIVE", HabitatsMethodsType.INDISCRIMINATE)
            .put("LEGHOLD_TRAP", HabitatsMethodsType.INDISCRIMINATE)
            .put("CONCEALED_WEAPON", HabitatsMethodsType.INDISCRIMINATE)      // From Finnish hunting law
            .put("OTHER_SELECTIVE", HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT)
            .put("SPEAR", HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT)     // From Finnish hunting law
            .put("BLOWPIPE", HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT)  // From Finnish hunting law
            .build();

    private static final ImmutableMap<String, ReasonsType> birdReasonMap = ImmutableMap.<String, ReasonsType>builder()
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

    private static final ImmutableMap<String, ReasonsType> habitatsReasonMap = ImmutableMap.<String, ReasonsType>builder()
            .put("REASON_PUBLIC_HEALTH", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_PUBLIC_SAFETY", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_AVIATION_SAFETY", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_CROPS_DAMAMGE", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_DOMESTIC_PETS", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FOREST_DAMAGE", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FISHING", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_WATER_SYSTEM", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FLORA", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_FAUNA", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_RESEARCH", ReasonsType.HABITATS_RESEARCH)
            .put("REASON_POPULATION_PRESERVATION", ReasonsType.HABITATS_REPOPULATION)

            .put("REASON_FLORA_41A", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_FAUNA_41A", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_CROPS_DAMAGE_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_CATTLE_DAMAGE_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FOREST_DAMAGE_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FISHING_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_REINDEER_HUSBANDRY_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_WATER_SYSTEM_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_OTHER_PROPERTY_DAMAGE_41A", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_PUBLIC_HEALTH_41A", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_PUBLIC_SAFETY_41A", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_OTHER_COMMON_INTEREST_41A", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_RESEARCH_41A", ReasonsType.HABITATS_RESEARCH)

            .put("REASON_FLORA_41C", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_FAUNA_41C", ReasonsType.HABITATS_FLORA_AND_FAUNA)
            .put("REASON_CROPS_DAMAGE_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_CATTLE_DAMAGE_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FOREST_DAMAGE_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_FISHING_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_REINDEER_HUSBANDRY_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_GAME_HUSBANDRY_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_WATER_SYSTEM_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_OTHER_PROPERTY_DAMAGE_41C", ReasonsType.HABITATS_CROPS_LIVESTOCK_ETC)
            .put("REASON_PUBLIC_HEALTH_41C", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_PUBLIC_SAFETY_41C", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_OTHER_COMMON_INTEREST_41C", ReasonsType.HABITATS_PUBLIC_HEALTH_AND_SAFETY)
            .put("REASON_RESEARCH_41C", ReasonsType.HABITATS_RESEARCH)
            .build();

    private enum DerogationJustificationsType {
        NOT_HUNTABLE("1"),
        REPRODUCTIVE_SEASON("2"),
        TRANSPORT_OR_FORBIDDEN_METHOD("3"),
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
        REPOPULATION("55"),

        HABITATS_FLORA_AND_FAUNA("11"),
        HABITATS_CROPS_LIVESTOCK_ETC("31"),
        HABITATS_PUBLIC_HEALTH_AND_SAFETY("41"),
        HABITATS_RESEARCH("51"),
        HABITATS_REPOPULATION("52");

        public final String label;

        ReasonsType(String label) {
            this.label = label;
        }
    }

    /* package */ enum ActivitiesType {
        KILLING("1"),
        CAPTURE_FOR_CAPTIVITY("2"),
        CAPTURE_AND_IMMEDIATE_RELEASE("3"),
        DESTROY_NESTS_OR_EGGS("4"),
        TAKE_EGGS("5"),
        DISTURB("6"),
        KEEP_PROHIBITED_SPECIES("7"),
        SALE("8"),

        HABITATS_ANIMALS_KILLING("10"),
        HABITATS_ANIMALS_CAPTURE_FOR_CAPTIVITY("20"),
        HABITATS_ANIMALS_CAPTURE_AND_IMMEDIATE_RELEASE("30"),
        HABITATS_ANIMALS_DISTURB("40"),
        HABITATS_ANIMALS_DESTROY_EGGS("50"),
        HABITATS_ANIMALS_DESTROY_BREEDING_SITES("60"),
        HABITATS_ANIMALS_SALE("70"),

        HABITATS_PLANTS_PICKING("80"),
        HABITATS_PLANTS_SALE("90");

        public final String label;

        ActivitiesType(String label) {
            this.label = label;
        }
    }

    interface MethodsType {
        String getLabel();
        boolean isForbiddenMethodsJustification();
    }

    // Package private for testing
    /* package */ enum BirdMethodsType implements MethodsType {    // Order is meaningful:
        FORBIDDEN("2"),                 // The highest priority
        NON_SELECTIVE("1"),
        SELECTIVE_OR_NOT_RELEVANT("0"); // The lowest priority

        private final String label;

        BirdMethodsType(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isForbiddenMethodsJustification() {
            return this == FORBIDDEN || this == NON_SELECTIVE;
        }
    }

    // Package private for testing
    /* package */ enum HabitatsMethodsType implements MethodsType {    // Order is meaningful:
        CRAYFISH_EXPLOSIVES_AND_POISON("14"),                 // The highest priority
        FISH_EXPLOSIVES_ETC("13"),
        MAMMALS_FORBIDDEN("12"),
        INDISCRIMINATE("11"),
        NOT_LISTED_OR_NOT_RELEVANT("10"); // The lowest priority

        private final String label;

        HabitatsMethodsType(String label) {
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean isForbiddenMethodsJustification() {
            return this == MAMMALS_FORBIDDEN || this == INDISCRIMINATE;
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

    public static DERO_Derogations generateXml(
            final GameSpecies species,
            final List<HarvestPermitSpeciesAmount> amounts,
            final Map<Long, Integer> harvestAmounts,
            final Map<Long, HabidesNestRemovalAmount> nestEggConstructionAmounts,
            final Map<Long, List<PermitDecisionDerogationReason>> reasons,
            final Map<Long, Map<Integer, List<PermitDecisionForbiddenMethod>>> methods,
            final Map<Long, String> locations,
            final Map<Long, String> nestLocations,
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
            final boolean isNestRemovalPermit =
                    PermitTypeCode.isNestRemovalPermitTypeCode(amount.getHarvestPermit().getPermitTypeCode());
            final Float harvestAmount =
                    !isNestRemovalPermit ? harvestAmounts.getOrDefault(permitId, 0).floatValue() : null;
            final HabidesNestRemovalAmount nestEggConstructionAmount =
                    nestEggConstructionAmounts.getOrDefault(permitId, new HabidesNestRemovalAmount(null, null, null));
            final String location = isNestRemovalPermit ? nestLocations.get(applicationId) : locations.get(applicationId);
            final List<PermitDecisionForbiddenMethod> methodList =
                    methods.getOrDefault(decisionId, Collections.emptyMap())
                            .getOrDefault(species.getOfficialCode(), Collections.emptyList());

            derogations.withDerogation(
                    createDerogation(f,
                            species,
                            amount,
                            harvestAmount,
                            nestEggConstructionAmount.getNestAmount(),
                            nestEggConstructionAmount.getEggAmount(),
                            nestEggConstructionAmount.getConstructionAmount(),
                            reasons.get(decisionId),
                            methodList,
                            location,
                            nutsAreas,
                            authorities));
        }

        return derogations;
    }

    private static DERO_Derogation createDerogation(
            final ObjectFactory f,
            final GameSpecies species,
            final HarvestPermitSpeciesAmount amount,
            final Float harvestedAmount,
            final Integer usedNestAmount,
            final Integer usedEggAmount,
            final Integer usedConstructionAmount,
            final List<PermitDecisionDerogationReason> reasons,
            final List<PermitDecisionForbiddenMethod> methods,
            final String location,
            final Map<String, String> nutsAreas,
            final Map<Long, String> authorities) {

        final DERO_Derogation derogation = f.createDERO_Derogation();

        final HarvestPermit permit = amount.getHarvestPermit();
        final PermitDecision decision = permit.getPermitDecision();
        final Riistanhoitoyhdistys rhy = permit.getRhy();
        final String authorityName = authorities.get(rhy.getRiistakeskuksenAlue().getId());
        final boolean isBirdPermitSpecies = GameSpecies.isBirdPermitSpecies(species.getOfficialCode());
        final String reference = isBirdPermitSpecies ? "FIMMM-B-1-" : "FIMMM-H-1-";
        final DERO_DirectiveType directive = isBirdPermitSpecies ? DERO_DirectiveType.HTTP_ROD_EIONET_EUROPA_EU_OBLIGATIONS_276 :
                DERO_DirectiveType.HTTP_ROD_EIONET_EUROPA_EU_OBLIGATIONS_268;
        final String rhyOfficialCode = MergedRhyMapping.translateIfMerged(rhy.getOfficialCode());

        return derogation
                // attributes
                .withCountry("FI")
                .withUserDerogationRef(permit.getPermitNumber())
                .withUserIdentity("FIMMM")
                .withDerogationReference(reference + permit.getPermitNumber() + "-" + species.getOfficialCode())
                .withDirective(directive)
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
                .withRegions(f.createDERO_Regions().withRegion(nutsAreas.get(rhyOfficialCode)))
                .withLocation(location)
                .withDerogationJustifications(createDerogationJustificationsType(f, decision, methods, species.getOfficialCode()))
                .withReasons(createReasons(f, reasons, species.getOfficialCode()))
                .withDerogationJustificationDetails("")
                .withStrictlySupervisedConditions("")
                .withSelectiveBasis("")
                .withSmallNumberIndividuals("")
                .withActivities(createActivities(f, species.getOfficialCode(), permit.getPermitTypeCode()))
                .withAdditionalActivities(f.createDERO_AdditionalActivities())
                .withActivitiesFurtherDetails("")
                .withMethods(createMethods(f, methods, species.getOfficialCode()))
                .withFurtherDetails(methodsAsString(methods))
                .withModesOfTransport(createModesOfTransport(f, decision))
                .withLicensed(createDerogationEntity(f, amount.getSpecimenAmount(), amount.getNestAmount(), amount.getEggAmount(), amount.getConstructionAmount()))
                .withActuallyTaken(createDerogationEntity(f, harvestedAmount, usedNestAmount, usedEggAmount, usedConstructionAmount))
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
            final PermitDecision decision,
            final List<PermitDecisionForbiddenMethod> methods,
            final int speciesCode) {
        DERO_DerogationJustificationsType justifications = f.createDERO_DerogationJustificationsType()
                .withDerogationJustification(DerogationJustificationsType.REPRODUCTIVE_SEASON.label);

        final boolean isForbiddenMethodJustification = methods.stream()
                .filter(method -> isForbiddenMethodJustification(method, speciesCode)).findAny().isPresent();

        if (decision.isLegalSection32() || isForbiddenMethodJustification) {
            justifications.withDerogationJustification(DerogationJustificationsType.TRANSPORT_OR_FORBIDDEN_METHOD.label);
        }

        return justifications;
    }

    private static boolean isForbiddenMethodJustification(final PermitDecisionForbiddenMethod method, final int speciesCode) {
        final MethodsType methodsType = mapMethod(method.getMethod().toString(), speciesCode);
        return methodsType.isForbiddenMethodsJustification();
    }

    // Package private for testing
    /* package */ static DERO_Reasons createReasons(
            final ObjectFactory f,
            final List<PermitDecisionDerogationReason> derogationReasons,
            final int speciesCode) {
        final DERO_Reasons reasons = f.createDERO_Reasons();

        if (derogationReasons != null) {
            reasons.withReason(
                    derogationReasons
                            .stream()
                            .map(value -> mapReason(value.getReasonType().toString(), speciesCode))
                            .filter(Objects::nonNull)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList()));
        }
        return reasons;
    }

    private static String mapReason(final String derogationReason, final int speciesCode) {
        final ImmutableMap<String, ReasonsType> reasonMap = GameSpecies.isBirdPermitSpecies(speciesCode) ?
                birdReasonMap : habitatsReasonMap;
        if (!reasonMap.containsKey(derogationReason)) {
            return null;
        }
        return reasonMap.get(derogationReason).label;
    }

    // Package private for testing
    /* package */ static DERO_Activities createActivities(final ObjectFactory f,
                                                          final int speciesCode,
                                                          final String permitTypeCode) {
        final DERO_Activities activities = f.createDERO_Activities();
        final boolean isNestRemovalPermit = PermitTypeCode.isNestRemovalPermitTypeCode(permitTypeCode);

        if (GameSpecies.isBirdPermitSpecies(speciesCode)) {
            activities.withActivity(isNestRemovalPermit ?
                    ActivitiesType.DESTROY_NESTS_OR_EGGS.label :
                    ActivitiesType.KILLING.label);
        } else {
            activities.withActivity(isNestRemovalPermit ?
                    ActivitiesType.HABITATS_ANIMALS_DESTROY_BREEDING_SITES.label :
                    ActivitiesType.HABITATS_ANIMALS_KILLING.label);
        }

        return activities;
    }

    // Package private for testing
    /* package */ static DERO_Methods createMethods(
            final ObjectFactory f,
            final List<PermitDecisionForbiddenMethod> forbiddenMethods,
            final int speciesCode) {

        final DERO_Methods methods = f.createDERO_Methods();
        final boolean isBirdPermitSpecies = GameSpecies.isBirdPermitSpecies(speciesCode);

        if (forbiddenMethods != null && !forbiddenMethods.isEmpty()) {
            forbiddenMethods
                    .stream()
                    .map(value -> mapMethod(value.getMethod().toString(), speciesCode))
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .map(value -> value.getLabel())
                    .findFirst()
                    .ifPresent(method -> methods.withMethod(method));
        } else {
            methods.withMethod(isBirdPermitSpecies ?
                    BirdMethodsType.SELECTIVE_OR_NOT_RELEVANT.label :
                    HabitatsMethodsType.NOT_LISTED_OR_NOT_RELEVANT.label);
        }

        return methods;
    }

    private static MethodsType mapMethod(final String forbiddenMethod, final int speciesCode) {
        if (GameSpecies.isBirdPermitSpecies(speciesCode)) {
            if (!birdMethodMap.containsKey(forbiddenMethod)) {
                return null;
            }
            return birdMethodMap.get(forbiddenMethod);
        } else {
            if (!habitatsMethodMap.containsKey(forbiddenMethod)) {
                return null;
            }
            return habitatsMethodMap.get(forbiddenMethod);
        }
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
            final Float individuals,
            final Integer nests,
            final Integer eggs,
            final Integer breeding) {
        final String individualsStr = Optional.ofNullable(individuals)
                .map(amount -> BigDecimal.valueOf(amount).setScale(0, BigDecimal.ROUND_DOWN).toString())
                .orElse("");
        final String eggsStr = Optional.ofNullable(eggs)
                .map(amount -> eggs.toString())
                .orElse("");
        final String nestsStr = Optional.ofNullable(nests)
                .map(amount -> nests.toString())
                .orElse("");
        final String breedingStr = Optional.ofNullable(breeding)
                .map(amount -> breeding.toString())
                .orElse("");
        final String noFigure = individuals == null && nests == null && eggs == null && breeding == null ?
                "true" : "false";
        return f.createDERO_DerogationEntity()
                .withIndividuals(individualsStr)
                .withEggs(eggsStr)
                .withNests(nestsStr)
                .withBreeding(breedingStr)
                .withOtherType("")
                .withNoFigureProvided(noFigure)
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
