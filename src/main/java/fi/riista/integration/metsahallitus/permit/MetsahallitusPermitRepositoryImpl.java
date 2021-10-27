package fi.riista.integration.metsahallitus.permit;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Propagation;
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

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public long deleteOldPermits() {
        final LocalDate deleteUntil = new LocalDate(DateUtil.huntingYear() - 1, 8, 1);
        return jpqlQueryFactory.delete(MH_PERMIT)
                .where(endDatePredicate(deleteUntil).or(modificationTimePredicate(deleteUntil)))
                .execute();
    }

    private static BooleanExpression ssnPredicate(final String ssn) {
        return ssn == null ? alwaysFalse() : MH_PERMIT.ssn.eq(ssn);
    }

    private static BooleanExpression hunterNumberPredicate(final String hunterNumber) {
        return hunterNumber == null ? alwaysFalse() : MH_PERMIT.hunterNumber.eq(hunterNumber);
    }

    private static BooleanExpression alwaysFalse() {
        // The isTrue is needed for successful querying
        return Expressions.asBoolean(false).isTrue();
    }

    private static BooleanExpression endDatePredicate(final LocalDate deleteUntil) {
        return MH_PERMIT.endDate.lt(deleteUntil);
    }

    private static BooleanExpression modificationTimePredicate(final LocalDate deleteUntil) {
        return MH_PERMIT.endDate.isNull()
                .and(MH_PERMIT.lifecycleFields.modificationTime.lt(DateUtil.toDateTimeNullSafe(deleteUntil)));
    }
}
