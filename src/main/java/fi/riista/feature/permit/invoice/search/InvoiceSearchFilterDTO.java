package fi.riista.feature.permit.invoice.search;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.invoice.InvoiceType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;
import java.util.stream.Stream;

public class InvoiceSearchFilterDTO implements HasBeginAndEndDate {

    private Integer applicationNumber;
    private Integer invoiceNumber;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String creditorReference;

    private InvoiceType type;
    private InvoiceDeliveryType deliveryType;

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
