package fi.riista.integration.fivaldi;

import com.ancientprogramming.fixedformat4j.annotation.Field;
import com.ancientprogramming.fixedformat4j.annotation.Record;
import fi.riista.integration.fivaldi.formatter.FivaldiCurrencySumFormatter;
import fi.riista.util.fixedformat.IntegerFormatter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;

import static fi.riista.util.fixedformat.FixedFormatHelper.scaleMonetaryAmount;

@Record
public class FivaldiRestapLine {

    public static final String RESTAP_RECORD_TYPE = "RESTAP";

    /** "Fivaldissa oleva yritysnumero" */
    private Integer companyNumber;

    /** "laji" */
    private String type;

    /** "asiakastunnus" */
    private String customerId;

    /** "laskun numero" */
    private Integer invoiceNumber;

    /** "kirjanpidon myyntitili" */
    private String salesAccountingNumber;

    /** "seurantakohde1 - projektinumero", optional (but required by Riistakeskus) */
    private String accountingMonitoringTarget1;

    /** "seurantakohde2 - kirjanpidon kustannuspaikka", optional (but required by Riistakeskus) */
    private String accountingMonitoringTarget2;

    /** "seurantakohde3 - kirjanpidon alakustannuspaikka", optional, not used */
    private String accountingMonitoringTarget3;

    /** "rivin summa, netto" */
    private BigDecimal sum;

    /** "valuuttasumma", optional, not used */
    private BigDecimal currencySum;

    /** "myyntirivin ALV-koodi", optional (but required by Riistakeskus) */
    private String vatCode;

    /** "seurantakohde4", optional (but required by Riistakeskus) */
    private String accountingMonitoringTarget4;

    /** "rivin vero" */
    private BigDecimal vatSum;

    /** "vero valuutassa" */
    private BigDecimal vatCurrencySum;

    /** "kirjanpidon verotili" */
    private String vatDebtAccountingNumber;

    public FivaldiRestapLine() {
        this.type = RESTAP_RECORD_TYPE;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public BigDecimal getTotalSum() {
        if (sum == null) {
            return vatSum;
        } else if (vatSum == null) {
            return sum;
        }
        return sum.add(vatSum);
    }

    public BigDecimal getTotalCurrencySum() {
        if (currencySum == null) {
            return vatCurrencySum;
        } else if (vatCurrencySum == null) {
            return currencySum;
        }
        return currencySum.add(vatCurrencySum);
    }

    // Accessors -->

    @Field(offset = 1, length = 6, formatter = IntegerFormatter.class)
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

    @Field(offset = 23, length = 8, formatter = IntegerFormatter.class)
    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final Integer invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Field(offset = 31, length = 8)
    public String getSalesAccountingNumber() {
        return salesAccountingNumber;
    }

    public void setSalesAccountingNumber(final String salesAccountingNumber) {
        this.salesAccountingNumber = salesAccountingNumber;
    }

    @Field(offset = 39, length = 8)
    public String getAccountingMonitoringTarget1() {
        return accountingMonitoringTarget1;
    }

    public void setAccountingMonitoringTarget1(final String target1) {
        this.accountingMonitoringTarget1 = target1;
    }

    @Field(offset = 47, length = 8)
    public String getAccountingMonitoringTarget2() {
        return accountingMonitoringTarget2;
    }

    public void setAccountingMonitoringTarget2(final String target2) {
        this.accountingMonitoringTarget2 = target2;
    }

    @Field(offset = 55, length = 8)
    public String getAccountingMonitoringTarget3() {
        return accountingMonitoringTarget3;
    }

    public void setAccountingMonitoringTarget3(final String target3) {
        this.accountingMonitoringTarget3 = target3;
    }

    @Field(offset = 63, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(final BigDecimal sum) {
        this.sum = scaleMonetaryAmount(sum);
    }

    @Field(offset = 82, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getCurrencySum() {
        return currencySum;
    }

    public void setCurrencySum(final BigDecimal sum) {
        this.currencySum = scaleMonetaryAmount(sum);
    }

    @Field(offset = 101, length = 1)
    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(final String vatCode) {
        this.vatCode = vatCode;
    }

    @Field(offset = 102, length = 8)
    public String getAccountingMonitoringTarget4() {
        return accountingMonitoringTarget4;
    }

    public void setAccountingMonitoringTarget4(final String target4) {
        this.accountingMonitoringTarget4 = target4;
    }

    @Field(offset = 110, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getVatSum() {
        return vatSum;
    }

    public void setVatSum(final BigDecimal sum) {
        this.vatSum = scaleMonetaryAmount(sum);
    }

    @Field(offset = 129, length = 19, formatter = FivaldiCurrencySumFormatter.class)
    public BigDecimal getVatCurrencySum() {
        return vatCurrencySum;
    }

    public void setVatCurrencySum(final BigDecimal sum) {
        this.vatCurrencySum = scaleMonetaryAmount(sum);
    }

    @Field(offset = 148, length = 8)
    public String getVatDebtAccountingNumber() {
        return vatDebtAccountingNumber;
    }

    public void setVatDebtAccountingNumber(final String vatDebtAccountingNumber) {
        this.vatDebtAccountingNumber = vatDebtAccountingNumber;
    }
}
