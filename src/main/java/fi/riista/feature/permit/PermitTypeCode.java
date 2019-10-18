package fi.riista.feature.permit;

import com.google.common.collect.ImmutableSet;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class PermitTypeCode {
    public static final String MOOSELIKE = "100";
    public static final String MOOSELIKE_AMENDMENT = "190";
    public static final String FOWL_AND_UNPROTECTED_BIRD = "305";
    public static final String ANNUAL_UNPROTECTED_BIRD = "346";
    public static final String BEAR_KANNAHOIDOLLINEN = "207";
    public static final String LYNX_KANNANHOIDOLLINEN = "208";
    public static final String WOLF_KANNANHOIDOLLINEN = "209";
    public static final String WOLVERINE_DAMAGE_BASED = "211";

    // These permit types possibly have permitted methods which are otherwise illegal.
    private static final Set<String> PERMITTED_METHOD_ALLOWED = ImmutableSet.of("300", "305", "310", "345", "346",
            "370");

    // XXX: Legacy option for harvest reporting. Consider removing.
    private static final Set<String> PERMIT_TYPES_AS_LIST = ImmutableSet.of("200", "210", "250", "251", "253", "300", "305", "310", "345", "346", "370");

    public static final Set<String> DEROGATION_PERMIT_CODES = ImmutableSet.of("200", "202", "203", "204", "206",
            "207", "208", "209", "210", "211", "305", "345", "346", "370");

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
                return PermitTypeCode.WOLF_KANNANHOIDOLLINEN;
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
}
