package fi.riista.feature.permit.application.conflict;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplication.Status;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.sql.SQHarvestPermitApplication;
import fi.riista.sql.SQHarvestPermitApplicationConflict;
import fi.riista.sql.SQHarvestPermitApplicationConflictPalsta;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQPalstaalue;
import fi.riista.sql.SQValtionmaa;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.util.DateUtil.huntingYear;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class SearchApplicationConflictsFeature {

    private static final Logger LOG = LoggerFactory.getLogger(SearchApplicationConflictsFeature.class);

    private static int DEFAULT_SPLICE_SIZE = 16384;

    private final LoadingCache<Integer, Boolean> VALTIONMAA_PALSTA_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, Boolean>() {
                @Override
                public Boolean load(final Integer aLong) throws Exception {
                    return filterStateArea(Collections.singletonList(aLong)).contains(aLong);
                }

                @Override
                public Map<Integer, Boolean> loadAll(final Iterable<? extends Integer> keys) throws Exception {
                    final ImmutableList<Integer> ints = ImmutableList.copyOf(keys);
                    final Set<Integer> valtiomaaPalstas = filterStateArea(ints);
                    return ints.stream().collect(toMap(identity(), valtiomaaPalstas::contains));
                }
            });

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationConflictRepository harvestPermitApplicationConflictRepository;

    @Resource
    private HarvestPermitApplicationConflictPalstaRepository harvestPermitApplicationConflictPalstaRepository;

    @Resource
    private HarvestPermitApplicationConflictBatchRepository harvestPermitApplicationConflictBatchRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public Long getPendingBatchId() {
        final QHarvestPermitApplicationConflictBatch BATCH = QHarvestPermitApplicationConflictBatch.harvestPermitApplicationConflictBatch;

        return jpqlQueryFactory.select(BATCH.id.min())
                .from(BATCH)
                .where(BATCH.completedAt.isNull())
                .fetchOne();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void markBatchComplete(final long batchId) {
        final QHarvestPermitApplicationConflictBatch BATCH = QHarvestPermitApplicationConflictBatch.harvestPermitApplicationConflictBatch;

        jpqlQueryFactory.update(BATCH)
                .set(BATCH.completedAt, DateUtil.now())
                .where(BATCH.id.eq(batchId))
                .execute();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public boolean isConflictCalculationPending(final long batchId) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitApplicationConflict CONFLICT = QHarvestPermitApplicationConflict.harvestPermitApplicationConflict;
        final QHarvestPermitApplicationConflictPalsta CONFLICT_PALSTA = QHarvestPermitApplicationConflictPalsta.harvestPermitApplicationConflictPalsta;

        final long conflictCount = jpqlQueryFactory.select(CONFLICT.id)
                .from(CONFLICT)
                .where(CONFLICT.batchId.eq(batchId))
                .fetchCount();

        final long palstaConflictCount = jpqlQueryFactory.select(CONFLICT_PALSTA.id)
                .from(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.batchId.eq(batchId))
                .fetchCount();

        final long applicationCount = jpqlQueryFactory.select(APPLICATION.id)
                .from(APPLICATION)
                .where(APPLICATION.area.isNotNull())
                .where(APPLICATION.applicationYear.gt(huntingYear()))
                .where(APPLICATION.status.in(Status.ACTIVE, Status.AMENDING))
                .where(APPLICATION.harvestPermitCategory.eq(HarvestPermitCategory.MOOSELIKE))
                .fetchCount();

        // 1. There should be some applications to process
        // 2. There should always be some conflicting applications or conflict calculation is already complete.
        // 3. Finally check if there are no results available from stage 2, which means first step has been interrupted.
        return applicationCount > 0 && (conflictCount == 0 || palstaConflictCount == 0);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<Long> getApplicationIdsToCalculate(final long batchId) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitApplicationConflict CONFLICT = QHarvestPermitApplicationConflict.harvestPermitApplicationConflict;

        // Try to restart calculation by searching for applications to skip from previous run
        final Long greatestPreviouslyCalculatedApplicationId = jpqlQueryFactory
                .select(CONFLICT.firstApplication.id.max())
                .from(CONFLICT)
                .where(CONFLICT.batchId.eq(batchId))
                .fetchOne();

        final Predicate extraPredicate = greatestPreviouslyCalculatedApplicationId != null
                ? APPLICATION.id.gt(greatestPreviouslyCalculatedApplicationId)
                : null;

        return jpqlQueryFactory.select(APPLICATION.id)
                .from(APPLICATION)
                .where(APPLICATION.area.isNotNull())
                .where(APPLICATION.applicationYear.gt(huntingYear()))
                .where(APPLICATION.status.in(Status.ACTIVE, Status.AMENDING))
                .where(APPLICATION.harvestPermitCategory.eq(HarvestPermitCategory.MOOSELIKE))
                .where(extraPredicate)
                .orderBy(APPLICATION.id.asc())
                .fetch();
    }

    // Not authorized, should only be set to run from admin context
    @Transactional
    public void calculateConflictingApplications(final long applicationId, final long batchId) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        LOG.info("Calculating conflicts for applicationId {}", applicationId);

        final HarvestPermitApplication firstApplication = harvestPermitApplicationRepository.getOne(applicationId);

        if (harvestPermitApplicationSpeciesAmountRepository.countByHarvestPermitApplication(firstApplication) == 0) {
            LOG.info("Skipping applicationId={} without species", firstApplication.getId());
            return;
        }

        if (harvestPermitApplicationRepository.findApplicationsWithAlsoOtherThanStateMooseLandsFromList(singletonList(firstApplication)).isEmpty()) {
            LOG.info("Skipping applicationId={} contains only state moose lands", firstApplication.getId());
            return;
        }

        List<HarvestPermitApplication> intersecting =
                harvestPermitApplicationRepository.findIntersecting(
                        firstApplication.getId(),
                        firstApplication.getApplicationYear());

        intersecting = harvestPermitApplicationRepository.findApplicationsWithAlsoOtherThanStateMooseLandsFromList(intersecting);

        final long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);

        for (final HarvestPermitApplication secondApplication : intersecting) {
            if (!applicationSpeciesIntersect(firstApplication, secondApplication)) {
                LOG.info("Skipping conflicting application with different species firstApplicationId={} secondApplicationId={}",
                        firstApplication.getId(), secondApplication.getId());
                continue;
            }

            LOG.info("Storing conflict firstApplicationId={} secondApplicationId={}",
                    firstApplication.getId(), secondApplication.getId());

            harvestPermitApplicationConflictRepository.save(
                    new HarvestPermitApplicationConflict(batchId, firstApplication, secondApplication, elapsed));
        }
    }

    private boolean applicationSpeciesIntersect(final HarvestPermitApplication first,
                                                final HarvestPermitApplication second) {
        return F.containsAny(
                harvestPermitApplicationSpeciesAmountRepository.findSpeciesByApplication(first),
                harvestPermitApplicationSpeciesAmountRepository.findSpeciesByApplication(second));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<Long> getConflictIdListForSecondStep(final long batchId) {
        final SQHarvestPermitApplicationConflict CONFLICT = SQHarvestPermitApplicationConflict.harvestPermitApplicationConflict;
        final SQHarvestPermitApplicationConflictPalsta CONFLICT_PALSTA = SQHarvestPermitApplicationConflictPalsta.harvestPermitApplicationConflictPalsta;
        final SQHarvestPermitApplication FIRST_APPLICATION = SQHarvestPermitApplication.harvestPermitApplication;
        final SQOrganisation RHY = SQOrganisation.organisation;

        final BooleanExpression noPalstaConflictExists = SQLExpressions.selectOne()
                .from(CONFLICT_PALSTA)
                .where(CONFLICT_PALSTA.batchId.eq(batchId))
                .where(CONFLICT_PALSTA.firstApplicationId.eq(CONFLICT.firstApplicationId),
                        CONFLICT_PALSTA.secondApplicationId.eq(CONFLICT.secondApplicationId))
                .notExists();

        return sqlQueryFactory.select(CONFLICT.harvestPermitApplicationConflictId)
                .from(CONFLICT)
                .join(FIRST_APPLICATION).on(CONFLICT.firstApplicationId.eq(FIRST_APPLICATION.harvestPermitApplicationId))
                .join(RHY).on(FIRST_APPLICATION.rhyId.eq(RHY.organisationId))
                // XXX: Only calculate pairwise conflicts once by ordering a.id < b.id
                .where(CONFLICT.firstApplicationId.lt(CONFLICT.secondApplicationId))
                .where(CONFLICT.batchId.eq(batchId))
                .where(noPalstaConflictExists)
                // Prioritize southern RHY first to speedup processing
                .orderBy(RHY.latitude.asc())
                .fetch();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(rollbackFor = ExecutionException.class)
    public String adminRetryConflictCalculation(final long batchId, final long conflictId, final int chunkSize) throws ExecutionException {
        final HarvestPermitApplicationConflictBatch batch = harvestPermitApplicationConflictBatchRepository.getOne(batchId);
        checkArgument(batch.getCompletedAt() != null, "Batch must be completed");

        final HarvestPermitApplicationConflict conflict = harvestPermitApplicationConflictRepository.getOne(conflictId);
        checkArgument(conflict.getBatchId().equals(batchId), "Batch id must match");

        if (conflict.getProcessingPalstaSeconds() == null) {
            doCalculateConflict(conflictId, chunkSize);
            return "calculated";
        }

        return "already finished";
    }

    // Not authorized, should only be set to run from admin context
    @Transactional(rollbackFor = ExecutionException.class)
    public void calculatePalstaListForConflict(final long conflictId) throws ExecutionException {
        doCalculateConflict(conflictId, DEFAULT_SPLICE_SIZE);
    }

    private void doCalculateConflict(final long conflictId, final int chunkSize) throws ExecutionException {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final HarvestPermitApplicationConflict conflict = harvestPermitApplicationConflictRepository.getOne(conflictId);

        LOG.info("Calculating palsta conflicts for firstApplicationId={} secondApplicationId={}",
                F.getId(conflict.getFirstApplication()), F.getId(conflict.getSecondApplication()));

        final List<HarvestPermitApplicationConflictPalsta> listOfConflicts =
                harvestPermitApplicationRepository.findIntersectingPalsta(
                        conflict.getFirstApplication(), conflict.getSecondApplication(), chunkSize);

        final List<Integer> palstaIds = listOfConflicts.stream()
                .mapToInt(HarvestPermitApplicationConflictPalsta::getPalstaId).boxed().collect(toList());

        final Set<Integer> metsahallitusPalstaIds = VALTIONMAA_PALSTA_CACHE.getAll(palstaIds).entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        listOfConflicts.forEach(palsta -> {
            palsta.setBatchId(conflict.getBatchId());
            palsta.setMetsahallitus(metsahallitusPalstaIds.contains(palsta.getPalstaId()));

            LOG.info("Storing conflict first={} second={} palstaId={} palstaName={} areaSize={}, waterSize={}",
                    conflict.getFirstApplication().getId(),
                    conflict.getSecondApplication().getId(),
                    palsta.getPalstaId(),
                    palsta.getPalstaNimi(),
                    palsta.getConflictAreaSize(),
                    palsta.getConflictAreaWaterSize());
        });

        harvestPermitApplicationConflictPalstaRepository.saveAll(listOfConflicts);

        if (listOfConflicts.isEmpty()) {
            LOG.warn("Removing conflict with no palsta conflicts for firstApplicationId={} secondApplicationId={}",
                    conflict.getFirstApplication().getId(),
                    conflict.getSecondApplication().getId());
            harvestPermitApplicationConflictRepository.delete(conflict);

            final QHarvestPermitApplicationConflict CONFLICT =
                    QHarvestPermitApplicationConflict.harvestPermitApplicationConflict;

            // Also delete opposite conflict
            jpqlQueryFactory.delete(CONFLICT).where(
                            CONFLICT.firstApplication.eq(conflict.getSecondApplication()),
                            CONFLICT.secondApplication.eq(conflict.getFirstApplication()))
                    .execute();
        }
        conflict.setProcessingPalstaSeconds(stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private Set<Integer> filterStateArea(final List<? extends Integer> listOfPalstaId) {
        final SQValtionmaa VALTIONMAA = SQValtionmaa.valtionmaa;
        final SQPalstaalue PALSTA_ALUE = SQPalstaalue.palstaalue;

        return ImmutableSet.copyOf(sqlQueryFactory
                .select(PALSTA_ALUE.id)
                .from(PALSTA_ALUE)
                .where(PALSTA_ALUE.id.in(listOfPalstaId)
                        .and(JPAExpressions.selectOne()
                                .from(VALTIONMAA)
                                .where(VALTIONMAA.geom.intersects(PALSTA_ALUE.geom.buffer(-0.01)))
                                .exists()
                        )).fetch());
    }
}
