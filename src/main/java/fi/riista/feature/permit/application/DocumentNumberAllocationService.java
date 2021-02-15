package fi.riista.feature.permit.application;

import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class DocumentNumberAllocationService {

    // Originally numbers were allocated for applications only, hence the database sequence name.
    private static final String DOCUMENT_NUMBER_SEQ = "seq_harvest_permit_application_number";

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int allocateNextNumber() {
        return incrementSequence();
    }

    private int incrementSequence() {
        return Objects.requireNonNull(sqlQueryFactory
                .select(SQLExpressions.nextval(Integer.class, DOCUMENT_NUMBER_SEQ))
                .fetchOne());
    }


}
