package fi.riista.feature.harvestpermit.payment;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Objects;

public class HuntingClubPermitPaymentDTO {

    public static HuntingClubPermitPaymentDTO create(final long huntingClubId,
                                                     final @Nonnull HarvestCountDTO count,
                                                     final @Nonnull MooselikePrice prices) {
        Objects.requireNonNull(count, "huntingClubPermitCount is null");
        Objects.requireNonNull(prices, "mooseLikePrice is null");

        final BigDecimal adultsPayment = calculatePayment(count.getNumberOfAdults(), count.getNumberOfNonEdibleAdults(), prices.getAdultPrice());
        final BigDecimal youngPayment = calculatePayment(count.getNumberOfYoung(), count.getNumberOfNonEdibleYoungs(), prices.getYoungPrice());

        return new HuntingClubPermitPaymentDTO(adultsPayment, youngPayment, huntingClubId);
    }

    private static BigDecimal calculatePayment(final int total, final int notEdible, final BigDecimal price) {
        final BigDecimal edible = new BigDecimal(total - notEdible);
        return edible.multiply(price);
    }

    private final long huntingClubId;
    private final BigDecimal adultsPayment;
    private final BigDecimal youngPayment;

    private HuntingClubPermitPaymentDTO(final BigDecimal adultsPayment, final BigDecimal youngPayment, final long huntingClubId) {
        this.adultsPayment = Objects.requireNonNull(adultsPayment);
        this.youngPayment = Objects.requireNonNull(youngPayment);
        this.huntingClubId = huntingClubId;
    }

    @JsonGetter
    public BigDecimal getTotalPayment() {
        return adultsPayment.add(youngPayment);
    }

    // accessors

    public long getHuntingClubId() {
        return huntingClubId;
    }

    public BigDecimal getAdultsPayment() {
        return adultsPayment;
    }

    public BigDecimal getYoungPayment() {
        return youngPayment;
    }
}
