package fi.riista.feature.permit.application.conflict;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class HarvestPermitApplicationConflictPalstaRepositoryImpl implements HarvestPermitApplicationConflictPalstaRepositoryCustom {

    private static final QHarvestPermitApplicationConflictPalsta CONFLICT_PALSTA = QHarvestPermitApplicationConflictPalsta.harvestPermitApplicationConflictPalsta;

    private static BooleanExpression palstaPredicate(final HarvestPermitApplication firstApplication,
                                                     final HarvestPermitApplication secondApplication) {
        // Pairwise conflicts are only calculated once when a.id < b.id
        return firstApplication.getId() < secondApplication.getId()
                ? CONFLICT_PALSTA.firstApplication.eq(firstApplication).and(CONFLICT_PALSTA.secondApplication.eq(secondApplication))
                : CONFLICT_PALSTA.firstApplication.eq(secondApplication).and(CONFLICT_PALSTA.secondApplication.eq(firstApplication));
    }


    private static BooleanExpression palstaPredicate(final HarvestPermitApplication firstApplication,
                                                     final List<HarvestPermitApplication> otherApplicationList) {
        final BooleanExpression p1 = CONFLICT_PALSTA.firstApplication.eq(firstApplication).and(CONFLICT_PALSTA.secondApplication.in(otherApplicationList));
        final BooleanExpression p2 = CONFLICT_PALSTA.firstApplication.in(otherApplicationList).and(CONFLICT_PALSTA.secondApplication.eq(firstApplication));
        return p1.or(p2);
    }

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictPalsta> listAll(final HarvestPermitApplication firstApplication,
                                                                final HarvestPermitApplication secondApplication) {
        return jpqlQueryFactory.selectFrom(CONFLICT_PALSTA).where(palstaPredicate(firstApplication, secondApplication)).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictPalsta> listAll(final HarvestPermitApplication firstApplication,
                                                                final List<HarvestPermitApplication> otherApplicationList) {
        return jpqlQueryFactory.selectFrom(CONFLICT_PALSTA).where(palstaPredicate(firstApplication, otherApplicationList)).fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ConfictSummaryDTO> countConflictSummaries(final HarvestPermitApplication application,
                                                               final List<HarvestPermitApplication> conflicting) {
        final NumberExpression<Integer> mhCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isTrue()).then(1)
                .otherwise(0)
                .sum();

        final NumberExpression<Integer> privateCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isFalse()).then(1)
                .otherwise(0)
                .sum();

        return jpqlQueryFactory
                .select(CONFLICT_PALSTA.firstApplication.id, CONFLICT_PALSTA.secondApplication.id,
                        mhCountExpression, privateCountExpression, CONFLICT_PALSTA.conflictAreaSize.sum())
                .from(CONFLICT_PALSTA)
                .where(palstaPredicate(application, conflicting))
                .groupBy(CONFLICT_PALSTA.firstApplication.id, CONFLICT_PALSTA.secondApplication.id)
                .fetch().stream()
                .map(t -> {
                    final Long id1 = t.get(CONFLICT_PALSTA.firstApplication.id);
                    final Long id2 = t.get(CONFLICT_PALSTA.secondApplication.id);
                    final Long applicationId = id1.equals(application.getId()) ? id2 : id1;
                    final Integer mhCount = t.get(mhCountExpression);
                    final Integer privateCount = t.get(privateCountExpression);
                    final Double conflictSum = t.get(CONFLICT_PALSTA.conflictAreaSize.sum());
                    return new ConfictSummaryDTO(applicationId, mhCount, privateCount, conflictSum);
                })
                .collect(toMap(ConfictSummaryDTO::getApplicationId, Function.identity()));

    }
}
