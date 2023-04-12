package fi.riista.feature.harvestpermit.report.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.season.QHarvestQuota;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class HarvestReportSearchRepository {

    private static final int MAX_RESULTS = 100_000;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;
    
    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class, propagation = Propagation.MANDATORY)
    public List<Harvest> queryForList(final HarvestReportSearchDTO params) {
        final List<Harvest> harvestList = createBaseQuery(params)
                .limit(MAX_RESULTS)
                .fetch();
        assertOnlyHarvestReports(harvestList);

        return harvestList;
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class, propagation = Propagation.MANDATORY)
    public Slice<Harvest> queryForSlice(final HarvestReportSearchDTO params, final Pageable pageRequest) {
        final List<Harvest> harvestList = createBaseQuery(params)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();

        assertOnlyHarvestReports(harvestList);

        return BaseRepositoryImpl.toSlice(harvestList, pageRequest);
    }

    private static void assertOnlyHarvestReports(final List<Harvest> harvestList) {
        // additional safety check
        for (final Harvest harvest : harvestList) {
            if (harvest.getHarvestReportState() == null) {
                throw new RuntimeException("Implementation failure. Attempted to return harvest without report");
            }

            if (harvest.getHarvestPermit() == null && harvest.getHarvestSeason() == null) {
                throw new RuntimeException("Implementation failure. Attempted to return harvest without season or permit");
            }

            if (harvest.getHuntingDayOfGroup() != null) {
                throw new RuntimeException("Implementation failure. Attmepted to return harvest attached to group hunting day");
            }
        }
    }

    private List<Long> getAreaRhyIds(final HarvestReportSearchDTO params) {
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;

        return jpqlQueryFactory
                .select(RHY.id)
                .from(RHY)
                .where(RHY.parentOrganisation.id.eq(params.getAreaId()))
                .fetch();
    }

    private JPQLQuery<Harvest> createBaseQuery(final HarvestReportSearchDTO params) {
        final QHarvest HARVEST = QHarvest.harvest;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final JPQLQuery<Harvest> query = jpqlQueryFactory.selectFrom(HARVEST);

        if (StringUtils.isNotBlank(params.getPermitNumber())) {
            query.innerJoin(HARVEST.harvestPermit, PERMIT);
        } else if (params.isCoordinatorSearch()) {
            query.leftJoin(HARVEST.harvestPermit, PERMIT);
        }

        if (params.getStates() == null || params.getStates().isEmpty()) {
            throw new IllegalArgumentException("states is required");
        }

        final BooleanBuilder builder = new BooleanBuilder()
                // Sanity check:
                // should not have hunting day
                // should be attached to season or permit
                // should always have report state
                .and(HARVEST.huntingDayOfGroup.isNull())
                .and(HARVEST.harvestSeason.isNotNull().or(HARVEST.harvestPermit.isNotNull()))
                .and(HARVEST.harvestReportState.isNotNull())
                .and(HARVEST.harvestReportState.in(params.getStates()));

        if (params.hasBeginAndEndDate()) {
            final Interval dateInterval = DateUtil.createDateInterval(params.getBeginDate(), params.getEndDate());
            builder.and(HARVEST.pointOfTime.between(
                    dateInterval.getStart(),
                    dateInterval.getEnd()));
        } else if (params.hasBeginDate()) {
            builder.and(HARVEST.pointOfTime.goe(DateUtil.toDateTimeNullSafe(params.getBeginDate())));
        } else if (params.hasEndDate()) {
            builder.and(HARVEST.pointOfTime.loe(DateUtil.toDateTimeNullSafe(params.getEndDate())));
        }

        if (params.getSeasonId() != null) {
            // SEASON
            builder.and(HARVEST.harvestSeason.id.eq(params.getSeasonId()));

        } else if (params.getGameSpeciesCode() != null) {
            // PERMIT
            builder.and(HARVEST.species.eq(gameSpeciesService.requireByOfficialCode(params.getGameSpeciesCode())));
            builder.and(HARVEST.harvestPermit.isNotNull());
        }

        if (StringUtils.isNotBlank(params.getPermitNumber())) {
            builder.and(PERMIT.permitNumber.eq(params.getPermitNumber()));
        }

        if (params.isModeratorSearch()) {
            if (params.getPersonId() != null) {
                final BooleanExpression author = HARVEST.author.id.eq(params.getPersonId());
                final BooleanExpression shooter = HARVEST.actualShooter.id.eq(params.getPersonId());
                builder.and(author.or(shooter));
            }

            if (params.getRhyId() != null) {
                builder.and(HARVEST.rhy.id.eq(params.getRhyId()));
            } else if (params.getAreaId() != null) {
                builder.and(HARVEST.rhy.id.in(getAreaRhyIds(params)));
            }

            if (params.getHarvestAreaId() != null) {
                final QHarvestQuota QUOTA = QHarvestQuota.harvestQuota;
                query.join(HARVEST.harvestQuota, QUOTA);
                builder.and(QUOTA.harvestArea.id.eq(params.getHarvestAreaId()));
            }

            if (StringUtils.isNotBlank(params.getText())) {
                for (final String s : params.getText().trim().split(" ")) {
                    builder.and(HARVEST.harvestReportMemo.likeIgnoreCase("%" + s.trim() + "%"));
                }
            }

        } else if (params.isCoordinatorSearch()) {
            if (params.getRhyId() != null) {
                final BooleanExpression permitRhyRelation = PERMIT.rhy.id.eq(params.getRhyId());
                final BooleanExpression directRhyRelation = HARVEST.rhy.id.eq(params.getRhyId());
                builder.and(permitRhyRelation.or(directRhyRelation));
            } else {
                throw new IllegalArgumentException("missing rhyId");
            }
        } else {
            throw new IllegalArgumentException("missing searchType");
        }

        return query.where(builder).orderBy(HARVEST.harvestReportDate.desc(), HARVEST.id.desc());
    }
}
