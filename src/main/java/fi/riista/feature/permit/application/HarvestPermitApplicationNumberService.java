package fi.riista.feature.permit.application;

import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class HarvestPermitApplicationNumberService {

    private static final String APPLICATION_NUMBER_SEQ = "seq_harvest_permit_application_number";

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public int getNextApplicationNumber() {
        return incrementSequence();
    }

    private int incrementSequence() {
        return Objects.requireNonNull(sqlQueryFactory
                .select(SQLExpressions.nextval(Integer.class, APPLICATION_NUMBER_SEQ))
                .fetchOne());
    }
}
