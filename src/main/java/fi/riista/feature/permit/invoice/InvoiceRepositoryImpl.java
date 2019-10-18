package fi.riista.feature.permit.invoice;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.permit.invoice.decision.QPermitDecisionInvoice;
import fi.riista.feature.permit.invoice.harvest.QPermitHarvestInvoice;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;
import fi.riista.feature.permit.invoice.search.InvoiceSearchQueryBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

    private static final int DEFAULT_MAX_SEARCH_RESULTS = 1000;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findElectronicInvoices(final HarvestPermit harvestPermit,
                                                final EnumSet<InvoiceType> invoiceTypes,
                                                final InvoiceState invoiceState) {

        checkArgument(!invoiceTypes.isEmpty(), "invoiceTypes must not be empty");

        final QInvoice INVOICE = QInvoice.invoice;
        final QPermitDecisionInvoice DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        final Set<Long> allInvoiceIds = new HashSet<>();

        if (invoiceTypes.contains(InvoiceType.PERMIT_PROCESSING) && harvestPermit.getPermitDecision() != null) {
            allInvoiceIds.addAll(jpqlQueryFactory
                    .select(INVOICE.id)
                    .from(DECISION_INVOICE)
                    .join(DECISION_INVOICE.invoice, INVOICE)
                    .where(DECISION_INVOICE.decision.eq(harvestPermit.getPermitDecision()))
                    .fetch());
        }

        if (invoiceTypes.contains(InvoiceType.PERMIT_HARVEST)) {
            allInvoiceIds.addAll(jpqlQueryFactory
                    .select(INVOICE.id)
                    .from(HARVEST_INVOICE)
                    .join(HARVEST_INVOICE.invoice, INVOICE)
                    .join(HARVEST_INVOICE.speciesAmount, SPA)
                    .where(SPA.harvestPermit.eq(harvestPermit))
                    .fetch());
        }

        return jpqlQueryFactory
                .selectFrom(INVOICE)
                .where(INVOICE.id.in(allInvoiceIds),
                        INVOICE.electronicInvoicingEnabled.isTrue(),
                        INVOICE.state.eq(invoiceState))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findHarvestInvoicesHavingInitiatedOrConfirmedPayments(final HarvestPermit harvestPermit) {
        final QInvoice INVOICE = QInvoice.invoice;
        final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QHarvestPermitSpeciesAmount SPA = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;

        // At the instant a payment is initiated via Paytrail, invoice is transitioned to PAID
        // state but it is not yet confirmed from a bank account statement. On the other hand,
        // when receivedAmount is not null, then invoice has one or more confirmed payments.
        final BooleanExpression paymentInitiatedOrConfirmed =
                INVOICE.state.eq(InvoiceState.PAID).or(INVOICE.receivedAmount.isNotNull());

        return jpqlQueryFactory
                .select(INVOICE)
                .from(HARVEST_INVOICE)
                .join(HARVEST_INVOICE.invoice, INVOICE)
                .join(HARVEST_INVOICE.speciesAmount, SPA)
                .where(SPA.harvestPermit.eq(harvestPermit), paymentInitiatedOrConfirmed)
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> search(@Nonnull final InvoiceSearchFilterDTO dto) {
        return search(dto, DEFAULT_MAX_SEARCH_RESULTS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> search(@Nonnull final InvoiceSearchFilterDTO dto, final int searchResultLimit) {
        requireNonNull(dto);

        final InvoiceSearchQueryBuilder queryBuilder;

        if (dto.getInvoiceNumber() != null) {
            queryBuilder = newBuilder()
                    .withInvoiceType(dto.getType())
                    .withInvoiceNumber(dto.getInvoiceNumber());

        } else if (dto.getApplicationNumber() != null) {
            queryBuilder = newBuilder()
                    .withInvoiceType(dto.getType())
                    .withApplicationNumber(dto.getApplicationNumber());

        } else {
            queryBuilder = constructQueryBuilder(dto).withMaxQueryResults(searchResultLimit);
        }

        return queryBuilder.list();
    }

    private InvoiceSearchQueryBuilder constructQueryBuilder(final InvoiceSearchFilterDTO dto) {
        final InvoiceSearchQueryBuilder builder = newBuilder();

        Optional.ofNullable(dto.getCreditorReference())
                .map(refNum -> refNum.replaceAll("[^0-9]", ""))
                .filter(StringUtils::isNotBlank)
                .ifPresent(builder::withCreditorReference);

        return builder
                .withInvoiceType(dto.getType())
                .withDeliveryType(dto.getDeliveryType())
                .withPaymentState(dto.getPaymentState())
                .withHuntingYear(dto.getHuntingYear())
                .withGameSpeciesCode(dto.getGameSpeciesCode())
                .withRkaOfficialCode(dto.getRkaOfficialCode())
                .withRhyOfficialCode(dto.getRhyOfficialCode())
                .withBeginDate(dto.getBeginDate())
                .withEndDate(dto.getEndDate());
    }

    private InvoiceSearchQueryBuilder newBuilder() {
        return new InvoiceSearchQueryBuilder(jpqlQueryFactory);
    }
}
