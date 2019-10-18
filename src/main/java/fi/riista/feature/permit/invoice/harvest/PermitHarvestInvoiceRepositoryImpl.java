package fi.riista.feature.permit.invoice.harvest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.permit.invoice.QInvoice;
import io.vavr.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fi.riista.config.Constants.ZERO_MONETARY_AMOUNT;
import static fi.riista.util.Collect.tuplesToMap;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyMap;

@Repository
public class PermitHarvestInvoiceRepositoryImpl implements PermitHarvestInvoiceRepositoryCustom {

    @Resource
    private JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    @Override
    public Map<Long, InvoicePaymentAmountsDTO> getMooselikeHarvestInvoicePaymentAmounts(final Set<Long> permitIds,
                                                                                        final int speciesCode) {
        if (permitIds.isEmpty()) {
            return emptyMap();
        }

        final QInvoice INVOICE = QInvoice.invoice;
        final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return queryFactory
                .select(PERMIT.id, INVOICE.amount, INVOICE.correctedAmount, INVOICE.receivedAmount)
                .from(HARVEST_INVOICE)
                .join(HARVEST_INVOICE.invoice, INVOICE)
                .join(HARVEST_INVOICE.speciesAmount, SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.gameSpecies, SPECIES)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .where(PERMIT.id.in(permitIds),
                        PERMIT.isMooselikePermit(),
                        SPECIES_AMOUNT.mooselikeHuntingFinished.isTrue(),
                        SPECIES.officialCode.eq(speciesCode))
                .fetch()
                .stream()
                .map(t -> {
                    final BigDecimal possiblyCorrectedInvoiceAmount = Optional
                            .ofNullable(t.get(INVOICE.correctedAmount))
                            .orElseGet(() -> t.get(INVOICE.amount));

                    final BigDecimal receivedAmountOrZero = Optional
                            .ofNullable(t.get(INVOICE.receivedAmount))
                            .orElse(ZERO_MONETARY_AMOUNT);

                    final BigDecimal remainingAmount = possiblyCorrectedInvoiceAmount.subtract(receivedAmountOrZero);

                    final BigDecimal surplusAmount =
                            remainingAmount.compareTo(ZERO) < 0 ? remainingAmount.negate() : ZERO_MONETARY_AMOUNT;

                    final BigDecimal deficientAmount =
                            remainingAmount.compareTo(ZERO) > 0 ? remainingAmount : ZERO_MONETARY_AMOUNT;

                    return Tuple.of(
                            t.get(PERMIT.id),
                            new InvoicePaymentAmountsDTO(receivedAmountOrZero, surplusAmount, deficientAmount));
                })
                .collect(tuplesToMap());
    }
}
