package fi.riista.feature.organization.rhy;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
public class RiistanhoitoyhdistysRepositoryImpl implements RiistanhoitoyhdistysRepositoryCustom{

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Map<Long, Integer> calculateMemberCountsForStatistics(final int statisticsYear) {
        final QPerson MEMBER = QPerson.person;

        return queryFactory
                .from(MEMBER)
                .join(MEMBER.rhyMembershipForStatistics)
                .where(MEMBER.rhyMembershipForStatistics.isNotNull())
                .where(MEMBER.huntingPaymentOneYear.eq(statisticsYear).or(MEMBER.huntingPaymentTwoYear.eq(statisticsYear)))
                .groupBy(MEMBER.rhyMembershipForStatistics.id)
                .transform(groupBy(MEMBER.rhyMembershipForStatistics.id).as(MEMBER.rhyMembershipForStatistics.id.count().intValue()));
    }
}
