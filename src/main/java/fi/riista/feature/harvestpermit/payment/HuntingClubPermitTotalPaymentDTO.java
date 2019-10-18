package fi.riista.feature.harvestpermit.payment;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.math.BigDecimal;

public class HuntingClubPermitTotalPaymentDTO {

    private int adultsCount;
    private int youngCount;

    private int adultsNotEdibleCount;
    private int youngNotEdibleCount;

    private BigDecimal adultPrice;
    private BigDecimal youngPrice;

    private BigDecimal adultsPayment;
    private BigDecimal youngPayment;

    @JsonGetter
    public BigDecimal getTotalPayment() {
        return adultsPayment.add(youngPayment);
    }

    // accessors

    public int getAdultsCount() {
        return adultsCount;
    }

    public void setAdultsCount(final int adultsCount) {
        this.adultsCount = adultsCount;
    }

    public int getYoungCount() {
        return youngCount;
    }

    public void setYoungCount(final int youngCount) {
        this.youngCount = youngCount;
    }

    public int getAdultsNotEdibleCount() {
        return adultsNotEdibleCount;
    }

    public void setAdultsNotEdibleCount(final int adultsNotEdibleCount) {
        this.adultsNotEdibleCount = adultsNotEdibleCount;
    }

    public int getYoungNotEdibleCount() {
        return youngNotEdibleCount;
    }

    public void setYoungNotEdibleCount(final int youngNotEdibleCount) {
        this.youngNotEdibleCount = youngNotEdibleCount;
    }

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public void setAdultPrice(final BigDecimal adultPrice) {
        this.adultPrice = adultPrice;
    }

    public BigDecimal getYoungPrice() {
        return youngPrice;
    }

    public void setYoungPrice(final BigDecimal youngPrice) {
        this.youngPrice = youngPrice;
    }

    public BigDecimal getAdultsPayment() {
        return adultsPayment;
    }

    public void setAdultsPayment(final BigDecimal adultsPayment) {
        this.adultsPayment = adultsPayment;
    }

    public BigDecimal getYoungPayment() {
        return youngPayment;
    }

    public void setYoungPayment(final BigDecimal youngPayment) {
        this.youngPayment = youngPayment;
    }
}
