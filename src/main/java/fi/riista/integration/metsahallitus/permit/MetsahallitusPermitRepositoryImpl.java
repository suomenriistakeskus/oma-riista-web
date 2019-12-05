package fi.riista.integration.metsahallitus.permit;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.Person;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

public class MetsahallitusPermitRepositoryImpl implements MetsahallitusPermitRepositoryCustom {

    private static final QMetsahallitusPermit MH_PERMIT = QMetsahallitusPermit.metsahallitusPermit;
    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<MetsahallitusPermit> findByPerson(final Person person) {
        final String ssn = person.getSsn();
        final String hunterNumber = person.getHunterNumber();

        if (ssn == null && hunterNumber == null) {
            return Collections.emptyList();
        }

        return jpqlQueryFactory
                .selectFrom(MH_PERMIT)
                .where(hunterNumberPredicate(hunterNumber).or(ssnPredicate(ssn)))
                .fetch();
    }

    private BooleanExpression ssnPredicate(final String ssn) {
        return ssn == null ? alwaysFalse() : MH_PERMIT.ssn.eq(ssn);
    }

    private BooleanExpression hunterNumberPredicate(final String hunterNumber) {
        return hunterNumber == null ? alwaysFalse() : MH_PERMIT.hunterNumber.eq(hunterNumber);
    }

    private BooleanExpression alwaysFalse() {
        // The isTrue is needed for successful querying
        return Expressions.asBoolean(false).isTrue();
    }
}
