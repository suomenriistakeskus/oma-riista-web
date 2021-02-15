package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmount {
    private static final BigDecimal PRICE_MOOSELIKE = new BigDecimal("100.00");
    private static final BigDecimal PRICE_MOOSELIKE_NEW = new BigDecimal("0.00");
    private static final BigDecimal PRICE_DEROGATION = new BigDecimal("70.00");
    private static final BigDecimal PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN = new BigDecimal("200.00");
    private static final BigDecimal PRICE_OTHER = new BigDecimal("0.00");
    private static final BigDecimal NO_PAYMENT = new BigDecimal("0.00");
    private static final BigDecimal PRICE_LAW_SECTION_TEN = new BigDecimal("110.00");
    private static final BigDecimal PRICE_WEAPON_TRANSPORTATION = new BigDecimal("70.00");
    private static final BigDecimal PRICE_DISABILITY = new BigDecimal("155.00");
    private static final BigDecimal PRICE_DOG_EVENT = new BigDecimal("80.00");
    private static final BigDecimal PRICE_DEPORTATION = new BigDecimal("0.00");
    private static final BigDecimal PRICE_RESEARCH = new BigDecimal("70.00");
    private static final BigDecimal PRICE_IMPORTING = new BigDecimal("140.00");
    private static final BigDecimal PRICE_GAME_MANAGEMENT = new BigDecimal("100.00");

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
                case NEST_REMOVAL:
                    return PRICE_DEROGATION;
                case LARGE_CARNIVORE_BEAR:
                case LARGE_CARNIVORE_LYNX:
                case LARGE_CARNIVORE_LYNX_PORONHOITO:
                case LARGE_CARNIVORE_WOLF:
                    return PRICE_LARGE_CARNIVORE_KANNANHOIDOLLINEN;
                case LAW_SECTION_TEN:
                    return PRICE_LAW_SECTION_TEN;
                case WEAPON_TRANSPORTATION:
                    return PRICE_WEAPON_TRANSPORTATION;
                case DISABILITY:
                    return PRICE_DISABILITY;
                case DOG_UNLEASH:
                case DOG_DISTURBANCE:
                    return PRICE_DOG_EVENT;
                case DEPORTATION:
                    return PRICE_DEPORTATION;
                case RESEARCH:
                    return PRICE_RESEARCH;
                case IMPORTING:
                    return PRICE_IMPORTING;
                case GAME_MANAGEMENT:
                    return PRICE_GAME_MANAGEMENT;
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
