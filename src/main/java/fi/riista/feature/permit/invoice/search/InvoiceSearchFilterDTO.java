package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;
import java.util.stream.Stream;

public class InvoiceSearchFilterDTO implements HasBeginAndEndDate {

    public static InvoiceSearchFilterDTO byInvoiceNumberOf(final Invoice invoice) {
        final InvoiceSearchFilterDTO dto = new InvoiceSearchFilterDTO();
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        return dto;
    }

    public static InvoiceSearchFilterDTO byCreditorReferenceOf(final Invoice invoice) {
        final InvoiceSearchFilterDTO dto = new InvoiceSearchFilterDTO();
        dto.setCreditorReference(invoice.getCreditorReference().getValue());
        return dto;
    }

    public static InvoiceSearchFilterDTO byApplicationNumber(final int applicationNumber) {
        final InvoiceSearchFilterDTO dto = new InvoiceSearchFilterDTO();
        dto.setApplicationNumber(applicationNumber);
        return dto;
    }

    private Integer applicationNumber;
    private Integer invoiceNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String creditorReference;

    private InvoiceType type;
    private InvoiceDeliveryType deliveryType;
    private InvoicePaymentState paymentState;

    private Integer huntingYear;
    private Integer gameSpeciesCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rkaOfficialCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String rhyOfficialCode;

    private LocalDate beginDate;
    private LocalDate endDate;

    @AssertTrue
    public boolean isValid() {
        // At most one of the following may be non-null.
        return Stream.of(applicationNumber, invoiceNumber, creditorReference)
                .filter(Objects::nonNull)
                .count() <= 1L;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(final String creditorReference) {
        this.creditorReference = creditorReference;
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

    public InvoicePaymentState getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(final InvoicePaymentState paymentState) {
        this.paymentState = paymentState;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public String getRkaOfficialCode() {
        return rkaOfficialCode;
    }

    public void setRkaOfficialCode(final String rkaOfficialCode) {
        this.rkaOfficialCode = rkaOfficialCode;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
