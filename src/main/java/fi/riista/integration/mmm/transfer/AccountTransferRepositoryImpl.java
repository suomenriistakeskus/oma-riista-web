package fi.riista.integration.mmm.transfer;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.permit.invoice.QInvoice;
import fi.riista.feature.permit.invoice.harvest.QPermitHarvestInvoice;
import fi.riista.feature.permit.invoice.payment.QInvoicePaymentLine;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountTransferRepositoryImpl implements AccountTransferRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<AccountTransfer> findAccountTransfersNotAssociatedWithInvoice() {
        return findAccountTransfersNotAssociatedWithInvoice(Optional.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTransfer> findAccountTransfersNotAssociatedWithInvoice(final CreditorReference creditorReference) {
        return findAccountTransfersNotAssociatedWithInvoice(Optional.of(creditorReference));
    }

    private List<AccountTransfer> findAccountTransfersNotAssociatedWithInvoice(final Optional<CreditorReference> referenceOpt) {
        final QAccountTransfer TRANSFER = QAccountTransfer.accountTransfer;
        final QInvoicePaymentLine PAYMENT_LINE = QInvoicePaymentLine.invoicePaymentLine;

        final BooleanExpression referencePredicate = referenceOpt
                .map(TRANSFER.creditorReference::eq)
                .orElseGet(() -> TRANSFER.creditorReference.in(subqueryForCreditorReferencesOfPermitHarvestInvoices()));

        return jpaQueryFactory
                .select(TRANSFER)
                .from(PAYMENT_LINE)
                .rightJoin(PAYMENT_LINE.accountTransfer, TRANSFER)
                .where(referencePredicate, PAYMENT_LINE.id.isNull())
                .fetch();
    }

    private static JPQLQuery<CreditorReference> subqueryForCreditorReferencesOfPermitHarvestInvoices() {
        final QPermitHarvestInvoice PERMIT_HARVEST_INVOICE = QPermitHarvestInvoice.permitHarvestInvoice;
        final QInvoice INVOICE = QInvoice.invoice;

        return JPAExpressions
                .select(INVOICE.creditorReference)
                .from(PERMIT_HARVEST_INVOICE)
                .innerJoin(PERMIT_HARVEST_INVOICE.invoice, INVOICE);
    }
}
