package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.harvestpermit.season.MooselikePrice;

import java.math.BigDecimal;

public class HuntingClubPermitPaymentDTO {

    public static HuntingClubPermitPaymentDTO create(HuntingClubPermitCountDTO count, MooselikePrice prices) {
        final HuntingClubPermitPaymentDTO dto = new HuntingClubPermitPaymentDTO();
        dto.setHuntingClubId(count.getHuntingClubId());

        dto.setAdultsPayment(calculatePayment(count.countAdults(), count.getNumberOfNonEdibleAdults(), prices.getAdultPrice()));
        dto.setYoungPayment(calculatePayment(count.countYoung(), count.getNumberOfNonEdibleYoungs(), prices.getYoungPrice()));

        return dto;
    }

    private static BigDecimal calculatePayment(int total, int notEdible, BigDecimal price) {
        final BigDecimal edible = new BigDecimal(total - notEdible);
        return edible.multiply(price);
    }

    private Long huntingClubId;

    private BigDecimal adultsPayment;
    private BigDecimal youngPayment;

    @JsonGetter
    public BigDecimal getTotalPayment() {
        return adultsPayment.add(youngPayment);
    }

    // accessors

    public Long getHuntingClubId() {
        return huntingClubId;
    }

    public void setHuntingClubId(Long huntingClubId) {
        this.huntingClubId = huntingClubId;
    }


    public BigDecimal getAdultsPayment() {
        return adultsPayment;
    }

    public void setAdultsPayment(BigDecimal adultsPayment) {
        this.adultsPayment = adultsPayment;
    }

    public BigDecimal getYoungPayment() {
        return youngPayment;
    }

    public void setYoungPayment(BigDecimal youngPayment) {
        this.youngPayment = youngPayment;
    }
}
