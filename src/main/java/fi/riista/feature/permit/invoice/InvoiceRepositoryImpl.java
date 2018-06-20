package fi.riista.feature.permit.invoice;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;
import fi.riista.feature.permit.invoice.search.InvoiceSearchQueryBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

    private static final int DEFAULT_MAX_SEARCH_RESULTS = 1000;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoice(final PermitDecision permitDecision, final long invoiceId) {
        final QPermitDecisionInvoice DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QInvoice INVOICE = QInvoice.invoice;

        return jpqlQueryFactory
                .select(INVOICE)
                .from(DECISION_INVOICE)
                .join(DECISION_INVOICE.invoice, INVOICE)
                .where(DECISION_INVOICE.decision.eq(permitDecision),
                        INVOICE.id.eq(invoiceId))
                .fetchOne();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> findElectronicInvoices(final PermitDecision permitDecision, final InvoiceState invoiceState) {
        final QPermitDecisionInvoice DECISION_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QInvoice INVOICE = QInvoice.invoice;

        return jpqlQueryFactory
                .select(INVOICE)
                .from(DECISION_INVOICE)
                .join(DECISION_INVOICE.invoice, INVOICE)
                .where(DECISION_INVOICE.decision.eq(permitDecision),
                        INVOICE.electronicInvoicingEnabled.isTrue(),
                        INVOICE.state.eq(invoiceState))
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
            queryBuilder = newBuilder().withInvoiceNumber(dto.getInvoiceNumber());
        } else if (dto.getApplicationNumber() != null) {
            queryBuilder = newBuilder().withApplicationNumber(dto.getApplicationNumber());
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
                .withBeginDate(dto.getBeginDate())
                .withEndDate(dto.getEndDate());
    }

    private InvoiceSearchQueryBuilder newBuilder() {
        return new InvoiceSearchQueryBuilder(jpqlQueryFactory);
    }
}
