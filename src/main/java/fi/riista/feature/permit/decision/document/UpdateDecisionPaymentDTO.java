package fi.riista.feature.permit.decision.document;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdateDecisionPaymentDTO {
    @NotNull
    private Long id;

    @NotNull
    private BigDecimal paymentAmount;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
}
