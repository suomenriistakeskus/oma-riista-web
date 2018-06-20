package fi.riista.integration.fivaldi;

import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import fi.riista.integration.fivaldi.formatter.FivaldiCurrencySumFormatter;
import fi.riista.integration.fivaldi.formatter.FivaldiIntegerFormatter;
import fi.riista.integration.fivaldi.formatter.FivaldiLocalDateFormatter;
import fi.riista.integration.fivaldi.formatter.FivaldiLongFormatter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

@Record
public class FivaldiReslasLine {

    public static final String RESLAS_RECORD_TYPE = "RESLAS";

    /** "Fivaldissa oleva yritysnumero" */
    private Integer companyNumber;

    /** "laji" */
    private String type;

    /** "asiakastunnus" */
    private String customerId;

    /** "laskun numero" */
    private Integer invoiceNumber;

    /** "hyvitettävän laskun numero", optional, not used */
    private Integer creditMemoNumber;

    /** "kausi", format is "yyyyMM" */
    private Integer season;

    /** "laskun päivä", format is "yyMMdd" */
    private LocalDate invoiceDate;

    /** "maksuehto" */
    private String paymentTermsCode;

    /** "kassa-alennuspvm", format is "yyMMdd", optional, not used */
    private LocalDate settlementDiscountDate;

    /** "kassa-alennusprosentti", no decimals, optional, not used */
    private Integer settlementDiscountPercentage;

    /** "nettoeräpäivä", format is "yyMMdd", optional, not used */
    private LocalDate netDueDate;

    /** "laskun summa" */
    private BigDecimal invoiceSum;

    /** "valuuttatunnus", optional, not used */
    private String currencyId;

    /** "valuuttasumma", optional, not used */
    private BigDecimal currencySum;

    /** "valuuttakurssi", optional, modeled as string because not used */
    private String exchangeRate;

    /** "kirjanpidon tilisaamistili" */
    private String tiliSaamisetAccountingNumber;

    /** "viitenumero" */
    private Long creditorReference;

    /** "viesti", optional, not used */
    private String message;

    /** "asiakkaan nimi1", optional, not used */
    private String customerName1;

    /** "asiakkaan nimi1", optional, not used */
    private String customerName2;

    /** "lähiosoite1", optional, not used */
    private String streetAddress1;

    /** "lähiosoite1", optional, not used */
    private String streetAddress2;

    /** "postinumero ja -toimipaikka", optional, not used */
    private String postalAddress;

    /** "maakoodi", optional, not used */
    private String countryCode;

    public FivaldiReslasLine() {
        this.type = RESLAS_RECORD_TYPE;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void updateSeason(final int year, final int month) {
        checkArgument(year >= 2000 && year < 2100);
        checkArgument(month >= 1 && month <= 12);

        setSeason(year * 100 + month);
    }

    // Accessors -->

    @Field(offset = 1, length = 6, formatter = FivaldiIntegerFormatter.class)
    public Integer getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final Integer companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Field(offset = 7, length = 6)
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Field(offset = 13, length = 10)
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    @Field(offset = 23, length = 8, formatter = FivaldiIntegerFormatter.class)
    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Field(offset = 31, length = 8, formatter = FivaldiIntegerFormatter.class)
    public Integer getCreditMemoNumber() {
        return creditMemoNumber;
    }

    public void setCreditMemoNumber(final Integer creditMemoNumber) {
        this.creditMemoNumber = creditMemoNumber;
    }

    @Field(offset = 39, length = 6, formatter = FivaldiIntegerFormatter.class)
    public Integer getSeason() {
        return season;
    }

    public void setSeason(final Integer season) {
        this.season = season;
    }

    @Field(offset = 45, length = 6, formatter = FivaldiLocalDateFormatter.class)
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    @Field(offset = 51, length = 4)
    public String getPaymentTermsCode() {
        return paymentTermsCode;
    }

    public void setPaymentTermsCode(final String paymentTermsCode) {
        this.paymentTermsCode = paymentTermsCode;
    }

    @Field(offset = 55, length = 6, formatter = FivaldiLocalDateFormatter.class)
    public LocalDate getSettlementDiscountDate() {
        return settlementDiscountDate;
    }

    public void setSettlementDiscountDate(final LocalDate settlementDiscountDate) {
        this.settlementDiscountDate = settlementDiscountDate;
    }

    @Field(offset = 61, length = 2, formatter = FivaldiIntegerFormatter.class)
    public Integer getSettlementDiscountPercentage() {
        return settlementDiscountPercentage;
    }

    public void setSettlementDiscountPercentage(final Integer settlementDiscountPercentage) {
        this.settlementDiscountPercentage = settlementDiscountPercentage;
    }

    @Field(offset = 63, length = 6, formatter = FivaldiLocalDateFormatter.class)
    public LocalDate getNetDueDate() {
        return netDueDate;
    }

    public void setNetDueDate(final LocalDate netDueDate) {
        this.netDueDate = netDueDate;
    }

    @Field(offset = 69, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getInvoiceSum() {
        return invoiceSum;
    }

    public void setInvoiceSum(final BigDecimal sum) {
        this.invoiceSum = FivaldiHelper.scaleMonetaryAmount(sum);
    }

    @Field(offset = 88, length = 3)
    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(final String currencyId) {
        this.currencyId = currencyId;
    }

    @Field(offset = 91, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getCurrencySum() {
        return currencySum;
    }

    public void setCurrencySum(final BigDecimal currencySum) {
        this.currencySum = FivaldiHelper.scaleMonetaryAmount(currencySum);
    }

    @Field(offset = 110, length = 16)
    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(final String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Field(offset = 126, length = 8)
    public String getTiliSaamisetAccountingNumber() {
        return tiliSaamisetAccountingNumber;
    }

    public void setTiliSaamisetAccountingNumber(final String tiliSaamisAccountNumber) {
        this.tiliSaamisetAccountingNumber = tiliSaamisAccountNumber;
    }

    @Field(offset = 134, length = 20, formatter = FivaldiLongFormatter.class)
    public Long getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(final Long creditorReference) {
        this.creditorReference = creditorReference;
    }

    @Field(offset = 154, length = 100)
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Field(offset = 254, length = 40)
    public String getCustomerName1() {
        return customerName1;
    }

    public void setCustomerName1(final String customerName1) {
        this.customerName1 = customerName1;
    }

    @Field(offset = 294, length = 40)
    public String getCustomerName2() {
        return customerName2;
    }

    public void setCustomerName2(final String customerName2) {
        this.customerName2 = customerName2;
    }

    @Field(offset = 334, length = 80)
    public String getStreetAddress1() {
        return streetAddress1;
    }

    public void setStreetAddress1(final String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }

    @Field(offset = 414, length = 80)
    public String getStreetAddress2() {
        return streetAddress2;
    }

    public void setStreetAddress2(final String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }

    @Field(offset = 494, length = 80)
    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(final String postalAddress) {
        this.postalAddress = postalAddress;
    }

    @Field(offset = 574, length = 2)
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }
}
