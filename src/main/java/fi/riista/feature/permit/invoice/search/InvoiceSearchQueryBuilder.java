package fi.riista.feature.permit.invoice.search;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.address.QAddress;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.InvoiceType;
import fi.riista.feature.permit.invoice.QInvoice;
import fi.riista.feature.permit.invoice.decision.QPermitDecisionInvoice;
import fi.riista.feature.permit.invoice.harvest.QPermitHarvestInvoice;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.selectOne;
import static fi.riista.feature.permit.invoice.InvoiceType.PERMIT_HARVEST;
import static fi.riista.feature.permit.invoice.InvoiceType.PERMIT_PROCESSING;
import static java.util.Objects.requireNonNull;

public class InvoiceSearchQueryBuilder {

    private static final QInvoice INVOICE = QInvoice.invoice;
    private static final QAddress ADDRESS = QAddress.address;
    private static final QPermitDecisionInvoice PROCESSING_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
    private static final QPermitHarvestInvoice HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
    private static final QHarvestPermitSpeciesAmount SPECIES_AMOUNT = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
    private static final QPermitDecision PERMIT_DECISION = QPermitDecision.permitDecision;
    private static final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

    private final JPQLQueryFactory jpqlQueryFactory;

    private Integer applicationNumber;
    private Integer invoiceNumber;
    private String creditorReference;

    private InvoiceType type;
    private InvoiceDeliveryType deliveryType;
    private InvoicePaymentState paymentState;

    private Integer huntingYear;
    private Integer gameSpeciesCode;

    private String rkaOfficialCode;
    private String rhyOfficialCode;

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

    public InvoiceSearchQueryBuilder withPaymentState(final InvoicePaymentState paymentState) {
        this.paymentState = paymentState;
        return this;
    }

    public InvoiceSearchQueryBuilder withHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
        return this;
    }

    public InvoiceSearchQueryBuilder withGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
        return this;
    }

    public InvoiceSearchQueryBuilder withRkaOfficialCode(final String rkaOfficialCode) {
        this.rkaOfficialCode = rkaOfficialCode;
        return this;
    }

    public InvoiceSearchQueryBuilder withRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
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
        final JPQLQuery<Invoice> query = jpqlQueryFactory
                .selectFrom(INVOICE)
                .join(INVOICE.recipientAddress, ADDRESS).fetchJoin()
                .orderBy(INVOICE.invoiceDate.desc(), INVOICE.invoiceNumber.desc());

        if (type != null) {
            query.where(INVOICE.type.eq(type));
        }

        if (invoiceNumber != null) {
            return query.where(INVOICE.invoiceNumber.eq(invoiceNumber));
        }

        if (isProcessingInvoiceSpecificPredicateNeeded()) {
            final BooleanExpression processingInvoicePredicate = constructProcessingInvoicePredicate();

            if (isHarvestInvoiceSpecificPredicateNeeded()) {
                query.where(processingInvoicePredicate.or(constructHarvestInvoicePredicate()));
            } else {
                query.where(processingInvoicePredicate);
            }
        } else if (isHarvestInvoiceSpecificPredicateNeeded()) {
            query.where(constructHarvestInvoicePredicate());
        }

        // Short-circuiting done because subsequent filter options are currently disabled in UI.
        if (applicationNumber != null) {
            // applicationNumber predicate is already included.
            return query;
        }

        if (creditorReference != null) {
            query.where(INVOICE.creditorReference.creditorReference.like("%" + creditorReference + "%"));
        }

        if (deliveryType != null) {
            query.where(INVOICE.electronicInvoicingEnabled.eq(deliveryType == InvoiceDeliveryType.ELECTRONIC));
        }

        if (paymentState != null) {
            query.where(createPaymentStatePredicate(paymentState));
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

    private static BooleanExpression createPaymentStatePredicate(final InvoicePaymentState paymentState) {
        final BooleanExpression processingInvoice = INVOICE.type.eq(PERMIT_PROCESSING);
        final BooleanExpression harvestInvoice = INVOICE.type.eq(PERMIT_HARVEST);

        final BooleanExpression paid = INVOICE.state.eq(InvoiceState.PAID);
        final BooleanExpression paymentReceived = INVOICE.receivedAmount.isNotNull();
        final BooleanExpression paymentSumMatches =
                INVOICE.correctedAmount.coalesce(INVOICE.amount).getValue().eq(INVOICE.receivedAmount);

        switch (paymentState) {
            case PAID:
                return processingInvoice.and(paid).or(harvestInvoice.and(paymentReceived).and(paymentSumMatches));
            case PAYMENT_SUM_DIFFERS:
                return harvestInvoice.and(paymentReceived).and(paymentSumMatches.not());
            case OTHER:
                return processingInvoice.and(paid.not()).or(harvestInvoice.and(paymentReceived.not()));
            default:
                throw new UnsupportedOperationException("Unknown invoice payment state: " + paymentState.name());
        }
    }

    private boolean isProcessingInvoiceSpecificPredicateNeeded() {
        return type != PERMIT_HARVEST
                && F.anyNonNull(applicationNumber, huntingYear, rkaOfficialCode, rhyOfficialCode);
    }

    private boolean isHarvestInvoiceSpecificPredicateNeeded() {
        return type != PERMIT_PROCESSING
                && F.anyNonNull(applicationNumber, huntingYear, gameSpeciesCode, rkaOfficialCode, rhyOfficialCode);
    }

    private BooleanExpression constructProcessingInvoicePredicate() {
        final JPQLQuery<Integer> query = selectOne()
                .from(PROCESSING_INVOICE)
                .join(PROCESSING_INVOICE.decision, PERMIT_DECISION)
                .where(PROCESSING_INVOICE.invoice.eq(INVOICE));

        if (applicationNumber != null || huntingYear != null) {
            query.join(PERMIT_DECISION.application, APPLICATION);

            if (applicationNumber != null) {
                // Short-circuiting predicate
                return query.where(APPLICATION.applicationNumber.eq(applicationNumber)).exists();
            }
            if (huntingYear != null) {
                query.where(APPLICATION.applicationYear.eq(huntingYear));
            }
        }

        if (rkaOfficialCode != null || rhyOfficialCode != null) {
            query.join(PERMIT_DECISION.rhy, RHY);

            if (rhyOfficialCode != null) {
                query.where(RHY.officialCode.eq(rhyOfficialCode));
            } else {
                query.join(RHY.parentOrganisation, RKA._super).where(RKA.officialCode.eq(rkaOfficialCode));
            }
        }

        return query.exists();
    }

    private BooleanExpression constructHarvestInvoicePredicate() {
        final JPQLQuery<Integer> query = selectOne()
                .from(HARVEST_INVOICE)
                .join(HARVEST_INVOICE.speciesAmount, SPECIES_AMOUNT)
                .join(SPECIES_AMOUNT.harvestPermit, PERMIT)
                .where(HARVEST_INVOICE.invoice.eq(INVOICE));

        if (applicationNumber != null || huntingYear != null) {
            query.join(PERMIT.permitDecision, PERMIT_DECISION)
                    .join(PERMIT_DECISION.application, APPLICATION);

            if (applicationNumber != null) {
                // Short-circuiting predicate
                return query.where(APPLICATION.applicationNumber.eq(applicationNumber)).exists();
            }
            if (huntingYear != null) {
                query.where(APPLICATION.applicationYear.eq(huntingYear));
            }
        }

        if (rkaOfficialCode != null || rhyOfficialCode != null) {
            query.join(PERMIT.rhy, RHY);

            if (rhyOfficialCode != null) {
                query.where(RHY.officialCode.eq(rhyOfficialCode));
            } else {
                query.join(RHY.parentOrganisation, RKA._super).where(RKA.officialCode.eq(rkaOfficialCode));
            }
        }

        if (gameSpeciesCode != null) {
            query.join(SPECIES_AMOUNT.gameSpecies, SPECIES).where(SPECIES.officialCode.eq(gameSpeciesCode));
        }

        return query.exists();
    }
}
