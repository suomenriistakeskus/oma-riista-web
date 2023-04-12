package fi.riista.feature.permit.decision;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.BEAR_KANNAHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.DEPORTATION;
import static fi.riista.feature.permit.PermitTypeCode.FORBIDDEN_METHODS;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.LYNX_KANNANHOIDOLLINEN;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE_AMENDMENT;
import static fi.riista.feature.permit.PermitTypeCode.NEST_REMOVAL_BASED;
import static fi.riista.feature.permit.PermitTypeCode.RESEARCH;
import static fi.riista.feature.permit.PermitTypeCode.WEAPON_TRANSPORTATION_BASED;
import static fi.riista.feature.permit.PermitTypeCode.WOLF_KANNANHOIDOLLINEN;
import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmountCommon {
    /*package*/ static final BigDecimal PRICE_MOOSELIKE_NEW = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_DEROGATION = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN = new BigDecimal("200.00");
    /*package*/ static final BigDecimal PRICE_OTHER = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_WEAPON_TRANSPORTATION = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_DEPORTATION = new BigDecimal("0.00");
    /*package*/ static final BigDecimal PRICE_RESEARCH = new BigDecimal("70.00");
    /*package*/ static final BigDecimal PRICE_FORBIDDEN_METHOD = new BigDecimal("70.00");

    public BigDecimal getPaymentAmount(final @Nonnull PermitDecision.DecisionType decisionType,
                                       final @Nonnull String permitTypeCode) {
        requireNonNull(decisionType);
        requireNonNull(permitTypeCode);

        if (decisionType == PermitDecision.DecisionType.HARVEST_PERMIT) {
            switch (permitTypeCode) {
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
                case WEAPON_TRANSPORTATION_BASED:
                    return PRICE_WEAPON_TRANSPORTATION;
                case DEPORTATION:
                    return PRICE_DEPORTATION;
                case RESEARCH:
                    return PRICE_RESEARCH;
                case FORBIDDEN_METHODS:
                    return PRICE_FORBIDDEN_METHOD;
                default:
                    throw new IllegalArgumentException("Unsupported permit type code:" + permitTypeCode);
            }
        }

        return PRICE_OTHER;
    }

}
