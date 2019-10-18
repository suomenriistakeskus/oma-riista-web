package fi.riista.feature.permit.invoice.decision;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.invoice.InvoiceState;
import fi.riista.feature.permit.invoice.QInvoice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.util.DateUtil.today;

@Repository
public class PermitDecisionInvoiceRepositoryImpl implements PermitDecisionInvoiceRepositoryCustom {

    @Resource
    private JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    @Override
    public List<PermitDecisionInvoice> getPermitDecisionInvoicesForNextFivaldiBatch() {
        final QPermitDecisionInvoice PD_INVOICE = QPermitDecisionInvoice.permitDecisionInvoice;
        final QPermitDecision DECISION = QPermitDecision.permitDecision;
        final QInvoice INVOICE = QInvoice.invoice;

        return queryFactory
                .selectFrom(PD_INVOICE)
                .join(PD_INVOICE.decision, DECISION)
                .join(PD_INVOICE.invoice, INVOICE).fetchJoin()
                .where(PD_INVOICE.batch.isNull())
                .where(INVOICE.invoiceDate.loe(today()))
                .where(INVOICE.state.ne(InvoiceState.VOID))
                .fetch();
    }
}
