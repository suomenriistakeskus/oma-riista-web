package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.security.EntityPermission.READ;

@Service
public class RhyAnnualStatisticsCrudFeature
        extends AbstractCrudFeature<Long, RhyAnnualStatistics, RhyAnnualStatisticsDTO> {

    @Resource
    private RhyAnnualStatisticsRepository annualStatsRepo;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepo;

    @Resource
    private AnnualStatisticsService annualStatsService;

    @Resource
    private RhyAnnualStatisticsStateTransitionService stateTransitionService;

    @Transactional
    public RhyAnnualStatisticsDTO getOrCreate(final long rhyId, final int calendarYear) {
        final Riistanhoitoyhdistys rhy = rhyRepo.getOne(rhyId);

        return annualStatsRepo.findByRhyAndYear(rhy, calendarYear)
                .map(statistics -> {
                    activeUserService.assertHasPermission(statistics, READ);
                    annualStatsService.refresh(statistics);
                    return toDTO(statistics);
                })
                .orElseGet(() -> {
                    final RhyAnnualStatisticsDTO dto = new RhyAnnualStatisticsDTO(rhyId, calendarYear);

                    dto.setBasicInfo(new RhyBasicInfoDTO());
                    dto.setHunterExams(new HunterExamStatisticsDTO());
                    dto.setShootingTests(new AnnualShootingTestStatisticsDTO());
                    dto.setHuntingControl(new HuntingControlStatistics());
                    dto.setGameDamage(new GameDamageStatistics());
                    dto.setOtherPublicAdmin(new OtherPublicAdminStatistics());
                    dto.setHunterExamTraining(new HunterExamTrainingStatisticsDTO());
                    dto.setJhtTraining(new JHTTrainingStatistics());
                    dto.setStateAidTraining(new StateAidTrainingStatistics());
                    dto.setOtherHunterTraining(new OtherHunterTrainingStatistics());
                    dto.setOtherTraining(new OtherTrainingStatistics());
                    dto.setOtherHuntingRelated(new OtherHuntingRelatedStatistics());
                    dto.setCommunication(new CommunicationStatistics());
                    dto.setShootingRanges(new ShootingRangeStatistics());
                    dto.setLuke(new LukeStatistics());
                    dto.setMetsahallitus(new MetsahallitusStatistics());

                    return create(dto);
                });
    }

    @Override
    protected JpaRepository<RhyAnnualStatistics, Long> getRepository() {
        return annualStatsRepo;
    }

    @Override
    protected void afterCreate(final RhyAnnualStatistics entity, final RhyAnnualStatisticsDTO dto) {
        super.afterCreate(entity, dto);
        stateTransitionService.transitionToInProgress(entity);
    }

    @Override
    protected RhyAnnualStatisticsDTO toDTO(final RhyAnnualStatistics statistics) {
        return RhyAnnualStatisticsDTO.create(statistics);
    }

    @Override
    protected void updateEntity(final RhyAnnualStatistics entity, final RhyAnnualStatisticsDTO dto) {
        final int calendarYear = dto.getYear();
        final boolean moderator = activeUserService.isModeratorOrAdmin();
        final Riistanhoitoyhdistys rhy;

        if (entity.isNew()) {
            if (calendarYear < 2017) {
                throw new IllegalArgumentException("Cannot create annual statistics for years prior to 2017");
            }

            rhy = rhyRepo.getOne(dto.getRhyId());
            entity.setRhy(rhy);
            entity.setYear(calendarYear);
        } else {
            rhy = entity.getRhy();

            checkArgument(entity.getYear() == calendarYear, "Year mismatch");
            checkArgument(rhy.getId().equals(dto.getRhyId()), "RHY-ID mismatch");

            if (!entity.isUpdateable(moderator)) {
                throw new AnnualStatisticsLockedException();
            }
        }

        final AnnualStatisticsResolver resolver = annualStatsService.getAnnualStatisticsResolver(rhy, calendarYear);

        annualStatsService.updateBasicInfo(entity, resolver, dto.getBasicInfo(), moderator);
        annualStatsService.updateHunterExams(entity, resolver, dto.getHunterExams(), moderator);
        annualStatsService.updateShootingTestStatistics(entity, resolver, dto.getShootingTests(), moderator);
        entity.setHuntingControl(annualStatsService.refresh(dto.getHuntingControl(), resolver));
        entity.setGameDamage(annualStatsService.refresh(dto.getGameDamage(), resolver));
        entity.setOtherPublicAdmin(dto.getOtherPublicAdmin());
        entity.setSrva(annualStatsService.refreshSrvaEventStatistics(entity, resolver));
        annualStatsService.updateHunterExamTrainingStatistics(
                entity, resolver, dto.getHunterExamTraining(), moderator);
        entity.setJhtTraining(dto.getJhtTraining());
        entity.setStateAidTraining(dto.getStateAidTraining());
        entity.setOtherHunterTraining(dto.getOtherHunterTraining());
        entity.setOtherTraining(dto.getOtherTraining());
        entity.setOtherHuntingRelated(annualStatsService.refresh(dto.getOtherHuntingRelated(), resolver));
        entity.setCommunication(annualStatsService.refresh(dto.getCommunication(), resolver));
        entity.setShootingRanges(dto.getShootingRanges());
        entity.setLuke(dto.getLuke());
        entity.setMetsahallitus(dto.getMetsahallitus());
    }
}
