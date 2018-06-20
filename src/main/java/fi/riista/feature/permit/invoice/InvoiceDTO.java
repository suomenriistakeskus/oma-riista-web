package fi.riista.feature.permit.invoice;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.permit.invoice.search.InvoiceDisplayState;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class InvoiceDTO extends BaseEntityDTO<Long> {

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

    private String creditorReference;

    private InvoiceContactDetailsDTO invoiceRecipient;

    private Long permitDecisionId;
    private String permitNumber;

    private InvoiceFivaldiState fivaldiState;

    private List<InvoiceEventDTO> events;

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

    public InvoiceFivaldiState getFivaldiState() {
        return fivaldiState;
    }

    public void setFivaldiState(final InvoiceFivaldiState fivaldiState) {
        this.fivaldiState = fivaldiState;
    }

    public List<InvoiceEventDTO> getEvents() {
        return events;
    }

    public void setEvents(final List<InvoiceEventDTO> events) {
        this.events = events;
    }

    public static class InvoiceEventDTO implements Serializable {

        public static InvoiceEventDTO create(@Nonnull final InvoiceStateChangeEvent event,
                                             @Nullable final SystemUser moderator) {
            requireNonNull(event);

            final InvoiceEventDTO dto = new InvoiceEventDTO();
            dto.setId(event.getId());
            dto.setType(event.getType());
            dto.setEventTime(event.getEventTime());

            if (moderator != null) {
                dto.setModeratorName(moderator.getFullName());
            }

            return dto;
        }

        private long id;
        private InvoiceStateChangeEventType type;
        private DateTime eventTime;
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

        public DateTime getEventTime() {
            return eventTime;
        }

        public void setEventTime(final DateTime eventTime) {
            this.eventTime = eventTime;
        }

        public String getModeratorName() {
            return moderatorName;
        }

        public void setModeratorName(final String moderatorName) {
            this.moderatorName = moderatorName;
        }
    }
}
