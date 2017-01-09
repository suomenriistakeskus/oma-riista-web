package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

public class HuntingClubPermitTotalPaymentDTO extends HuntingClubPermitPaymentDTO {

    private Long huntingClubId;

    private String iban;
    private String bic;
    private String recipientName;
    private String creditorReference;
    private BigDecimal adultPrice;
    private BigDecimal youngPrice;

    private BigDecimal adultsPayment;
    private BigDecimal youngPayment;

    private LocalDate dueDate;

    private int adultsCount;
    private int adultsNotEdibleCount;
    private int youngCount;
    private int youngNotEdibleCount;

    @Override
    @JsonGetter
    public BigDecimal getTotalPayment() {
        return adultsPayment.add(youngPayment);
    }

    // accessors

    @Override
    public Long getHuntingClubId() {
        return huntingClubId;
    }

    @Override
    public void setHuntingClubId(Long huntingClubId) {
        this.huntingClubId = huntingClubId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getCreditorReference() {
        return creditorReference;
    }

    public void setCreditorReference(String creditorReference) {
        this.creditorReference = creditorReference;
    }

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(BigDecimal adultPrice) {
        this.adultPrice = adultPrice;
    }

    public BigDecimal getYoungPrice() {
        return youngPrice;
    }

    public void setYoungPrice(BigDecimal youngPrice) {
        this.youngPrice = youngPrice;
    }

    @Override
    public BigDecimal getAdultsPayment() {
        return adultsPayment;
    }

    @Override
    public void setAdultsPayment(BigDecimal adultsPayment) {
        this.adultsPayment = adultsPayment;
    }

    @Override
    public BigDecimal getYoungPayment() {
        return youngPayment;
    }

    @Override
    public void setYoungPayment(BigDecimal youngPayment) {
        this.youngPayment = youngPayment;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(int adultsCount) {
        this.adultsCount = adultsCount;
    }

    public int getAdultsNotEdibleCount() {
        return adultsNotEdibleCount;
    }

    public void setAdultsNotEdibleCount(int adultsNotEdibleCount) {
        this.adultsNotEdibleCount = adultsNotEdibleCount;
    }

    public int getYoungCount() {
        return youngCount;
    }

    public void setYoungCount(int youngCount) {
        this.youngCount = youngCount;
    }

    public int getYoungNotEdibleCount() {
        return youngNotEdibleCount;
    }

    public void setYoungNotEdibleCount(int youngNotEdibleCount) {
        this.youngNotEdibleCount = youngNotEdibleCount;
    }
}
