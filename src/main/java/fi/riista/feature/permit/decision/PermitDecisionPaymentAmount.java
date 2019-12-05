package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmount {
    private static final BigDecimal PRICE_MOOSELIKE = new BigDecimal("90.00");
    private static final BigDecimal PRICE_MOOSELIKE_NEW = new BigDecimal("0.00");
    private static final BigDecimal PRICE_BIRD_MAMMAL = new BigDecimal("70.00");
    private static final BigDecimal PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN = new BigDecimal("200.00");
    private static final BigDecimal PRICE_OTHER = new BigDecimal("0.00");
    private static final BigDecimal NO_PAYMENT = new BigDecimal("0.00");

    public static BigDecimal getDefaultPaymentAmount(final @Nonnull PermitDecision permitDecision) {
        requireNonNull(permitDecision);

        return getDefaultPaymentAmount(permitDecision.getDecisionType(),
                permitDecision.getApplication().getHarvestPermitCategory());
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
                                                     final @Nonnull HarvestPermitCategory permitCategory) {
        requireNonNull(decisionType);
        requireNonNull(permitCategory);

        if (decisionType == PermitDecision.DecisionType.HARVEST_PERMIT) {
            switch (permitCategory) {
                case MOOSELIKE:
                    return PRICE_MOOSELIKE;
                case MOOSELIKE_NEW:
                    return PRICE_MOOSELIKE_NEW;
                case BIRD:
                case MAMMAL:
                    return PRICE_BIRD_MAMMAL;
                case LARGE_CARNIVORE_BEAR:
                case LARGE_CARNIVORE_LYNX:
                case LARGE_CARNIVORE_LYNX_PORONHOITO:
                case LARGE_CARNIVORE_WOLF:
                    return PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN;
                default:
                    throw new IllegalArgumentException("Unsupported application category:" +
                            permitCategory);
            }
        }

        return PRICE_OTHER;
    }

    private PermitDecisionPaymentAmount() {
        throw new AssertionError();
    }
}
