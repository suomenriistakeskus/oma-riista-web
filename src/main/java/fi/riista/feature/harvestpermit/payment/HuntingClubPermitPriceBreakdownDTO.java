package fi.riista.feature.harvestpermit.payment;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HuntingClubPermitPriceBreakdownDTO {
    private final Map<Long, HuntingClubPermitPaymentDTO> payments;
    private final HuntingClubPermitTotalPaymentDTO totalPayment;

    public HuntingClubPermitPriceBreakdownDTO(final Map<Long, HuntingClubPermitPaymentDTO> payments,
                                              final HuntingClubPermitTotalPaymentDTO totalPayment) {
        this.payments = requireNonNull(payments);
        this.totalPayment = requireNonNull(totalPayment);
    }

    public Map<Long, HuntingClubPermitPaymentDTO> getPayments() {
        return payments;
    }

    public HuntingClubPermitTotalPaymentDTO getTotalPayment() {
        return totalPayment;
    }
}
