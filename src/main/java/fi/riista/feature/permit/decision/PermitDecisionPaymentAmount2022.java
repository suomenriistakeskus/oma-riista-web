package fi.riista.feature.permit.decision;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

import static fi.riista.feature.permit.PermitTypeCode.DISABILITY_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_DISTURBANCE_BASED;
import static fi.riista.feature.permit.PermitTypeCode.DOG_UNLEASH_BASED;
import static fi.riista.feature.permit.PermitTypeCode.EUROPEAN_BEAVER;
import static fi.riista.feature.permit.PermitTypeCode.GAME_MANAGEMENT;
import static fi.riista.feature.permit.PermitTypeCode.IMPORTING;
import static fi.riista.feature.permit.PermitTypeCode.LAW_SECTION_TEN_BASED;
import static fi.riista.feature.permit.PermitTypeCode.MOOSELIKE;
import static fi.riista.feature.permit.PermitTypeCode.PARTRIDGE;
import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmount2022 extends PermitDecisionPaymentAmountCommon {
    /*package*/ static final BigDecimal PRICE_MOOSELIKE = new BigDecimal("140.00");
    /*package*/ static final BigDecimal PRICE_LAW_SECTION_TEN = new BigDecimal("160.00");
    /*package*/ static final BigDecimal PRICE_DISABILITY = new BigDecimal("130.00");
    /*package*/ static final BigDecimal PRICE_DOG_EVENT = new BigDecimal("100.00");
    /*package*/ static final BigDecimal PRICE_IMPORTING = new BigDecimal("210.00");
    /*package*/ static final BigDecimal PRICE_GAME_MANAGEMENT = new BigDecimal("70.00");

    public BigDecimal getPaymentAmount(final @Nonnull PermitDecision.DecisionType decisionType,
                                              final @Nonnull String permitTypeCode) {
        requireNonNull(decisionType);
        requireNonNull(permitTypeCode);

        if (decisionType == PermitDecision.DecisionType.HARVEST_PERMIT) {
            switch (permitTypeCode) {
                case MOOSELIKE:
                    return PRICE_MOOSELIKE;
                case LAW_SECTION_TEN_BASED:
                case EUROPEAN_BEAVER:
                case PARTRIDGE:
                    return PRICE_LAW_SECTION_TEN;
                case DISABILITY_BASED:
                    return PRICE_DISABILITY;
                case DOG_UNLEASH_BASED:
                case DOG_DISTURBANCE_BASED:
                    return PRICE_DOG_EVENT;
                case IMPORTING:
                    return PRICE_IMPORTING;
                case GAME_MANAGEMENT:
                    return PRICE_GAME_MANAGEMENT;
                default:
                    return super.getPaymentAmount(decisionType, permitTypeCode);
            }
        }

        return super.getPaymentAmount(decisionType, permitTypeCode);
    }

}
