package fi.riista.feature.permit.invoice.search;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.QInvoice;
import fi.riista.feature.permit.invoice.QPermitDecisionInvoice;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static java.util.Objects.requireNonNull;

public class InvoiceSearchQueryBuilder {

    private final JPQLQueryFactory jpqlQueryFactory;

    private Integer applicationNumber;
    private Integer invoiceNumber;
    private String creditorReference;

    private InvoiceType type;
    private InvoiceDeliveryType deliveryType;

    private LocalDate beginDate;
    private LocalDate endDate;

    private int maxQueryResults = -1;

    public InvoiceSearchQueryBuilder(@Nonnull final JPQLQueryFactory jpqlQueryFactory) {
        this.jpqlQueryFactory = requireNonNull(jpqlQueryFactory);
    }

    public InvoiceSearchQueryBuilder withApplicationNumber(final int applicationNumber) {
        this.applicationNumber = applicationNumber;
        return this;
    }

    public InvoiceSearchQueryBuilder withInvoiceNumber(final int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    public InvoiceSearchQueryBuilder withCreditorReference(final String creditorReference) {
        this.creditorReference = creditorReference;
        return this;
    }

    public InvoiceSearchQueryBuilder withInvoiceType(final InvoiceType type) {
        this.type = type;
        return this;
    }

    public InvoiceSearchQueryBuilder withDeliveryType(final InvoiceDeliveryType deliveryType) {
        this.deliveryType = deliveryType;
        return this;
    }

    public InvoiceSearchQueryBuilder withBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
        return this;
    }

    public InvoiceSearchQueryBuilder withEndDate(final LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public InvoiceSearchQueryBuilder withMaxQueryResults(final int maxQueryResults) {
        this.maxQueryResults = maxQueryResults;
        return this;
    }

    public List<Invoice> list() {
        return build().fetch();
    }

    private JPQLQuery<Invoice> build() {
        final QInvoice INVOICE = QInvoice.invoice;
        final QAddress ADDRESS = QAddress.address;
        final QPermitDecisionInvoice PROCESSING_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecision PERMIT_DECISION = QPermitDecision.permitDecision;
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

        final JPQLQuery<Invoice> query = jpqlQueryFactory
                .selectFrom(INVOICE)
                .join(INVOICE.recipientAddress, ADDRESS).fetchJoin()
                .orderBy(INVOICE.invoiceDate.desc(), INVOICE.invoiceNumber.desc());

        if (applicationNumber != null) {
            final JPQLQuery<Integer> applicationNumberMatchViaProcessingInvoicePathQuery = JPAExpressions
                    .select(constant(1))
                    .from(PROCESSING_INVOICE)
                    .join(PROCESSING_INVOICE.decision, PERMIT_DECISION)
                    .join(PERMIT_DECISION.application, APPLICATION)
                    .where(PROCESSING_INVOICE.invoice.eq(INVOICE),
                            APPLICATION.applicationNumber.eq(applicationNumber));

            // TODO Need to later include access path for species amount based invoices.
            final BooleanExpression applicationNumberPredicate =
                    applicationNumberMatchViaProcessingInvoicePathQuery.exists();

            query.where(applicationNumberPredicate);
        }

        if (invoiceNumber != null) {
            query.where(INVOICE.invoiceNumber.eq(invoiceNumber));
        }

        if (creditorReference != null) {
            query.where(INVOICE.creditorReference.creditorReference.like("%" + creditorReference + "%"));
        }

        if (type != null) {
            query.where(INVOICE.type.eq(type));
        }

        if (deliveryType != null) {
            query.where(INVOICE.electronicInvoicingEnabled.eq(deliveryType == InvoiceDeliveryType.EMAIL));
        }

        if (beginDate != null) {
            query.where(INVOICE.dueDate.goe(beginDate));
        }

        if (endDate != null) {
            query.where(INVOICE.invoiceDate.loe(endDate));
        }

        if (maxQueryResults > 0) {
            query.limit(maxQueryResults);
        }

        return query;
    }
}
