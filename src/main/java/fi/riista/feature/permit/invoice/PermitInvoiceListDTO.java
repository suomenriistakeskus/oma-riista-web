package fi.riista.feature.permit.invoice;

import org.joda.time.LocalDate;

public class PermitInvoiceListDTO {

    private Long id;
    private InvoiceType invoiceType;
    private String invoiceDescription;
    private Integer invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private String amount;
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

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final Integer invoiceNumber) {
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
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
