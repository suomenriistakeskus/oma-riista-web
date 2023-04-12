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
    public List<HarvestPermitApplicationConflictPalsta> listAll(final long batchId,
                                                                final HarvestPermitApplication firstApplication,
                                                                final HarvestPermitApplication secondApplication) {
        return jpqlQueryFactory.selectFrom(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.batchId.eq(batchId))
                .where(palstaPredicate(firstApplication, secondApplication))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplicationConflictPalsta> listAll(final long batchId,
                                                                final HarvestPermitApplication firstApplication,
                                                                final List<HarvestPermitApplication> otherApplicationList) {
        return jpqlQueryFactory.selectFrom(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.batchId.eq(batchId))
                .where(palstaPredicate(firstApplication, otherApplicationList))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ConflictSummaryDTO> countConflictSummaries(final long batchId,
                                                                final HarvestPermitApplication application,
                                                                final List<HarvestPermitApplication> conflicting) {
        final NumberExpression<Integer> mhCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isTrue()).then(1)
                .otherwise(0)
                .sum();

        final NumberExpression<Integer> privateCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isFalse()).then(1)
                .otherwise(0)
                .sum();

        final NumberExpression<Double> privateAreaSumExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isFalse()).then(CONFLICT_PALSTA.conflictAreaSize)
                .otherwise(0.0)
                .sum();

        final NumberExpression<Double> privateAreaWaterSumExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isFalse()).then(CONFLICT_PALSTA.conflictAreaWaterSize)
                .otherwise(0.0)
                .sum();

        return jpqlQueryFactory
                .select(CONFLICT_PALSTA.firstApplication.id, CONFLICT_PALSTA.secondApplication.id,
                        mhCountExpression, privateCountExpression, CONFLICT_PALSTA.conflictAreaSize.sum(),
                        CONFLICT_PALSTA.conflictAreaWaterSize.sum(), privateAreaSumExpression, privateAreaWaterSumExpression)
                .from(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.batchId.eq(batchId))
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
                    final Double conflictWaterSum = t.get(CONFLICT_PALSTA.conflictAreaWaterSize.sum());
                    final Double privateAreaSum = t.get(privateAreaSumExpression);
                    final Double privateAreaWaterSum = t.get(privateAreaWaterSumExpression);
                    return new ConflictSummaryDTO(applicationId, mhCount, privateCount, conflictSum, conflictWaterSum, privateAreaSum, privateAreaWaterSum);
                })
                .collect(toMap(ConflictSummaryDTO::getApplicationId, Function.identity()));

    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, PalstaConflictSummaryDTO> getPalstaConflictSummaries(final List<Integer> palstaIds, final HarvestPermitApplication application, final HarvestPermitApplication conflictingApplication) {
        final NumberExpression<Integer> mhCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isTrue()).then(1)
                .otherwise(0)
                .sum();

        final NumberExpression<Integer> privateCountExpression = new CaseBuilder()
                .when(CONFLICT_PALSTA.metsahallitus.isFalse()).then(1)
                .otherwise(0)
                .sum();

        return jpqlQueryFactory
                .select(CONFLICT_PALSTA.palstaId,
                        mhCountExpression, privateCountExpression, CONFLICT_PALSTA.conflictAreaSize.sum(),
                        CONFLICT_PALSTA.conflictAreaWaterSize.sum())
                .from(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.palstaId.in(palstaIds))
                .where(palstaPredicate(application, conflictingApplication))
                .groupBy(CONFLICT_PALSTA.palstaId)
                .fetch().stream()
                .map(t -> {
                    final Integer palstaId = t.get(CONFLICT_PALSTA.palstaId);
                    final Integer mhCount = t.get(mhCountExpression);
                    final Integer privateCount = t.get(privateCountExpression);
                    final Double conflictSum = t.get(CONFLICT_PALSTA.conflictAreaSize.sum());
                    final Double conflictWaterSum = t.get(CONFLICT_PALSTA.conflictAreaWaterSize.sum());
                    return new PalstaConflictSummaryDTO(palstaId, mhCount, privateCount, conflictSum, conflictWaterSum);
                })
                .collect(toMap(PalstaConflictSummaryDTO::getPalstaId, Function.identity()));
    }
}
