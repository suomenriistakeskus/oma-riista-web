package fi.riista.integration.metsahallitus.permit;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.Person;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

public class MetsahallitusPermitRepositoryImpl implements MetsahallitusPermitRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<MetsahallitusPermit> findByPerson(final Person person) {
        final QMetsahallitusPermit MH_PERMIT = QMetsahallitusPermit.metsahallitusPermit;

        final BooleanExpression ssnMatches = MH_PERMIT.ssn.eq(person.getSsn());

        final JPQLQuery<MetsahallitusPermit> query = jpqlQueryFactory.selectFrom(MH_PERMIT);

        if (person.getHunterNumber() != null) {
            final BooleanExpression hunterNumberMatches = MH_PERMIT.hunterNumber.eq(person.getHunterNumber());

            return query.where(hunterNumberMatches.or(ssnMatches)).fetch();
        }

        return query.where(ssnMatches).fetch();
    }
}
