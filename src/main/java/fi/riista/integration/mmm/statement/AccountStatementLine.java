package fi.riista.integration.mmm.statement;

import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import fi.riista.integration.mmm.statement.formatter.AccountStatementAmountFormatter;
import fi.riista.util.fixedformat.FixedFormatHelper;
import fi.riista.util.fixedformat.IntegerFormatter;
import fi.riista.util.fixedformat.LocalDateFormatter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

@Record
public class AccountStatementLine {

    /** "aineistotunnus"; 3 = suoraveloitus, 5 = viitesiirto */
    private String transactionType;

    /** "laskuttajan tilinumero" */
    private String creditorAccountNumber;

    /**
     * "kirjauspvm" - presumably the date on which the transaction is processed
     * i.e. the first business day after last cut-off time. Also known as
     * "effective date".
     */
    private LocalDate bookingDate;

    /**
     * "maksupvm" - the date on which the transaction is initiated, may not be a
     * business day.
     */
    private LocalDate transactionDate;

    /** "arkistointitunnus" */
    private String accountServiceReference;

    /** "viitenumero" */
    private String creditorReference;

    /** "maksajan nimilyhenne" */
    private String debtorNameAbbrv;

    /** "rahayksikkökoodi" (1 = EUR) */
    private Integer currencyCode;

    /** "nimen lähde" ('A', 'J', 'K') */
    private String nameOrigin;

    /** "maksun summa" */
    private BigDecimal amount;

    /** "oikaisutunnus" */
    private Integer reversalIndicator;

    /** "välitystapa" */
    private String mediationType;

    /** "palautekoodi" */
    private Integer returnCode;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final AccountStatementLine that = (AccountStatementLine) obj;

        return new EqualsBuilder()
                .append(transactionType, that.transactionType)
                .append(creditorAccountNumber, that.creditorAccountNumber)
                .append(bookingDate, that.bookingDate)
                .append(transactionDate, that.transactionDate)
                .append(accountServiceReference, that.accountServiceReference)
                .append(creditorReference, that.creditorReference)
                .append(debtorNameAbbrv, that.debtorNameAbbrv)
                .append(currencyCode, that.currencyCode)
                .append(nameOrigin, that.nameOrigin)
                .append(amount, that.amount)
                .append(reversalIndicator, that.reversalIndicator)
                .append(mediationType, that.mediationType)
                .append(returnCode, that.returnCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(transactionType)
                .append(creditorAccountNumber)
                .append(bookingDate)
                .append(transactionDate)
                .append(accountServiceReference)
                .append(creditorReference)
                .append(debtorNameAbbrv)
                .append(currencyCode)
                .append(nameOrigin)
                .append(amount)
                .append(reversalIndicator)
                .append(mediationType)
                .append(returnCode)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public Iban getCreditorAccountNumberAsIban() {
        requireNonNull(creditorAccountNumber, "creditorAccountNumber is null");

        if (creditorAccountNumber.length() != 14) {
            throw new IllegalStateException("Not valid account number");
        }

        return new Iban.Builder()
                .countryCode(CountryCode.FI)
                .bankCode(creditorAccountNumber.substring(0, 6))
                .accountNumber(creditorAccountNumber.substring(6, 13))
                .nationalCheckDigit(creditorAccountNumber.substring(13))
                .build(true);
    }

    // Accessors -->

    @Field(offset = 1, length = 1)
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }

    @Field(offset = 2, length = 14)
    public String getCreditorAccountNumber() {
        return creditorAccountNumber;
    }

    public void setCreditorAccountNumber(final String creditorAccountNumber) {
        this.creditorAccountNumber = creditorAccountNumber;
    }

    @Field(offset = 16, length = 6, formatter = LocalDateFormatter.class)
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(final LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    @Field(offset = 22, length = 6, formatter = LocalDateFormatter.class)
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(final LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Field(offset = 28, length = 16)
    public String getAccountServiceReference() {
        return accountServiceReference;
    }

    public void setAccountServiceReference(final String accountServiceReference) {
        this.accountServiceReference = accountServiceReference;
    }

    @Field(offset = 44, length = 20)
    public String getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(final String creditorReference) {
        this.creditorReference = creditorReference;
    }

    @Field(offset = 64, length = 12)
    public String getDebtorNameAbbrv() {
        return debtorNameAbbrv;
    }

    public void setDebtorNameAbbrv(final String debtorNameAbbrv) {
        this.debtorNameAbbrv = debtorNameAbbrv;
    }

    @Field(offset = 76, length = 1, formatter = IntegerFormatter.class)
    public Integer getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(final Integer currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Field(offset = 77, length = 1)
    public String getNameOrigin() {
        return nameOrigin;
    }

    public void setNameOrigin(final String nameOrigin) {
        this.nameOrigin = nameOrigin;
    }

    @Field(offset = 78, length = 10, formatter = AccountStatementAmountFormatter.class)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = FixedFormatHelper.scaleMonetaryAmount(amount);
    }

    @Field(offset = 88, length = 1)
    public Integer getReversalIndicator() {
        return reversalIndicator;
    }

    public void setReversalIndicator(final Integer reversalIndicator) {
        this.reversalIndicator = reversalIndicator;
    }

    @Field(offset = 89, length = 1)
    public String getMediationType() {
        return mediationType;
    }

    public void setMediationType(final String mediationType) {
        this.mediationType = mediationType;
    }

    @Field(offset = 90, length = 1)
    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(final Integer returnCode) {
        this.returnCode = returnCode;
    }
}
