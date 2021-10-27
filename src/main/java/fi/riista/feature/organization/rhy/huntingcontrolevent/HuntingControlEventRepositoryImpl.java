package fi.riista.feature.organization.rhy.huntingcontrolevent;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class HuntingControlEventRepositoryImpl implements HuntingControlEventRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Transactional(readOnly = true)
    public Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> findByYear(final int year) {
        final QOrganisation RKA = QOrganisation.organisation;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        return jpaQueryFactory.from(EVENT)
                .innerJoin(EVENT.rhy, RHY)
                .innerJoin(RHY.parentOrganisation, RKA)
                .where(EVENT.date.year().eq(year))
                .orderBy(EVENT.date.asc())
                .transform(GroupBy.groupBy(RKA).as(GroupBy.map(RHY, GroupBy.list(EVENT))));
    }
}
