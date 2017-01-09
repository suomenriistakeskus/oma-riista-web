package fi.riista.feature.huntingclub.statistics;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.sql.SQHarvestPermit;
import fi.riista.sql.SQHarvestPermitPartners;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class HuntingClubStatisticsFeature {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR')")
    public List<HuntingClubStatisticsRow> calculate(final boolean includePermitHolders) {
        return new HuntingClubStatistics(new HuntingClubStatisticsRkaQueries(queryFactory))
                .calculate(listClubsWithPermit(includePermitHolders));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR')")
    public List<HuntingClubStatisticsRow> calculate() {
        return new HuntingClubStatistics(new HuntingClubStatisticsRkaQueries(queryFactory))
                .calculate(listClubsWithPermit(true));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR')")
    public List<HuntingClubStatisticsRow> calculateByRka(Long rkaId, final boolean includePermitHolders) {
        return new HuntingClubStatistics(new HuntingClubStatisticsRhyQueries(queryFactory, rkaId))
                .calculate(listClubsWithPermit(includePermitHolders));
    }

    private Set<Long> listClubsWithPermit(final boolean includePermitHolders) {
        final Set<Long> result = new HashSet<>();
        result.addAll(findAllPermitPartners());

        if (includePermitHolders) {
            result.addAll(findAllPermitHolders());
        }

        return result;
    }

    private List<Long> findAllPermitPartners() {
        final SQHarvestPermit harvestPermit = new SQHarvestPermit("hp");
        final SQHarvestPermitPartners harvestPermitPartners = new SQHarvestPermitPartners("hpp");

        final SQLQuery<Long> mooseLikeHarvestPermitIds = SQLExpressions.select(harvestPermit.harvestPermitId)
                .from(harvestPermit)
                .where(harvestPermit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE));

        return sqlQueryFactory.from(harvestPermitPartners)
                .where(harvestPermitPartners.harvestPermitId.in(mooseLikeHarvestPermitIds))
                .select(harvestPermitPartners.organisationId)
                .fetch();
    }

    private List<Long> findAllPermitHolders() {
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;

        return queryFactory.from(harvestPermit).select(harvestPermit.permitHolder.id)
                .where(harvestPermit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE),
                        harvestPermit.permitHolder.isNotNull())
                .fetch();
    }
}
