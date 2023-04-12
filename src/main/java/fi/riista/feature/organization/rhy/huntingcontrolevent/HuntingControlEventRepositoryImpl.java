package fi.riista.feature.organization.rhy.huntingcontrolevent;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventReportQueryDTO.HuntingControlSubsidyFilter.ACCEPTED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventReportQueryDTO.HuntingControlSubsidyFilter.ACCEPTED_SUBSIDIZED;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.F;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class HuntingControlEventRepositoryImpl implements HuntingControlEventRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    private NamedParameterJdbcTemplate jdbcTemplate;
    private HuntingControlEventQuery eventQuery;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.eventQuery = new HuntingControlEventQuery(jdbcTemplate);
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<Integer> listEventYears(final Riistanhoitoyhdistys rhy) {
        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        return jpaQueryFactory
                .select(EVENT.date.year()).distinct()
                .from(EVENT)
                .where(EVENT.rhy.eq(rhy))
                .orderBy(EVENT.date.year().asc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> listEventYears(final Riistanhoitoyhdistys rhy, final Person person) {
        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        return jpaQueryFactory
                .select(EVENT.date.year()).distinct()
                .from(EVENT)
                .where(EVENT.rhy.eq(rhy),
                        EVENT.inspectors.contains(person))
                .orderBy(EVENT.date.year().asc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> mapInspectorPersonIdsByEventId(final Collection<HuntingControlEvent> events) {
        return eventQuery.mapInspectorPersonIdsByEventId(events);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<HuntingControlCooperationType>> mapCooperationTypesByEventId(final Collection<HuntingControlEvent> events) {
        return eventQuery.mapCooperationTypesByEventId(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HuntingControlEvent> findByRhyAndInspectorAndModifiedAfterOrder(final Organisation org,
                                                                                final Person inspector,
                                                                                final DateTime modificationTime) {

        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        return jpaQueryFactory.from(EVENT)
                .select(EVENT)
                .where(EVENT.rhy.id.eq(org.getId()),
                        EVENT.inspectors.contains(inspector),
                        EVENT.lifecycleFields.modificationTime.gt(modificationTime))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HuntingControlEvent> findReportEvents(Riistanhoitoyhdistys rhy, HuntingControlEventReportQueryDTO filters) {
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        final HuntingControlEventType eventType = filters.getEventType();
        final BooleanExpression typeExpression = eventType != null ? EVENT.eventType.eq(eventType) : null;

        final HuntingControlCooperationType cooperationType = filters.getCooperationType();
        final BooleanExpression cooperationExpression = cooperationType != null ? EVENT.cooperationTypes.contains(cooperationType) : null;

        final HuntingControlEventStatus status = filters.getStatus();
        BooleanExpression statusExpression = status != null ? EVENT.status.eq(status) : null;
        if (status == HuntingControlEventStatus.ACCEPTED) {
            final HuntingControlEventReportQueryDTO.HuntingControlSubsidyFilter subsidized = filters.getSubsidized();
            if (subsidized == ACCEPTED_SUBSIDIZED) {
                statusExpression = EVENT.status.eq(HuntingControlEventStatus.ACCEPTED_SUBSIDIZED);
            } else if (subsidized == ACCEPTED) {
                statusExpression = EVENT.status.eq(HuntingControlEventStatus.ACCEPTED);
            } else {
                statusExpression = EVENT.status.eq(HuntingControlEventStatus.ACCEPTED_SUBSIDIZED)
                        .or(EVENT.status.eq(HuntingControlEventStatus.ACCEPTED));
            }
        }

        return jpaQueryFactory.selectFrom(EVENT)
                .join(EVENT.rhy, RHY)
                .where(RHY.id.eq(rhy.getId())
                        .and(EVENT.date.year().eq(filters.getYear()))
                        .and(typeExpression)
                        .and(cooperationExpression)
                        .and(statusExpression))
                .orderBy(EVENT.date.asc(), EVENT.beginTime.asc(), EVENT.id.asc())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HuntingControlEvent> findReportEvents(final HuntingControlEventSearchParametersDTO filters) {
        if (F.isNullOrEmpty(filters.getTypes())
                || F.isNullOrEmpty(filters.getStatuses())
                || F.isNullOrEmpty(filters.getCooperationTypes())) {
            return Collections.emptyList();
        }
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final QHuntingControlEvent EVENT = QHuntingControlEvent.huntingControlEvent;

        final BooleanExpression typeExpression = EVENT.eventType.in(filters.getTypes());
        final BooleanExpression statusExpression = EVENT.status.in(filters.getStatuses());

        final BooleanBuilder cooperationExpression = new BooleanBuilder();
        filters.getCooperationTypes().forEach(c -> cooperationExpression.or(EVENT.cooperationTypes.contains(c)));

        final BooleanExpression beginDateExpression = filters.getBeginDate() != null ? EVENT.date.goe(filters.getBeginDate()) : null;
        final BooleanExpression endDateExpression = filters.getEndDate() != null ? EVENT.date.loe(filters.getEndDate()) : null;

        final BooleanExpression organisationExpression = createOrganisationExpression(RHY, filters.getOrgType(), filters.getOrgCode());

        return jpaQueryFactory.selectFrom(EVENT)
                .join(EVENT.rhy, RHY)
                .where(typeExpression
                        .and(cooperationExpression)
                        .and(statusExpression)
                        .and(beginDateExpression)
                        .and(endDateExpression)
                        .and(organisationExpression))
                .orderBy(EVENT.date.asc(), EVENT.beginTime.asc(), EVENT.id.asc())
                .fetch();
    }

    private BooleanExpression createOrganisationExpression(final QRiistanhoitoyhdistys RHY, final OrganisationType orgType, final String orgCode) {
        if (orgType == null || orgCode == null) {
            return null;
        }
        if (orgType == OrganisationType.RHY) {
            return RHY.officialCode.eq(orgCode);
        }
        if (orgType == OrganisationType.RKA) {
            return RHY.parentOrganisation.officialCode.eq(orgCode);
        }
        return null;
    }

}
