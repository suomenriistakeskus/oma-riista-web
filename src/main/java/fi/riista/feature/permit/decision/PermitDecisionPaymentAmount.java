package fi.riista.feature.permit.decision;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.BEAR_KANNAHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.DEPORTATION;
import static fi.riista.feature.permit.PermitTypeCode.DISABILITY_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_DISTURBANCE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_UNLEASH_BASED;
import static fi.riista.feature.permit.PermitTypeCode.FORBIDDEN_METHODS;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.GAME_MANAGEMENT;
import static fi.riista.feature.permit.PermitTypeCode.IMPORTING;
import static fi.riista.feature.permit.PermitTypeCode.LAW_SECTION_TEN_BASED;
import static fi.riista.feature.permit.PermitTypeCode.LYNX_KANNANHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE_AMENDMENT;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static fi.riista.feature.permit.PermitTypeCode.RESEARCH;
import static fi.riista.feature.permit.PermitTypeCode.WEAPON_TRANSPORTATION_BASED;
import static fi.riista.feature.permit.PermitTypeCode.WOLF_KANNANHOIDOLLINEN;
import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmount {
    /*package*/ static final BigDecimal PRICE_MOOSELIKE = new BigDecimal("100.00");
    /*package*/ static final BigDecimal PRICE_MOOSELIKE_NEW = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_DEROGATION = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN = new BigDecimal("200.00");
    /*package*/ static final BigDecimal PRICE_OTHER = new BigDecimal("0.00");
    /*package*/ static final BigDecimal NO_PAYMENT = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_LAW_SECTION_TEN = new BigDecimal("110.00");
    /*package*/ static final BigDecimal PRICE_WEAPON_TRANSPORTATION = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_DISABILITY = new BigDecimal("155.00");
    /*package*/ static final BigDecimal PRICE_DOG_EVENT = new BigDecimal("80.00");
    /*package*/ static final BigDecimal PRICE_DEPORTATION = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_RESEARCH = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_IMPORTING = new BigDecimal("140.00");
    /*package*/ static final BigDecimal PRICE_GAME_MANAGEMENT = new BigDecimal("100.00");
    /*package*/ static final BigDecimal PRICE_FORBIDDEN_METHOD = new BigDecimal("70.00");

    public static BigDecimal getDefaultPaymentAmount(final @Nonnull PermitDecision permitDecision) {
        requireNonNull(permitDecision);

        return getDefaultPaymentAmount(permitDecision.getDecisionType(),
                permitDecision.getPermitTypeCode());
    }

    public static List<BigDecimal> getPaymentOptionsFor(final @Nonnull PermitDecision permitDecision) {
        requireNonNull(permitDecision);

        return Stream.of(
                NO_PAYMENT,
                PermitDecisionPaymentAmount.getDefaultPaymentAmount(permitDecision))
                .distinct()
                .collect(Collectors.toList());
    }

    public static BigDecimal getDefaultPaymentAmount(final @Nonnull PermitDecision.DecisionType decisionType,
                                                     final @Nonnull String permitTypeCode) {
        requireNonNull(decisionType);
        requireNonNull(permitTypeCode);

        if (decisionType == PermitDecision.DecisionType.HARVEST_PERMIT) {
            switch (permitTypeCode) {
                case MOOSELIKE:
                    return PRICE_MOOSELIKE;
                case MOOSELIKE_AMENDMENT:
                    return PRICE_MOOSELIKE_NEW;
                case FOWL_AND_UNPROTECTED_BIRD:
                case ANNUAL_UNPROTECTED_BIRD:
                case MAMMAL_DAMAGE_BASED:
                case NEST_REMOVAL_BASED:
                    return PRICE_DEROGATION;
                case BEAR_KANNAHOIDOLLINEN:
                case LYNX_KANNANHOIDOLLINEN:
                case WOLF_KANNANHOIDOLLINEN:
                    return PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN;
                case LAW_SECTION_TEN_BASED:
                    return PRICE_LAW_SECTION_TEN;
                case WEAPON_TRANSPORTATION_BASED:
                    return PRICE_WEAPON_TRANSPORTATION;
                case DISABILITY_BASED:
                    return PRICE_DISABILITY;
                case DOG_UNLEASH_BASED:
                case DOG_DISTURBANCE_BASED:
                    return PRICE_DOG_EVENT;
                case DEPORTATION:
                    return PRICE_DEPORTATION;
                case RESEARCH:
                    return PRICE_RESEARCH;
                case IMPORTING:
                    return PRICE_IMPORTING;
                case GAME_MANAGEMENT:
                    return PRICE_GAME_MANAGEMENT;
                case FORBIDDEN_METHODS:
                    return PRICE_FORBIDDEN_METHOD;
                default:
                    throw new IllegalArgumentException("Unsupported permit type code:" + permitTypeCode);
            }
        }

        return PRICE_OTHER;
    }

    private PermitDecisionPaymentAmount() {
        throw new AssertionError();
    }
}
