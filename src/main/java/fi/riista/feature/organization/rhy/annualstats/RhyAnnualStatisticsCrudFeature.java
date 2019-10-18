package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsStateTransitionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.function.BiConsumer;

import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsAuthorization.Permission.MODERATOR_UPDATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

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

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private RhyAnnualStatisticsDTOTransformer dtoTransformer;

    @Transactional
    public RhyAnnualStatisticsDTO getOrCreate(final long rhyId, final int calendarYear) {
        final Riistanhoitoyhdistys rhy = rhyRepo.getOne(rhyId);

        return annualStatsRepo
                .findByRhyAndYear(rhy, calendarYear)
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
                    dto.setSrva(new SrvaEventStatistics());
                    dto.setHunterExamTraining(new HunterExamTrainingStatisticsDTO());
                    dto.setJhtTraining(new JHTTrainingStatistics());
                    dto.setHunterTraining(new HunterTrainingStatistics());
                    dto.setYouthTraining(new YouthTrainingStatistics());
                    dto.setOtherHunterTraining(new OtherHunterTrainingStatistics());
                    dto.setPublicEvents(new PublicEventStatistics());
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
        stateTransitionService.transitionToNotStarted(entity);
    }

    @Override
    protected RhyAnnualStatisticsDTO toDTO(final RhyAnnualStatistics statistics) {
        return dtoTransformer.transform(statistics);
    }

    @Override
    protected void updateEntity(final RhyAnnualStatistics entity, final RhyAnnualStatisticsDTO dto) {
        if (!entity.isNew()) {
            throw new UnsupportedOperationException("update not supported");
        }

        final int calendarYear = dto.getYear();

        if (calendarYear < 2017) {
            throw new IllegalArgumentException("Cannot create annual statistics for years prior to 2017");
        }

        entity.setYear(calendarYear);
        entity.setRhy(rhyRepo.getOne(dto.getRhyId()));

        annualStatsService.refresh(entity);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateBasicInfo(final long id, @Nonnull final RhyBasicInfoDTO dto) {
        return update(requireEntity(id, MODERATOR_UPDATE), dto, annualStatsService::updateBasicInfo);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateHunterExams(final long id, @Nonnull final HunterExamStatistics input) {
        return update(id, input, annualStatsService::updateHunterExams);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateHunterExams(final long id,
                                                             @Nonnull final HunterExamStatisticsDTO dto) {

        return update(requireEntity(id, MODERATOR_UPDATE), dto, annualStatsService::moderatorUpdateHunterExams);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateShootingTests(final long id,
                                                               @Nonnull final AnnualShootingTestStatisticsDTO dto) {

        return update(requireEntity(id, MODERATOR_UPDATE), dto, annualStatsService::moderatorUpdateShootingTests);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateHuntingControl(final long id, @Nonnull final HuntingControlStatistics input) {
        return update(id, input, annualStatsService::updateHuntingControl);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateGameDamage(final long id, @Nonnull final GameDamageStatistics input) {
        return update(id, input, annualStatsService::updateGameDamage);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateOtherPublicAdmin(final long id,
                                                         @Nonnull final OtherPublicAdminStatistics input) {

        return update(id, input, annualStatsService::updateOtherPublicAdmin);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateHunterExamTraining(final long id,
                                                           @Nonnull final HunterExamTrainingStatistics input) {

        return update(id, input, annualStatsService::updateHunterExamTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateHunterExamTraining(final long id,
                                                                    @Nonnull final HunterExamTrainingStatisticsDTO input) {

        return update(requireEntity(id, MODERATOR_UPDATE), input,
                annualStatsService::moderatorUpdateHunterExamTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateJhtTraining(final long id, @Nonnull final JHTTrainingStatistics input) {
        return update(id, input, annualStatsService::updateJhtTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateHunterTraining(final long id, @Nonnull final HunterTrainingStatistics input) {
        return update(id, input, annualStatsService::updateHunterTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateYouthTraining(final long id, @Nonnull final YouthTrainingStatistics input) {
        return update(id, input, annualStatsService::updateYouthTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateOtherHunterTraining(final long id,
                                                            @Nonnull final OtherHunterTrainingStatistics input) {

        return update(id, input, annualStatsService::updateOtherHunterTraining);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updatePublicEvents(final long id, @Nonnull final PublicEventStatistics input) {
        return update(id, input, annualStatsService::updatePublicEvents);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateOtherHuntingRelated(final long id,
                                                            @Nonnull final OtherHuntingRelatedStatistics input) {

        return update(id, input, annualStatsService::updateOtherHuntingRelated);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateOtherHuntingRelated(final long id,
                                                                     @Nonnull final OtherHuntingRelatedStatistics input) {

        return update(requireEntity(id, MODERATOR_UPDATE), input,
                annualStatsService::moderatorUpdateOtherHuntingRelated);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateCommunication(final long id, @Nonnull final CommunicationStatistics input) {
        return update(id, input, annualStatsService::updateCommunication);
    }

    @Transactional
    public RhyAnnualStatisticsDTO updateShootingRanges(final long id, @Nonnull final ShootingRangeStatistics input) {
        return update(id, input, annualStatsService::updateShootingRanges);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateLuke(final long id, @Nonnull final LukeStatistics input) {
        return update(requireEntity(id, MODERATOR_UPDATE), input, annualStatsService::updateLuke);
    }

    @Transactional
    public RhyAnnualStatisticsDTO moderatorUpdateMetsahallitus(final long id,
                                                               @Nonnull final MetsahallitusStatistics input) {

        return update(requireEntity(id, MODERATOR_UPDATE), input, annualStatsService::updateMetsahallitus);
    }

    private <DTO> RhyAnnualStatisticsDTO update(final long id,
                                                final DTO dto,
                                                final BiConsumer<RhyAnnualStatistics, DTO> updater) {

        return update(requireEntity(id, UPDATE), dto, updater);
    }

    private <DTO> RhyAnnualStatisticsDTO update(final RhyAnnualStatistics entity,
                                                final DTO dto,
                                                final BiConsumer<RhyAnnualStatistics, DTO> updater) {
        updater.accept(entity, dto);
        return toDTO(entity);
    }

    private RhyAnnualStatistics requireEntity(final long statisticsId, final Enum<?> permission) {
        return requireEntityService.requireRhyAnnualStatistics(statisticsId, permission);
    }
}
