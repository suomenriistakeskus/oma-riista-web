package fi.riista.feature.organization.rhy.annualstats;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import io.vavr.Tuple2;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.DEPORTATION;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.INJURED_ANIMAL;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.OTHER;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.RAILWAY_ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.TRAFFIC_ACCIDENT;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJAKURSSI;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINTO;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
import static java.util.Objects.requireNonNull;

@Component
public class AnnualStatisticsService {

    @Resource
    private RhyAnnualStatisticsRepository statisticsRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private CalendarEventRepository eventRepository;

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    public AnnualStatisticsResolver getAnnualStatisticsResolver(@Nonnull final Riistanhoitoyhdistys rhy,
                                                                final int calendarYear) {

        return new AnnualStatisticsResolver(
                rhy, calendarYear, occupationRepository, eventRepository, jpaQueryFactory, sqlQueryFactory);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void refresh(@Nonnull final RhyAnnualStatistics statistics) {

        requireNonNull(statistics);

        if (statistics.canComputedPropertiesBeRefreshed()) {
            final AnnualStatisticsResolver resolver =
                    getAnnualStatisticsResolver(statistics.getRhy(), statistics.getYear());

            statistics.setBasicInfo(refresh(statistics.getOrCreateBasicInfo(), resolver));
            statistics.setHunterExams(refresh(statistics.getOrCreateHunterExams(), resolver));
            statistics.setShootingTests(refresh(statistics.getOrCreateShootingTests(), resolver));
            statistics.setGameDamage(refresh(statistics.getOrCreateGameDamage(), resolver));
            statistics.setHuntingControl(refresh(statistics.getOrCreateHuntingControl(), resolver));
            statistics.setOtherPublicAdmin(statistics.getOrCreateOtherPublicAdmin());
            statistics.setSrva(resolveSrvaEventStatistics(resolver));
            statistics.setHunterExamTraining(refresh(statistics.getOrCreateHunterExamTraining(), resolver));
            statistics.setJhtTraining(statistics.getOrCreateJhtTraining());
            statistics.setStateAidTraining(statistics.getOrCreateStateAidTraining());
            statistics.setOtherHunterTraining(statistics.getOrCreateOtherHunterTraining());
            statistics.setOtherTraining(statistics.getOrCreateOtherTraining());
            statistics.setOtherHuntingRelated(refresh(statistics.getOrCreateOtherHuntingRelated(), resolver));
            statistics.setCommunication(refresh(statistics.getOrCreateCommunication(), resolver));
            statistics.setShootingRanges(statistics.getOrCreateShootingRanges());
            statistics.setLuke(statistics.getOrCreateLuke());
            statistics.setMetsahallitus(statistics.getOrCreateMetsahallitus());

            statisticsRepository.saveAndFlush(statistics);
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public RhyBasicInfo resolveBasicInfo(@Nonnull final RhyAnnualStatistics statistics,
                                         @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final RhyBasicInfo basicInfo = statistics.getOrCreateBasicInfo();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(basicInfo, resolver) : basicInfo;
    }

    private static RhyBasicInfo refresh(@Nonnull final RhyBasicInfo basicInfo,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(basicInfo, "basicInfo is null");
        requireNonNull(resolver, "resolver is null");

        final RhyBasicInfo copy = new RhyBasicInfo(basicInfo);
        final boolean yearChanged = DateUtil.today().getYear() != resolver.getCalendarYear();

        if (basicInfo.getIban() == null) {
            Optional.ofNullable(resolver.getIbanFromPreviousYear())
                    .ifPresent(copy::setIban);
        }

        if (!yearChanged || basicInfo.getRhyMembers() == null) {
            copy.setRhyMembers(resolver.getNumberOfRhyMembers());
        }

        // Update land area size only if it is null in order to prevent manually entered value being overridden.
        if (basicInfo.getOperationalLandAreaSize() == null) {
            Optional.ofNullable(resolver.getRhyLandAreaSizeInHectares()).ifPresent(copy::setOperationalLandAreaSize);
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateBasicInfo(@Nonnull final RhyAnnualStatistics statistics,
                                @Nonnull final AnnualStatisticsResolver resolver,
                                @Nonnull final RhyBasicInfoDTO dto,
                                final boolean isModerator) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");
        requireNonNull(dto, "dto is null");

        if (!statistics.isUpdateable(isModerator)) {
            throw new AnnualStatisticsLockedException();
        }

        final Integer originalLandAreaSize = statistics.getOrCreateBasicInfo().getOperationalLandAreaSize();
        final RhyBasicInfo basicInfo = resolveBasicInfo(statistics, resolver);

        final String newIban = dto.getIban();

        if (!Objects.equals(newIban, basicInfo.getIbanAsFormattedString())) {

            if (!isModerator) {
                AnnualStatisticsModeratorFieldException.requestInvolvesMutationOnlyAllowedForModerator("iban");
            }

            basicInfo.setIbanAsFormattedString(newIban);
        }

        final Integer newLandAreaSize = dto.getOperationalLandAreaSize();
        final Integer refreshedLandAreaSize = basicInfo.getOperationalLandAreaSize();

        if (!Objects.equals(newLandAreaSize, refreshedLandAreaSize)) {

            if (!isModerator && !Objects.equals(newLandAreaSize, originalLandAreaSize)) {
                AnnualStatisticsModeratorFieldException.requestInvolvesMutationOnlyAllowedForModerator("operationalLandAreaSize");
            }

            final Integer landAreaSizeInHectares =
                    Optional.ofNullable(newLandAreaSize).orElseGet(resolver::getRhyLandAreaSizeInHectares);
            basicInfo.setOperationalLandAreaSize(landAreaSizeInHectares);
        }

        statistics.setBasicInfo(basicInfo);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamStatistics resolveHunterExamStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                            @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final HunterExamStatistics hunterExams = statistics.getOrCreateHunterExams();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(hunterExams, resolver) : hunterExams;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamStatistics refresh(@Nonnull final HunterExamStatistics statistics,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HunterExamStatistics copy = new HunterExamStatistics(statistics);
        copy.setHunterExamOfficials(resolver.getOccupationTypeCount(METSASTAJATUTKINNON_VASTAANOTTAJA));

        if (!copy.isHunterExamEventsManuallyOverridden()) {
            copy.setHunterExamEvents(resolver.getEventTypeCount(METSASTAJATUTKINTO));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHunterExams(@Nonnull final RhyAnnualStatistics statistics,
                                  @Nonnull final AnnualStatisticsResolver resolver,
                                  @Nonnull final HunterExamStatisticsDTO dto,
                                  final boolean isModerator) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");
        requireNonNull(dto, "dto is null");

        if (!statistics.isUpdateable(isModerator)) {
            throw new AnnualStatisticsLockedException();
        }

        final HunterExamStatistics refreshed = refresh(statistics.getOrCreateHunterExams(), resolver);

        final Integer overriddenHunterExamEvents = dto.getModeratorOverriddenHunterExamEvents();
        final boolean hunterExamEventsUpdatedManually =
                (overriddenHunterExamEvents != null || refreshed.isHunterExamEventsManuallyOverridden())
                        && !Objects.equals(overriddenHunterExamEvents, refreshed.getHunterExamEvents());

        if (!isModerator && hunterExamEventsUpdatedManually) {
            AnnualStatisticsModeratorFieldException
                    .requestInvolvesMutationOnlyAllowedForModerator("moderatorOverriddenHunterExamEvents");
        }

        final DateTime now = DateUtil.now();

        if (hunterExamEventsUpdatedManually) {
            refreshed.setHunterExamEventsWithModeratorOverride(overriddenHunterExamEvents, now);
        }

        final boolean anyFullyManualFieldUpdated =
                !Objects.equals(refreshed.getPassedHunterExams(), dto.getPassedHunterExams()) ||
                        !Objects.equals(refreshed.getFailedHunterExams(), dto.getFailedHunterExams());

        refreshed.setPassedHunterExams(dto.getPassedHunterExams());
        refreshed.setFailedHunterExams(dto.getFailedHunterExams());

        if (hunterExamEventsUpdatedManually || anyFullyManualFieldUpdated) {
            refreshed.setLastModified(now);
        }

        statistics.setHunterExams(refreshed);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public AnnualShootingTestStatistics resolveShootingTestStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                                      @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final AnnualShootingTestStatistics shootingTests = statistics.getOrCreateShootingTests();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(shootingTests, resolver) : shootingTests;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public AnnualShootingTestStatistics refresh(@Nonnull final AnnualShootingTestStatistics statistics,
                                                @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final AnnualShootingTestStatistics copy = new AnnualShootingTestStatistics(statistics);
        copy.setShootingTestOfficials(resolver.getOccupationTypeCount(AMPUMAKOKEEN_VASTAANOTTAJA));

        if (!copy.isFirearmTestEventsManuallyOverridden()) {
            copy.setFirearmTestEvents(resolver.getEventTypeCount(AMPUMAKOE));
        }
        if (!copy.isBowTestEventsManuallyOverridden()) {
            copy.setBowTestEvents(resolver.getEventTypeCount(JOUSIAMPUMAKOE));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateShootingTestStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                             @Nonnull final AnnualStatisticsResolver resolver,
                                             @Nonnull final AnnualShootingTestStatisticsDTO dto,
                                             @Nonnull final boolean isModerator) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");
        requireNonNull(dto, "dto is null");

        if (!statistics.isUpdateable(isModerator)) {
            throw new AnnualStatisticsLockedException();
        }

        final AnnualShootingTestStatistics refreshed = refresh(statistics.getOrCreateShootingTests(), resolver);

        final Integer overriddenFirearmTestEvents = dto.getModeratorOverriddenFirearmTestEvents();
        final Integer overriddenBowTestEvents = dto.getModeratorOverriddenBowTestEvents();

        final boolean firearmTestEventsUpdatedManually =
                (overriddenFirearmTestEvents != null || refreshed.isFirearmTestEventsManuallyOverridden())
                        && !Objects.equals(overriddenFirearmTestEvents, refreshed.getFirearmTestEvents());

        final boolean bowTestEventsUpdatedManually =
                (overriddenBowTestEvents != null || refreshed.isBowTestEventsManuallyOverridden())
                        && !Objects.equals(overriddenBowTestEvents, refreshed.getBowTestEvents());

        if (!isModerator) {
            if (firearmTestEventsUpdatedManually) {
                AnnualStatisticsModeratorFieldException
                        .requestInvolvesMutationOnlyAllowedForModerator("moderatorOverriddenFirearmTestEvents");
            }
            if (bowTestEventsUpdatedManually) {
                AnnualStatisticsModeratorFieldException
                        .requestInvolvesMutationOnlyAllowedForModerator("moderatorOverriddenBowTestEvents");
            }
        }

        final DateTime now = DateUtil.now();

        if (firearmTestEventsUpdatedManually) {
            refreshed.setFirearmTestEventsWithModeratorOverride(overriddenFirearmTestEvents, now);
        }
        if (bowTestEventsUpdatedManually) {
            refreshed.setBowTestEventsWithModeratorOverride(overriddenBowTestEvents, now);
        }

        final boolean anyFullyManualFieldChanged = !allFullyManualFieldsEqual(refreshed, dto);

        if (anyFullyManualFieldChanged) {
            copyFieldsToEntity(dto, refreshed);
        }

        if (firearmTestEventsUpdatedManually || bowTestEventsUpdatedManually || anyFullyManualFieldChanged) {
            refreshed.setLastModified(now);
        }

        statistics.setShootingTests(refreshed);
    }

    private static boolean allFullyManualFieldsEqual(final AnnualShootingTestStatistics entity,
                                                     final AnnualShootingTestStatisticsDTO dto) {

        return Objects.equals(entity.getAllMooseAttempts(), dto.getAllMooseAttempts())
                && Objects.equals(entity.getQualifiedMooseAttempts(), dto.getQualifiedMooseAttempts())
                && Objects.equals(entity.getAllBearAttempts(), dto.getAllBearAttempts())
                && Objects.equals(entity.getQualifiedBearAttempts(), dto.getQualifiedBearAttempts())
                && Objects.equals(entity.getAllRoeDeerAttempts(), dto.getAllRoeDeerAttempts())
                && Objects.equals(entity.getQualifiedRoeDeerAttempts(), dto.getQualifiedRoeDeerAttempts())
                && Objects.equals(entity.getAllBowAttempts(), dto.getAllBowAttempts())
                && Objects.equals(entity.getQualifiedBowAttempts(), dto.getQualifiedBowAttempts());
    }

    private static void copyFieldsToEntity(final AnnualShootingTestStatisticsDTO dto,
                                           final AnnualShootingTestStatistics entity) {

        entity.setAllMooseAttempts(dto.getAllMooseAttempts());
        entity.setQualifiedMooseAttempts(dto.getQualifiedMooseAttempts());
        entity.setAllBearAttempts(dto.getAllBearAttempts());
        entity.setQualifiedBearAttempts(dto.getQualifiedBearAttempts());
        entity.setAllRoeDeerAttempts(dto.getAllRoeDeerAttempts());
        entity.setQualifiedRoeDeerAttempts(dto.getQualifiedRoeDeerAttempts());
        entity.setAllBowAttempts(dto.getAllBowAttempts());
        entity.setQualifiedBowAttempts(dto.getQualifiedBowAttempts());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingControlStatistics resolveHuntingControlStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                                    @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final HuntingControlStatistics huntingControl = statistics.getOrCreateHuntingControl();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(huntingControl, resolver) : huntingControl;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingControlStatistics refresh(@Nonnull final HuntingControlStatistics statistics,
                                            @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HuntingControlStatistics copy = new HuntingControlStatistics(statistics);
        copy.setHuntingControllers(resolver.getOccupationTypeCount(METSASTYKSENVALVOJA));
        return copy;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDamageStatistics resolveGameDamageStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                            @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final GameDamageStatistics gameDamage = statistics.getOrCreateGameDamage();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(gameDamage, resolver) : gameDamage;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDamageStatistics refresh(@Nonnull final GameDamageStatistics statistics,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final GameDamageStatistics copy = new GameDamageStatistics(statistics);
        copy.setGameDamageInspectors(resolver.getOccupationTypeCount(RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA));
        return copy;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SrvaEventStatistics refreshSrvaEventStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                          @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        return statistics.canComputedPropertiesBeRefreshed()
                ? resolveSrvaEventStatistics(resolver)
                : statistics.getOrCreateSrva();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SrvaEventStatistics resolveSrvaEventStatistics(@Nonnull final AnnualStatisticsResolver resolver) {
        requireNonNull(resolver, "resolver is null");

        final SrvaEventStatistics stats = new SrvaEventStatistics(
                resolver.getSrvaEventCounts(ACCIDENT),
                resolver.getSrvaEventCounts(DEPORTATION),
                resolver.getSrvaEventCounts(INJURED_ANIMAL));

        stats.setTrafficAccidents(resolver.getSrvaAccidentCount(TRAFFIC_ACCIDENT));
        stats.setRailwayAccidents(resolver.getSrvaAccidentCount(RAILWAY_ACCIDENT));
        stats.setOtherAccidents(resolver.getSrvaAccidentCount(OTHER));

        final Tuple2<Integer, Integer> timeSpentAndPersonCount = resolver.getTimeSpentAndPersonCountFromSrvaEvents();
        stats.setTotalSrvaWorkHours(timeSpentAndPersonCount._1);
        stats.setSrvaParticipants(timeSpentAndPersonCount._2);
        return stats;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamTrainingStatistics resolveHunterExamTrainingStatistics(
            @Nonnull final RhyAnnualStatistics statistics, @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final HunterExamTrainingStatistics hunterExamTraining = statistics.getOrCreateHunterExamTraining();

        return statistics.canComputedPropertiesBeRefreshed()
                ? refresh(hunterExamTraining, resolver)
                : hunterExamTraining;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamTrainingStatistics refresh(@Nonnull final HunterExamTrainingStatistics statistics,
                                                @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HunterExamTrainingStatistics copy = new HunterExamTrainingStatistics(statistics);
        if (!copy.isHunterExamTrainingEventsManuallyOverridden()) {
            copy.setHunterExamTrainingEvents(resolver.getEventTypeCount(METSASTAJAKURSSI));
        }
        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHunterExamTrainingStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                   @Nonnull final AnnualStatisticsResolver resolver,
                                                   @Nonnull final HunterExamTrainingStatisticsDTO dto,
                                                   final boolean isModerator) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");
        requireNonNull(dto, "dto is null");

        if (!statistics.isUpdateable(isModerator)) {
            throw new AnnualStatisticsLockedException();
        }

        final HunterExamTrainingStatistics refreshed = refresh(statistics.getOrCreateHunterExamTraining(), resolver);

        final Integer overriddenHunterExamTrainingEvents = dto.getModeratorOverriddenHunterExamTrainingEvents();
        final boolean hunterExamTrainingEventsUpdatedManually =
                (overriddenHunterExamTrainingEvents != null || refreshed.isHunterExamTrainingEventsManuallyOverridden())
                        && !Objects.equals(overriddenHunterExamTrainingEvents, refreshed.getHunterExamTrainingEvents());

        if (!isModerator && hunterExamTrainingEventsUpdatedManually) {
            AnnualStatisticsModeratorFieldException
                    .requestInvolvesMutationOnlyAllowedForModerator("moderatorOverriddenHunterExamTrainingEvents");
        }

        final DateTime now = DateUtil.now();

        if (hunterExamTrainingEventsUpdatedManually) {
            refreshed.setHunterExamEventsWithModeratorOverride(overriddenHunterExamTrainingEvents, now);
        }

        final Integer newParticipantCount = dto.getHunterExamTrainingParticipants();
        final boolean participantsUpdated =
                !Objects.equals(refreshed.getHunterExamTrainingParticipants(), newParticipantCount);

        refreshed.setHunterExamTrainingParticipants(newParticipantCount);

        if (hunterExamTrainingEventsUpdatedManually || participantsUpdated) {
            refreshed.setLastModified(now);
        }

        statistics.setHunterExamTraining(refreshed);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public OtherHuntingRelatedStatistics resolveOtherHuntingRelatedStatistics(
            @Nonnull final RhyAnnualStatistics statistics, @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final OtherHuntingRelatedStatistics otherHuntingRelated = statistics.getOrCreateOtherHuntingRelated();

        return statistics.canComputedPropertiesBeRefreshed()
                ? refresh(otherHuntingRelated, resolver)
                : otherHuntingRelated;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public OtherHuntingRelatedStatistics refresh(@Nonnull final OtherHuntingRelatedStatistics statistics,
                                                 @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final OtherHuntingRelatedStatistics copy = new OtherHuntingRelatedStatistics(statistics);
        copy.setHarvestPermitApplicationPartners(resolver.getNumberOfHarvestPermitApplicationPartners());
        return copy;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public CommunicationStatistics resolveCommunicationStatistics(@Nonnull final RhyAnnualStatistics statistics,
                                                                  @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");

        final CommunicationStatistics communication = statistics.getOrCreateCommunication();

        return statistics.canComputedPropertiesBeRefreshed() ? refresh(communication, resolver) : communication;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public CommunicationStatistics refresh(@Nonnull final CommunicationStatistics statistics,
                                           @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final CommunicationStatistics copy = new CommunicationStatistics(statistics);
        copy.setOmariistaAnnouncements(resolver.getNumberOfAnnouncements());
        return copy;
    }
}
