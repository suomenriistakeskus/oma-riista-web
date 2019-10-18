package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.permit.invoice.InvoiceContactDetailsDTO;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

public class PermitInvoiceListDTO {

    private Long id;
    private InvoiceType invoiceType;
    private String invoiceDescription;
    private int invoiceNumber;

    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate paymentDate;

    private BigDecimal amount;
    private BigDecimal paidAmount;
    private boolean corrected;

    private InvoiceContactDetailsDTO from;
    private InvoiceContactDetailsDTO to;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(final InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceDescription() {
        return invoiceDescription;
    }

    public void setInvoiceDescription(final String invoiceDescription) {
        this.invoiceDescription = invoiceDescription;
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

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(final BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public boolean isCorrected() {
        return corrected;
    }

    public void setCorrected(final boolean corrected) {
        this.corrected = corrected;
    }

    public InvoiceContactDetailsDTO getFrom() {
        return from;
    }

    public void setFrom(final InvoiceContactDetailsDTO from) {
        this.from = from;
    }

    public InvoiceContactDetailsDTO getTo() {
        return to;
    }

    public void setTo(final InvoiceContactDetailsDTO to) {
        this.to = to;
    }
}
