package fi.riista.integration.metsahallitus.permit;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Map;

@Component
public class MetsahallitusPermitStatisticsFeature {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;


    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public MetsahallitusPermitStatisticsDTO listStatistics(final Locale locale) {

        final QMetsahallitusPermit MH_PERMIT = QMetsahallitusPermit.metsahallitusPermit;

        // Currently metsähallitus permits have null permit_type_swedish-values for some permits so returning types
        // only in Finnish for now.
//        final StringPath permitPath = Locales.isSwedish(locale) ? MH_PERMIT.permitTypeSwedish : MH_PERMIT.permitType;
        final StringPath permitPath = MH_PERMIT.permitType;

        final BooleanExpression validPeriodPredicate =
                MH_PERMIT.beginDate.isNotNull().and(MH_PERMIT.endDate.isNotNull());

        final long hunterCount = jpqlQueryFactory
                .selectDistinct(MH_PERMIT.hunterNumber)
                .from(MH_PERMIT)
                .where(validPeriodPredicate)
                .fetchCount();

        final Map<String, Long> permitCounts = jpqlQueryFactory
                .select(permitPath, permitPath.count())
                .from(MH_PERMIT)
                .where(validPeriodPredicate)
                .groupBy(permitPath)
                .transform(GroupBy.groupBy(permitPath).as(permitPath.count()));

        final long invalidPermitCount = jpqlQueryFactory
                .select(MH_PERMIT.count())
                .from(MH_PERMIT)
                .where(validPeriodPredicate.not())
                .fetchCount();


        final long swedishTypeMissingCount = jpqlQueryFactory
                .select(MH_PERMIT.count())
                .from(MH_PERMIT)
                .where(MH_PERMIT.permitTypeSwedish.isNull())
                .fetchCount();

        return new MetsahallitusPermitStatisticsDTO(hunterCount, permitCounts, invalidPermitCount,
                swedishTypeMissingCount);
    }
}
