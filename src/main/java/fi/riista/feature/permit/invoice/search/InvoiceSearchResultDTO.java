package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.math.BigDecimal;

import static fi.riista.feature.permit.invoice.search.InvoiceDeliveryType.EMAIL;
import static fi.riista.feature.permit.invoice.search.InvoiceDeliveryType.LETTER;
import static java.util.Objects.requireNonNull;

public class InvoiceSearchResultDTO implements Serializable {

    public static InvoiceSearchResultDTO create(@Nonnull final Invoice invoice,
                                                @Nonnull final Address recipientAddress) {
        requireNonNull(invoice, "invoice is null");
        requireNonNull(recipientAddress, "recipientAddress is null");

        final InvoiceSearchResultDTO dto = new InvoiceSearchResultDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setType(invoice.getType());
        dto.setDeliveryType(invoice.isElectronicInvoicingEnabled() ? EMAIL : LETTER);
        dto.setState(invoice.getDisplayState());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaymentAmount(invoice.getAmount());
        dto.setCreditorReference(invoice.getCreditorReference().toString());
        dto.setInvoiceRecipientName(invoice.getRecipientName());
        dto.setInvoiceRecipientAddress(AddressDTO.from(recipientAddress));
        return dto;
    }

    private long id;

    private InvoiceType type;
    private InvoiceDeliveryType deliveryType;
    private InvoiceDisplayState state;

    private int invoiceNumber;

    private LocalDate invoiceDate;
    private LocalDate dueDate;

    private BigDecimal paymentAmount;

    private String creditorReference;

    private String invoiceRecipientName;
    private AddressDTO invoiceRecipientAddress;

    public Long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public InvoiceType getType() {
        return type;
    }

    public void setType(final InvoiceType type) {
        this.type = type;
    }

    public InvoiceDeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(final InvoiceDeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public InvoiceDisplayState getState() {
        return state;
    }

    public void setState(final InvoiceDisplayState state) {
        this.state = state;
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

    public String getInvoiceRecipientName() {
        return invoiceRecipientName;
    }

    public void setInvoiceRecipientName(final String invoiceRecipientName) {
        this.invoiceRecipientName = invoiceRecipientName;
    }

    public AddressDTO getInvoiceRecipientAddress() {
        return invoiceRecipientAddress;
    }

    public void setInvoiceRecipientAddress(final AddressDTO invoiceRecipientAddress) {
        this.invoiceRecipientAddress = invoiceRecipientAddress;
    }
}
