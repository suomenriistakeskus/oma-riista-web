package fi.riista.feature.permit.decision;

import fi.riista.util.DateUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class PermitDecisionPaymentAmount {
    /*package*/ static final BigDecimal NO_PAYMENT = new BigDecimal("0.00");

    public static BigDecimal getDefaultPaymentAmount(final @Nonnull PermitDecision permitDecision) {
        requireNonNull(permitDecision);

        return getDefaultPaymentAmount(permitDecision.getDecisionType(), permitDecision.getPermitTypeCode());
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

        final int currentYear = DateUtil.currentYear();
        return getPaymentAmountForYear(decisionType, permitTypeCode, currentYear);
    }

    public static BigDecimal getPaymentAmountForYear(final @Nonnull PermitDecision.DecisionType decisionType,
                                                     final @Nonnull String permitTypeCode,
                                                     final int year) {
        final PermitDecisionPaymentAmountCommon paymentAmount = year >= 2022 ?
                new PermitDecisionPaymentAmount2022() :
                new PermitDecisionPaymentAmount2021();

        return paymentAmount.getPaymentAmount(decisionType, permitTypeCode);
    }

    private PermitDecisionPaymentAmount() {
        throw new AssertionError();
    }
}
