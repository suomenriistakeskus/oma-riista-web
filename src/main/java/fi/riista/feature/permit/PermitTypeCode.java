package fi.riista.feature.permit;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.decision.PermitDecisionName;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class PermitTypeCode {
    public static final String MOOSELIKE = "100";
    public static final String MOOSELIKE_AMENDMENT = "190";
    public static final String FOWL_AND_UNPROTECTED_BIRD = "305";
    public static final String ANNUAL_UNPROTECTED_BIRD = "346";
    public static final String BEAR_DAMAGE_BASED = "202";
    public static final String BEAR_KANNAHOIDOLLINEN = "207";
    public static final String LYNX_DAMAGE_BASED = "203";
    public static final String LYNX_KANNANHOIDOLLINEN = "208";
    public static final String WOLF_DAMAGE_BASED = "204";
    public static final String WOLF_KANNANHOIDOLLINEN = "209";
    public static final String WOLVERINE_DAMAGE_BASED = "211";

    public static final String MAMMAL_DAMAGE_BASED = "215";

    public static final String NEST_REMOVAL_BASED = "615";

    public static final String LAW_SECTION_TEN_BASED = "255";
    public static final String EUROPEAN_BEAVER = "250";
    public static final String PARTRIDGE = "253";

    public static final String WEAPON_TRANSPORTATION_BASED = "380";

    public static final String IMPORTING = "360";

    public static final String DISABILITY_BASED = "710";

    public static final String DOG_UNLEASH_BASED = "700";
    public static final String DOG_DISTURBANCE_BASED = "830";

    public static final String DEPORTATION = "395";
    public static final String RESEARCH = "396";

    public static final String GAME_MANAGEMENT = "512";

    public static final String FORBIDDEN_METHODS = "370";

    // These permit types possibly have permitted methods which are otherwise illegal.
    private static final Set<String> PERMITTED_METHOD_ALLOWED = ImmutableSet.of("300", "305", "310", "345", ANNUAL_UNPROTECTED_BIRD,
            FORBIDDEN_METHODS);

    // XXX: Legacy option for harvest reporting. Consider removing.
    private static final Set<String> PERMIT_TYPES_AS_LIST = ImmutableSet.of("200", "210", EUROPEAN_BEAVER, "251",
            PARTRIDGE, "300", "305", "310", "345", ANNUAL_UNPROTECTED_BIRD, FORBIDDEN_METHODS);

    public static final Set<String> DEROGATION_PERMIT_CODES = ImmutableSet.of("200", BEAR_DAMAGE_BASED, LYNX_DAMAGE_BASED,
            "204", "206", BEAR_KANNAHOIDOLLINEN, LYNX_KANNANHOIDOLLINEN, WOLF_KANNANHOIDOLLINEN, "210",
            WOLVERINE_DAMAGE_BASED, FOWL_AND_UNPROTECTED_BIRD, "345", ANNUAL_UNPROTECTED_BIRD, FORBIDDEN_METHODS,
            MAMMAL_DAMAGE_BASED, NEST_REMOVAL_BASED);

    public static final Set<String> CARNIVORE_PERMIT_CODES = ImmutableSet.of(BEAR_KANNAHOIDOLLINEN,
            WOLF_KANNANHOIDOLLINEN, LYNX_KANNANHOIDOLLINEN, MAMMAL_DAMAGE_BASED);

    public static final Set<String> HAS_NO_SPECIES_AMOUNTS = ImmutableSet.of(
            WEAPON_TRANSPORTATION_BASED, DISABILITY_BASED, DOG_UNLEASH_BASED, DOG_DISTURBANCE_BASED
    );

    public static final Set<String> CANNOT_LINK_HARVESTS = ImmutableSet.of(MOOSELIKE, MOOSELIKE_AMENDMENT,
            NEST_REMOVAL_BASED, WEAPON_TRANSPORTATION_BASED, IMPORTING, DISABILITY_BASED, DOG_UNLEASH_BASED,
            DOG_DISTURBANCE_BASED, DEPORTATION, RESEARCH, GAME_MANAGEMENT);

    @Nonnull
    public static String getPermitTypeCode(final HarvestPermitCategory permitCategory, final Integer validityYears) {
        switch (permitCategory) {
            case MOOSELIKE:
                return PermitTypeCode.MOOSELIKE;
            case MOOSELIKE_NEW:
                return PermitTypeCode.MOOSELIKE_AMENDMENT;
            case BIRD:
                return Objects.requireNonNull(validityYears, "Validity years not specified") > 0
                        ? PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD
                        : PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;

            case LARGE_CARNIVORE_BEAR:
                return PermitTypeCode.BEAR_KANNAHOIDOLLINEN;
            case LARGE_CARNIVORE_LYNX:
            case LARGE_CARNIVORE_LYNX_PORONHOITO:
                return PermitTypeCode.LYNX_KANNANHOIDOLLINEN;
            case LARGE_CARNIVORE_WOLF:
            case LARGE_CARNIVORE_WOLF_PORONHOITO:
                return PermitTypeCode.WOLF_KANNANHOIDOLLINEN;
            case MAMMAL:
                return MAMMAL_DAMAGE_BASED;
            case NEST_REMOVAL:
                return NEST_REMOVAL_BASED;
            case LAW_SECTION_TEN:
                return LAW_SECTION_TEN_BASED;
            case WEAPON_TRANSPORTATION:
                return WEAPON_TRANSPORTATION_BASED;
            case DISABILITY:
                return DISABILITY_BASED;
            case DOG_UNLEASH:
                return DOG_UNLEASH_BASED;
            case DOG_DISTURBANCE:
                return DOG_DISTURBANCE_BASED;
            case DEPORTATION:
                return DEPORTATION;
            case RESEARCH:
                return RESEARCH;
            case IMPORTING:
                return IMPORTING;
            case GAME_MANAGEMENT:
                return GAME_MANAGEMENT;
            case EUROPEAN_BEAVER:
                return EUROPEAN_BEAVER;
            case PARTRIDGE:
                return PARTRIDGE;
            default:
                throw new IllegalArgumentException("Unsupported permit category: " + permitCategory);
        }
    }

    public static boolean isMooselikePermitTypeCode(final String permitTypeCode) {
        return MOOSELIKE.equals(permitTypeCode);
    }

    public static boolean isAmendmentPermitTypeCode(final String permitTypeCode) {
        return MOOSELIKE_AMENDMENT.equals(permitTypeCode);
    }

    public static boolean isAnnualUnprotectedBird(final String permitTypeCode) {
        return ANNUAL_UNPROTECTED_BIRD.equals(permitTypeCode);
    }

    public static boolean isPermittedMethodAllowed(final String permitTypeCode) {
        return PERMITTED_METHOD_ALLOWED.contains(permitTypeCode);
    }

    public static boolean isDerogationPermitType(final String permitTypeCode) {
        return DEROGATION_PERMIT_CODES.contains(permitTypeCode);
    }

    public static boolean isNestRemovalPermitTypeCode(final String permitTypeCode) {
        return NEST_REMOVAL_BASED.equals(permitTypeCode);
    }

    public static boolean isWeaponTransportationPermitTypeCode(final String permitTypeCode) {
        return WEAPON_TRANSPORTATION_BASED.equals(permitTypeCode);
    }

    public static boolean isDisabilityPermitTypeCode(final String permitTypeCode) {
        return DISABILITY_BASED.equals(permitTypeCode);
    }

    public static boolean isGameManagementPermitTypeCode(final String permitTypeCode) {
        return GAME_MANAGEMENT.equals(permitTypeCode);
    }

    public static boolean hasSpecies(final String permitTypeCode) {
        return !HAS_NO_SPECIES_AMOUNTS.contains(permitTypeCode);
    }

    public static boolean canLinkHarvests(final String permitTypeCode) {
        return !CANNOT_LINK_HARVESTS.contains(permitTypeCode);
    }

    // Used for import

    public static boolean checkIsHarvestsAsList(final String permitTypeCode) {
        return PERMIT_TYPES_AS_LIST.contains(permitTypeCode);
    }

    public static boolean checkShouldResolvePermitHolder(final String permitTypeCode) {
        return MOOSELIKE.equals(permitTypeCode);
    }

    public static boolean checkShouldResolvePermitPartners(final String permitTypeCode) {
        return MOOSELIKE.equals(permitTypeCode);
    }

    public static LocalisedString getDecisionName(final String permitTypeCode) {
        switch (permitTypeCode) {
            case MOOSELIKE:
                return PermitDecisionName.MOOSELIKE;
            case MOOSELIKE_AMENDMENT:
                return PermitDecisionName.MOOSELIKE_AMENDMENT;
            case FOWL_AND_UNPROTECTED_BIRD:
            case ANNUAL_UNPROTECTED_BIRD:
                return PermitDecisionName.BIRD;
            case MAMMAL_DAMAGE_BASED:
                return PermitDecisionName.MAMMAL;
            case NEST_REMOVAL_BASED:
                return PermitDecisionName.NEST_REMOVAL;
            case BEAR_KANNAHOIDOLLINEN:
            case LYNX_KANNANHOIDOLLINEN:
            case WOLF_KANNANHOIDOLLINEN:
                return PermitDecisionName.LARGE_CARNIVORE;
            case LAW_SECTION_TEN_BASED:
            case EUROPEAN_BEAVER:
            case PARTRIDGE:
                return PermitDecisionName.LAW_SECTION_TEN;
            case WEAPON_TRANSPORTATION_BASED:
                return PermitDecisionName.WEAPON_TRANSPORTATION;
            case DISABILITY_BASED:
                return PermitDecisionName.DISABILITY;
            case DOG_UNLEASH_BASED:
                return PermitDecisionName.DOG_UNLEASH;
            case DOG_DISTURBANCE_BASED:
                return PermitDecisionName.DOG_DISTURBANCE;
            case DEPORTATION:
                return PermitDecisionName.DEPORTATION;
            case RESEARCH:
                return PermitDecisionName.RESEARCH;
            case IMPORTING:
                return PermitDecisionName.IMPORTING;
            case GAME_MANAGEMENT:
                return PermitDecisionName.GAME_MANAGEMENT;
            case FORBIDDEN_METHODS:
                return PermitDecisionName.FORBIDDEN_METHOD;
            default:
                throw new IllegalArgumentException("Unsupported permit type code:" + permitTypeCode);
        }
    }
}
