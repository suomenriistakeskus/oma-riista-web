package fi.riista.feature.permit.application.conflict;

import com.google.common.base.Stopwatch;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.gis.metsahallitus.MetsahallitusHirviRepository;
import fi.riista.feature.gis.metsahallitus.MetsahallitusProperties;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmountRepository;
import fi.riista.sql.SQHarvestPermitApplication;
import fi.riista.sql.SQHarvestPermitApplicationConflict;
import fi.riista.sql.SQHarvestPermitApplicationConflictPalsta;
import fi.riista.sql.SQOrganisation;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Component
public class SearchApplicationConflictsFeature {

    private static final Logger LOG = LoggerFactory.getLogger(SearchApplicationConflictsFeature.class);

    private static final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitApplicationSpeciesAmountRepository harvestPermitApplicationSpeciesAmountRepository;

    @Resource
    private HarvestPermitApplicationConflictRepository harvestPermitApplicationConflictRepository;

    @Resource
    private HarvestPermitApplicationConflictPalstaRepository harvestPermitApplicationConflictPalstaRepository;

    @Resource
    private MetsahallitusProperties metsahallitusProperties;

    @Resource
    private MetsahallitusHirviRepository metsahallitusHirviRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Transactional(readOnly = true)
    public boolean isConflictCalculationPending() {
        return harvestPermitApplicationConflictRepository.count() == 0;
    }

    @Transactional(readOnly = true)
    public List<Long> getApplicationIdsToCalculate() {
        final int currentHuntingYear = DateUtil.huntingYear();
        final BooleanExpression predicate = APPLICATION.area.isNotNull()
                .and(APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE))
                .and(APPLICATION.huntingYear.gt(currentHuntingYear));

        return jpqlQueryFactory.select(APPLICATION.id)
                .from(APPLICATION)
                .where(predicate)
                .orderBy(APPLICATION.id.asc())
                .fetch();
    }

    @Transactional
    public void calculateConflictingApplications(final long applicationId) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        LOG.info("Calculating conflicts for applicationId {}", applicationId);

        final HarvestPermitApplication firstApplication = harvestPermitApplicationRepository.getOne(applicationId);

        if (harvestPermitApplicationSpeciesAmountRepository.countByHarvestPermitApplication(firstApplication) == 0) {
            LOG.info("Skipping applicationId={} without species", firstApplication.getId());
            return;
        }

        final List<HarvestPermitApplication> intersecting =
                harvestPermitApplicationRepository.findIntersecting(
                        firstApplication.getId(), firstApplication.getArea().getHuntingYear());

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
                    new HarvestPermitApplicationConflict(firstApplication, secondApplication, elapsed));
        }
    }

    private boolean applicationSpeciesIntersect(final HarvestPermitApplication first,
                                                final HarvestPermitApplication second) {
        return F.containsAny(
                harvestPermitApplicationSpeciesAmountRepository.findSpeciesByApplication(first),
                harvestPermitApplicationSpeciesAmountRepository.findSpeciesByApplication(second));
    }

    @Transactional(readOnly = true)
    public List<Long> getConflictIdListForSecondStep() {
        final SQHarvestPermitApplicationConflict CONFLICT = SQHarvestPermitApplicationConflict.harvestPermitApplicationConflict;
        final SQHarvestPermitApplicationConflictPalsta CONFLICT_PALSTA = SQHarvestPermitApplicationConflictPalsta.harvestPermitApplicationConflictPalsta;
        final SQHarvestPermitApplication FIRST_APPLICATION = SQHarvestPermitApplication.harvestPermitApplication;
        final SQOrganisation RHY = SQOrganisation.organisation;

        return sqlQueryFactory.select(CONFLICT.harvestPermitApplicationConflictId)
                .from(CONFLICT)
                .join(FIRST_APPLICATION).on(CONFLICT.firstApplicationId.eq(FIRST_APPLICATION.harvestPermitApplicationId))
                .join(RHY).on(FIRST_APPLICATION.rhyId.eq(RHY.organisationId))
                // XXX: Only calculate pairwise conflicts once by ordering a.id < b.id
                .where(CONFLICT.firstApplicationId.lt(CONFLICT.secondApplicationId),
                        SQLExpressions.selectOne()
                                .from(CONFLICT_PALSTA)
                                .where(CONFLICT_PALSTA.firstApplicationId.eq(CONFLICT.firstApplicationId),
                                        CONFLICT_PALSTA.secondApplicationId.eq(CONFLICT.secondApplicationId))
                                .limit(1)
                                .notExists())
                // Prioritize southern RHY first to speedup processing
                .orderBy(RHY.latitude.asc())
                .fetch();
    }

    @Transactional
    public void calculatePalstaListForConflict(final long conflictId) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final HarvestPermitApplicationConflict conflict = harvestPermitApplicationConflictRepository.getOne(conflictId);

        LOG.info("Calculating palsta conflicts for firstApplicationId={} secondApplicationId={}",
                F.getId(conflict.getFirstApplication()), F.getId(conflict.getSecondApplication()));

        final List<HarvestPermitApplicationConflictPalsta> listOfConflicts =
                harvestPermitApplicationRepository.findIntersectingPalsta(
                        conflict.getFirstApplication(), conflict.getSecondApplication());

        final List<Integer> palstaIds = listOfConflicts.stream()
                .mapToInt(HarvestPermitApplicationConflictPalsta::getPalstaId).boxed().collect(toList());
        final Set<Integer> metsahallitusPalstaIds =
                metsahallitusHirviRepository.filterPalstaIntersectingHirvi(palstaIds, metsahallitusProperties.getLatestMetsahallitusYear());

        listOfConflicts.forEach(palsta -> {
            palsta.setMetsahallitus(metsahallitusPalstaIds.contains(palsta.getPalstaId()));

            LOG.info("Storing conflict first={} second={} palstaId={} palstaName={} areaSize={}",
                    conflict.getFirstApplication().getId(),
                    conflict.getSecondApplication().getId(),
                    palsta.getPalstaId(),
                    palsta.getPalstaNimi(),
                    palsta.getConflictAreaSize());
        });

        harvestPermitApplicationConflictPalstaRepository.save(listOfConflicts);

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
}
