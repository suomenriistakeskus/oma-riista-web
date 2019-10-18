package fi.riista.feature.harvestpermit.payment;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.NumberUtils.sum;

@Service
public class MooselikePermitPriceService {

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClubPermitPriceBreakdownDTO getPartnerPriceBreakdown(final GameSpecies gameSpecies,
                                                                       final Map<Long, HarvestCountDTO> harvestCounts) {
        final MooselikePrice prices = MooselikePrice.get(gameSpecies);

        final Map<Long, HuntingClubPermitPaymentDTO> payments = harvestCounts.entrySet().stream()
                .map(h -> HuntingClubPermitPaymentDTO.create(h.getKey(), h.getValue(), prices))
                .collect(indexingBy(HuntingClubPermitPaymentDTO::getHuntingClubId));

        final HuntingClubPermitTotalPaymentDTO totalPayment = calculateTotalPayment(harvestCounts.values(), payments.values(), prices);

        return new HuntingClubPermitPriceBreakdownDTO(payments, totalPayment);
    }

    private static HuntingClubPermitTotalPaymentDTO calculateTotalPayment(
            final Collection<HarvestCountDTO> harvests,
            final Collection<HuntingClubPermitPaymentDTO> payments,
            final MooselikePrice prices) {

        final HuntingClubPermitTotalPaymentDTO total = new HuntingClubPermitTotalPaymentDTO();

        total.setAdultsCount(sum(harvests, HarvestCountDTO::getNumberOfAdults));
        total.setAdultsNotEdibleCount(sum(harvests, HarvestCountDTO::getNumberOfNonEdibleAdults));

        total.setYoungCount(sum(harvests, HarvestCountDTO::getNumberOfYoung));
        total.setYoungNotEdibleCount(sum(harvests, HarvestCountDTO::getNumberOfNonEdibleYoungs));

        total.setYoungPayment(sum(payments, HuntingClubPermitPaymentDTO::getYoungPayment));
        total.setAdultsPayment(sum(payments, HuntingClubPermitPaymentDTO::getAdultsPayment));

        total.setAdultPrice(prices.getAdultPrice());
        total.setYoungPrice(prices.getYoungPrice());

        return total;
    }

}
