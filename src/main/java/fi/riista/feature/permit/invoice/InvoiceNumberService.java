package fi.riista.feature.permit.invoice;

import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class InvoiceNumberService {

    private static final String INVOICE_NUMBER_SEQ = "seq_invoice_number";

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int getNextInvoiceNumber() {
        return incrementSequence();
    }

    private int incrementSequence() {
        return requireNonNull(sqlQueryFactory
                .select(SQLExpressions.nextval(Integer.class, INVOICE_NUMBER_SEQ))
                .fetchOne());
    }
}
