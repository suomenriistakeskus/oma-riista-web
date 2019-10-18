package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class InvoicePaymentLineDTO extends BaseEntityDTO<Long> implements Serializable {

    public static InvoicePaymentLineDTO create(@Nonnull final InvoicePaymentLine payment,
                                               @Nullable final SystemUser moderator) {
        requireNonNull(payment);

        final InvoicePaymentLineDTO dto = new InvoicePaymentLineDTO();
        DtoUtil.copyBaseFields(payment, dto);

        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setVerifiedFromAccountStatement(payment.getAccountTransfer() != null);

        if (moderator != null) {
            dto.setModeratorName(moderator.getFullName());
        }

        return dto;
    }

    public Long id;
    public Integer rev;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private BigDecimal amount;

    private boolean verifiedFromAccountStatement;

    private String moderatorName;

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(final LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isVerifiedFromAccountStatement() {
        return verifiedFromAccountStatement;
    }

    public void setVerifiedFromAccountStatement(final boolean verifiedFromAccountStatement) {
        this.verifiedFromAccountStatement = verifiedFromAccountStatement;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public void setModeratorName(final String moderatorName) {
        this.moderatorName = moderatorName;
    }
}
