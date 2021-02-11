package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.permit.invoice.InvoiceContactDetailsDTO;
import fi.riista.feature.permit.invoice.InvoiceFivaldiState;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEvent;
import fi.riista.feature.permit.invoice.InvoiceStateChangeEventType;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLineDTO;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class InvoiceModeratorDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    private InvoiceType type;
    private InvoiceDisplayState state;
    private boolean electronicInvoicingEnabled;

    private int invoiceNumber;

    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private boolean overdue;

    private BigDecimal paymentAmount;
    private BigDecimal receivedAmount;
    private boolean isPaymentAmountCorrected;

    private String creditorReference;

    private InvoiceContactDetailsDTO invoiceRecipient;

    private Long permitDecisionId;
    private String permitNumber;
    private String permitTypeCode;

    private InvoiceFivaldiState fivaldiState;

    private List<InvoicePaymentLineDTO> payments;
    private List<InvoiceActionDTO> actions;

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

    public InvoiceType getType() {
        return type;
    }

    public void setType(final InvoiceType type) {
        this.type = type;
    }

    public InvoiceDisplayState getState() {
        return state;
    }

    public void setState(final InvoiceDisplayState state) {
        this.state = state;
    }

    public boolean isElectronicInvoicingEnabled() {
        return electronicInvoicingEnabled;
    }

    public void setElectronicInvoicingEnabled(final boolean electronicInvoicingEnabled) {
        this.electronicInvoicingEnabled = electronicInvoicingEnabled;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(final LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(final boolean overdue) {
        this.overdue = overdue;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(final BigDecimal receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public boolean isPaymentAmountCorrected() {
        return isPaymentAmountCorrected;
    }

    public void setPaymentAmountCorrected(final boolean isPaymentAmountCorrected) {
        this.isPaymentAmountCorrected = isPaymentAmountCorrected;
    }

    public String getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(final String creditorReference) {
        this.creditorReference = creditorReference;
    }

    public InvoiceContactDetailsDTO getInvoiceRecipient() {
        return invoiceRecipient;
    }

    public void setInvoiceRecipient(final InvoiceContactDetailsDTO invoiceRecipient) {
        this.invoiceRecipient = invoiceRecipient;
    }

    public Long getPermitDecisionId() {
        return permitDecisionId;
    }

    public void setPermitDecisionId(final Long permitDecisionId) {
        this.permitDecisionId = permitDecisionId;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public InvoiceFivaldiState getFivaldiState() {
        return fivaldiState;
    }

    public void setFivaldiState(final InvoiceFivaldiState fivaldiState) {
        this.fivaldiState = fivaldiState;
    }

    public List<InvoicePaymentLineDTO> getPayments() {
        return payments;
    }

    public void setPayments(final List<InvoicePaymentLineDTO> payments) {
        this.payments = payments;
    }

    public List<InvoiceActionDTO> getActions() {
        return actions;
    }

    public void setActions(final List<InvoiceActionDTO> actions) {
        this.actions = actions;
    }

    public static class InvoiceActionDTO implements Serializable {

        public static InvoiceActionDTO create(@Nonnull final InvoiceStateChangeEvent event,
                                              @Nullable final SystemUser moderator) {
            requireNonNull(event);

            final InvoiceActionDTO dto = new InvoiceActionDTO();
            dto.setId(event.getId());
            dto.setType(event.getType());
            dto.setActionTime(event.getEventTime());

            if (moderator != null) {
                dto.setModeratorName(moderator.getFullName());
            }

            return dto;
        }

        private long id;
        private InvoiceStateChangeEventType type;
        private DateTime actionTime;
        private String moderatorName;

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }

        public InvoiceStateChangeEventType getType() {
            return type;
        }

        public void setType(final InvoiceStateChangeEventType type) {
            this.type = type;
        }

        public DateTime getActionTime() {
            return actionTime;
        }

        public void setActionTime(final DateTime actionTime) {
            this.actionTime = actionTime;
        }

        public String getModeratorName() {
            return moderatorName;
        }

        public void setModeratorName(final String moderatorName) {
            this.moderatorName = moderatorName;
        }
    }
}
