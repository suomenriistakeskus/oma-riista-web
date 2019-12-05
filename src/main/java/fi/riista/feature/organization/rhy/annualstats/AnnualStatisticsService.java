package fi.riista.feature.organization.rhy.annualstats;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.audit.RhyAnnualStatisticsModeratorUpdateEvent;
import fi.riista.feature.organization.rhy.annualstats.audit.RhyAnnualStatisticsModeratorUpdateEventRepository;
import fi.riista.feature.organization.rhy.annualstats.statechange.RhyAnnualStatisticsStateTransitionService;
import fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageInspectionEventRepository;
import fi.riista.util.DateUtil;
import io.vavr.Tuple2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.DEPORTATION;
import static fi.riista.feature.gamediary.srva.SrvaEventNameEnum.INJURED_ANIMAL;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.OTHER;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.RAILWAY_ACCIDENT;
import static fi.riista.feature.gamediary.srva.SrvaEventTypeEnum.TRAFFIC_ACCIDENT;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.AMPUMAKOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.JALJESTAJAKOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.JOUSIAMPUMAKOE;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJAKOULUTUS_HIRVIELAIMET;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJAKOULUTUS_SUURPEDOT;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJAKURSSI;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTAJATUTKINTO;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTYKSENJOHTAJA_HIRVIELAIMET;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTYKSENJOHTAJA_SUURPEDOT;
import static fi.riista.feature.organization.calendar.CalendarEventType.METSASTYKSENVALVOJA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.MUU_RIISTANHOITOKOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.NUORISOTILAISUUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.OPPILAITOSTILAISUUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.PETOYHDYSHENKILO_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.PIENPETOJEN_PYYNTI_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTAKANTOJEN_HOITO_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTALASKENTA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.RIISTAVAHINKOTARKASTAJA_KOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.SRVAKOULUTUS;
import static fi.riista.feature.organization.calendar.CalendarEventType.TILAISUUS_KOULUILLE;
import static fi.riista.feature.organization.calendar.CalendarEventType.VAHINKOKOULUTUS;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.NOT_STARTED;
import static fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageType.LARGE_CARNIVORE;
import static fi.riista.feature.organization.rhy.gamedamageinspection.GameDamageType.MOOSELIKE;
import static fi.riista.feature.shootingtest.ShootingTestType.ROE_DEER;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.BOW;
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
    private GameDamageInspectionEventRepository gameDamageInspectionEventRepository;

    @Resource
    private RhyAnnualStatisticsModeratorUpdateEventRepository moderatorUpdateEventRepository;

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private RhyAnnualStatisticsStateTransitionService stateTransitionService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private EnumLocaliser localiser;

    public AnnualStatisticsResolver getAnnualStatisticsResolver(@Nonnull final Riistanhoitoyhdistys rhy,
                                                                final int calendarYear) {

        return new AnnualStatisticsResolver(
                rhy, calendarYear, occupationRepository, eventRepository, gameDamageInspectionEventRepository,
                jpaQueryFactory, sqlQueryFactory);
    }

    public AnnualStatisticsResolver getAnnualStatisticsResolver(@Nonnull final RhyAnnualStatistics entity) {
        return getAnnualStatisticsResolver(entity.getRhy(), entity.getYear());
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void refresh(@Nonnull final RhyAnnualStatistics statistics) {
        requireNonNull(statistics);

        if (isRefreshable(statistics)) {
            final AnnualStatisticsResolver resolver = getAnnualStatisticsResolver(statistics);

            // SRVA must be updated first because it might be null (as JPA-embeddable)
            statistics.setSrva(resolveSrvaEventStatistics(resolver));

            final RhyBasicInfo basicInfo = refresh(statistics.getOrCreateBasicInfo(), resolver);
            final HunterExamStatistics hunterExams = refresh(statistics.getOrCreateHunterExams(), resolver);
            final AnnualShootingTestStatistics shootingTests = refresh(statistics.getOrCreateShootingTests(), resolver);
            final GameDamageStatistics gameDamage = refresh(statistics.getOrCreateGameDamage(), resolver);
            final HuntingControlStatistics huntingControl = refresh(statistics.getOrCreateHuntingControl(), resolver);
            final HunterExamTrainingStatistics hunterExamTraining =
                    refresh(statistics.getOrCreateHunterExamTraining(), resolver);
            final OtherHuntingRelatedStatistics otherHuntingRelated =
                    refresh(statistics.getOrCreateOtherHuntingRelated(), resolver);
            final CommunicationStatistics communication = refresh(statistics.getOrCreateCommunication(), resolver);
            final HunterTrainingStatistics hunterTraining = refresh(statistics.getOrCreateHunterTraining(), resolver);
            final YouthTrainingStatistics youthTraining = refresh(statistics.getOrCreateYouthTraining(), resolver);
            final JHTTrainingStatistics jhtTraining = refresh(statistics.getOrCreateJhtTraining(), resolver);
            final OtherHunterTrainingStatistics otherHunterTraining =
                    refresh(statistics.getOrCreateOtherHunterTraining(), resolver);

            statistics.setBasicInfo(basicInfo);
            statistics.setHunterExams(hunterExams);
            statistics.setShootingTests(shootingTests);
            statistics.setGameDamage(gameDamage);
            statistics.setHuntingControl(huntingControl);
            statistics.setHunterExamTraining(hunterExamTraining);
            statistics.setOtherHuntingRelated(otherHuntingRelated);
            statistics.setCommunication(communication);
            statistics.setHunterTraining(hunterTraining);
            statistics.setYouthTraining(youthTraining);
            statistics.setJhtTraining(jhtTraining);
            statistics.setOtherHunterTraining(otherHunterTraining);

            statisticsRepository.saveAndFlush(statistics);
        }
    }

    private static RhyBasicInfo refresh(@Nonnull final RhyBasicInfo basicInfo,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(basicInfo, "basicInfo is null");
        requireNonNull(resolver, "resolver is null");

        final RhyBasicInfo copy = basicInfo.makeCopy();
        final boolean yearChanged = DateUtil.today().getYear() != resolver.getCalendarYear();

        if (basicInfo.getIban() == null) {
            Optional.ofNullable(resolver.getIbanFromPreviousYear()).ifPresent(copy::setIban);
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
    public void updateBasicInfo(@Nonnull final RhyAnnualStatistics statistics, @Nonnull final RhyBasicInfoDTO dto) {
        requireNonNull(statistics, "statistics is null");
        requireNonNull(dto, "dto is null");

        final RhyBasicInfo original = statistics.getOrCreateBasicInfo();

        final RhyBasicInfo updated = original.makeCopy();
        updated.setIbanAsFormattedString(dto.getIban());
        updated.setOperationalLandAreaSize(dto.getOperationalLandAreaSize());

        updateGroup(statistics, original, updated);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamStatistics refresh(@Nonnull final HunterExamStatistics statistics,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HunterExamStatistics copy = statistics.makeCopy();
        copy.setHunterExamOfficials(resolver.getOccupationTypeCount(METSASTAJATUTKINNON_VASTAANOTTAJA));

        if (!copy.isHunterExamEventsManuallyOverridden()) {
            copy.setHunterExamEvents(resolver.getEventTypeCount(METSASTAJATUTKINTO));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHunterExams(@Nonnull final RhyAnnualStatistics statistics,
                                  @Nonnull final HunterExamStatistics group) {

        updateGroup(statistics, statistics.getOrCreateHunterExams(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void moderatorUpdateHunterExams(@Nonnull final RhyAnnualStatistics statistics,
                                           @Nonnull final HunterExamStatisticsDTO dto) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(dto, "dto is null");

        // isModerator = true
        statistics.assertIsUpdateable(true, localiser);

        final HunterExamStatistics original = statistics.getOrCreateHunterExams();

        final HunterExamStatistics updated = original.makeCopy();
        updated.setPassedHunterExams(dto.getPassedHunterExams());
        updated.setFailedHunterExams(dto.getFailedHunterExams());

        final boolean changed = original.merge(updated);

        final Integer overriddenEvents = dto.getModeratorOverriddenHunterExamEvents();
        final boolean overriddenEventsUpdated =
                overriddenEvents != null && !Objects.equals(overriddenEvents, original.getHunterExamEvents());

        if (changed || overriddenEventsUpdated) {
            if (overriddenEventsUpdated) {
                original.setHunterExamEventsOverridden(overriddenEvents);
            }
            addModeratorUpdateEvent(statistics, original);
        }

        refresh(statistics);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public AnnualShootingTestStatistics refresh(@Nonnull final AnnualShootingTestStatistics statistics,
                                                @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final AnnualShootingTestStatistics copy = statistics.makeCopy();
        copy.setShootingTestOfficials(resolver.getOccupationTypeCount(AMPUMAKOKEEN_VASTAANOTTAJA));

        if (!copy.isFirearmTestEventsManuallyOverridden()) {
            copy.setFirearmTestEvents(resolver.getEventTypeCount(AMPUMAKOE));
        }
        if (!copy.isBowTestEventsManuallyOverridden()) {
            copy.setBowTestEvents(resolver.getEventTypeCount(JOUSIAMPUMAKOE));
        }

        copy.setAllRoeDeerAttempts(resolver.getShootingTestTotalCount(ROE_DEER));
        copy.setQualifiedRoeDeerAttempts(resolver.getShootingTestQualifiedCount(ROE_DEER));

        copy.setAllMooseAttempts(resolver.getShootingTestTotalCount(MOOSE));
        copy.setQualifiedMooseAttempts(resolver.getShootingTestQualifiedCount(MOOSE));

        copy.setAllBearAttempts(resolver.getShootingTestTotalCount(BEAR));
        copy.setQualifiedBearAttempts(resolver.getShootingTestQualifiedCount(BEAR));

        copy.setAllBowAttempts(resolver.getShootingTestTotalCount(BOW));
        copy.setQualifiedBowAttempts(resolver.getShootingTestQualifiedCount(BOW));

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void moderatorUpdateShootingTests(@Nonnull final RhyAnnualStatistics statistics,
                                             @Nonnull final AnnualShootingTestStatisticsDTO dto) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(dto, "dto is null");

        // isModerator = true
        statistics.assertIsUpdateable(true, localiser);

        final AnnualShootingTestStatistics original = statistics.getOrCreateShootingTests();

        final AnnualShootingTestStatistics updated = original.makeCopy();
        updated.setTestEventsOverridden(dto.getModeratorOverriddenFirearmTestEvents(), dto.getModeratorOverriddenBowTestEvents());

        final boolean changed = original.merge(updated);

        if (changed) {
            addModeratorUpdateEvent(statistics, original);
        }

        refresh(statistics);
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

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHuntingControl(@Nonnull final RhyAnnualStatistics statistics,
                                     @Nonnull final HuntingControlStatistics group) {

        updateGroup(statistics, statistics.getOrCreateHuntingControl(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDamageStatistics refresh(@Nonnull final GameDamageStatistics statistics,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final GameDamageStatistics copy = new GameDamageStatistics(statistics);
        copy.setGameDamageInspectors(resolver.getOccupationTypeCount(RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA));

        if (!copy.isMooselikeDamageInspectionLocationsOverridden()) {
            copy.setMooselikeDamageInspectionLocations(resolver.getGameDamageInspectionEventCount(MOOSELIKE));
        }
        if (!copy.isLargeCarnivoreDamageInspectionLocationsOverridden()) {
            copy.setLargeCarnivoreDamageInspectionLocations(resolver.getGameDamageInspectionEventCount(LARGE_CARNIVORE));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateGameDamage(@Nonnull final RhyAnnualStatistics statistics,
                                 @Nonnull final GameDamageStatistics group) {

        final GameDamageStatistics original = statistics.getOrCreateGameDamage();

        final boolean originalMooselikeOverridden = original.isMooselikeDamageInspectionLocationsOverridden();
        final boolean mooselikeDamageInspectionLocationsOverridden = originalMooselikeOverridden ||
                !Objects.equals(original.getMooselikeDamageInspectionLocations(), group.getMooselikeDamageInspectionLocations());
        group.setMooselikeDamageInspectionLocationsOverridden(mooselikeDamageInspectionLocationsOverridden);

        final boolean originalLargeCarnivoreOverridden = original.isLargeCarnivoreDamageInspectionLocationsOverridden();
        final boolean largeCarnivoreDamageInspectionLocationsOverridden = originalLargeCarnivoreOverridden ||
                !Objects.equals(original.getLargeCarnivoreDamageInspectionLocations(), group.getLargeCarnivoreDamageInspectionLocations());
        group.setLargeCarnivoreDamageInspectionLocationsOverridden(largeCarnivoreDamageInspectionLocationsOverridden);

        updateGroup(statistics, statistics.getOrCreateGameDamage(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateOtherPublicAdmin(@Nonnull final RhyAnnualStatistics statistics,
                                       @Nonnull final OtherPublicAdminStatistics group) {

        updateGroup(statistics, statistics.getOrCreateOtherPublicAdmin(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public SrvaEventStatistics resolveSrvaEventStatistics(@Nonnull final AnnualStatisticsResolver resolver) {
        requireNonNull(resolver);

        final SrvaEventStatistics stats = new SrvaEventStatistics(
                resolver.getSrvaEventCounts(ACCIDENT),
                resolver.getSrvaEventCounts(DEPORTATION),
                resolver.getSrvaEventCounts(INJURED_ANIMAL));

        stats.setTrafficAccidents(resolver.getSrvaAccidentCount(TRAFFIC_ACCIDENT));
        stats.setRailwayAccidents(resolver.getSrvaAccidentCount(RAILWAY_ACCIDENT));
        stats.setOtherAccidents(resolver.getSrvaAccidentCount(OTHER));

        final Tuple2<Integer, Integer> timeSpentAndPersonCount = resolver.getTimeSpentAndPersonCountFromSrvaEvents();
        stats.setTotalSrvaWorkHours(Optional.ofNullable(timeSpentAndPersonCount._1).orElse(0));
        stats.setSrvaParticipants(Optional.ofNullable(timeSpentAndPersonCount._2).orElse(0));
        return stats;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterExamTrainingStatistics refresh(@Nonnull final HunterExamTrainingStatistics statistics,
                                                @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HunterExamTrainingStatistics copy = statistics.makeCopy();
        if (!copy.isHunterExamTrainingEventsManuallyOverridden()) {
            copy.setHunterExamTrainingEvents(resolver.getEventTypeCount(METSASTAJAKURSSI));
        }
        if (!copy.isHunterExamTraininingParticipantsOverridden()) {
            copy.setHunterExamTrainingParticipants(resolver.getEventParticipantsCount(METSASTAJAKURSSI));
        }
        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHunterExamTraining(@Nonnull final RhyAnnualStatistics statistics,
                                         @Nonnull final HunterExamTrainingStatistics group) {

        final HunterExamTrainingStatistics original = statistics.getOrCreateHunterExamTraining();

        final boolean originalOverridden = original.isHunterExamTraininingParticipantsOverridden();
        final boolean hunterExamTrainingParticipantsOverridden = originalOverridden ||
                !Objects.equals(original.getHunterExamTrainingParticipants(), group.getHunterExamTrainingParticipants());
        group.setHunterExamTrainingParticipantsOverridden(hunterExamTrainingParticipantsOverridden);

        updateGroup(statistics, statistics.getOrCreateHunterExamTraining(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void moderatorUpdateHunterExamTraining(@Nonnull final RhyAnnualStatistics statistics,
                                                  @Nonnull final HunterExamTrainingStatisticsDTO dto) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(dto, "dto is null");

        // isModerator = true
        statistics.assertIsUpdateable(true, localiser);

        final HunterExamTrainingStatistics original = statistics.getOrCreateHunterExamTraining();

        final HunterExamTrainingStatistics updated = original.makeCopy();
        updated.setHunterExamTrainingParticipants(dto.getHunterExamTrainingParticipants());

        final boolean originalOverridden = original.isHunterExamTraininingParticipantsOverridden();
        final boolean hunterExamTrainingParticipantsOverridden = originalOverridden ||
                !Objects.equals(original.getHunterExamTrainingParticipants(), updated.getHunterExamTrainingParticipants());

        final boolean changed = original.merge(updated);

        final Integer overriddenTrainingEvents = dto.getModeratorOverriddenHunterExamTrainingEvents();
        final boolean overriddenTrainingEventsUpdated = overriddenTrainingEvents != null
                && !Objects.equals(overriddenTrainingEvents, original.getHunterExamTrainingEvents());

        if (changed || overriddenTrainingEventsUpdated || hunterExamTrainingParticipantsOverridden) {
            if (overriddenTrainingEventsUpdated) {
                original.setHunterExamTrainingEventsOverridden(overriddenTrainingEvents);
            }
            if (hunterExamTrainingParticipantsOverridden) {
                original.setHunterExamTrainingParticipantsOverridden(hunterExamTrainingParticipantsOverridden);
            }
            addModeratorUpdateEvent(statistics, original);
        }

        refresh(statistics);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateJhtTraining(@Nonnull final RhyAnnualStatistics statistics,
                                  @Nonnull final JHTTrainingStatistics group) {

        final JHTTrainingStatistics original = statistics.getOrCreateJhtTraining();

        // Shooting test trainings
        final boolean shootingTestTrainingEventsOverridden = original.isShootingTestTrainingEventsOverridden();
        group.setShootingTestTrainingEventsOverridden(shootingTestTrainingEventsOverridden ||
                !Objects.equals(original.getShootingTestTrainingEvents(), group.getShootingTestTrainingEvents()));
        final boolean shootingTestTrainingParticipantsOverridden = original.isShootingTestTrainingParticipantsOverridden();
        group.setShootingTestTrainingParticipantsOverridden(shootingTestTrainingParticipantsOverridden ||
                !Objects.equals(original.getShootingTestTrainingParticipants(), group.getShootingTestTrainingParticipants()));

        // Hunter exam official trainings
        final boolean hunterExamOfficialTrainingEventsOverridden = original.isHunterExamOfficialTrainingEventsOverridden();
        group.setHunterExamOfficialTrainingEventsOverridden(hunterExamOfficialTrainingEventsOverridden ||
                !Objects.equals(original.getHunterExamOfficialTrainingEvents(), group.getHunterExamOfficialTrainingEvents()));
        final boolean hunterExamOfficialTrainingParticipantsOverridden = original.isHunterExamOfficialTrainingParticipantsOverridden();
        group.setHunterExamOfficialTrainingParticipantsOverridden(hunterExamOfficialTrainingParticipantsOverridden ||
                !Objects.equals(original.getHunterExamOfficialTrainingParticipants(), group.getHunterExamOfficialTrainingParticipants()));

        // Game damage trainings
        final boolean gameDamageTrainingEventsOverridden = original.isGameDamageTrainingEventsOverridden();
        group.setGameDamageTrainingEventsOverridden(gameDamageTrainingEventsOverridden ||
                !Objects.equals(original.getGameDamageTrainingEvents(), group.getGameDamageTrainingEvents()));
        final boolean gameDamageTrainingParticipantsOverridden = original.isGameDamageTrainingParticipantsOverridden();
        group.setGameDamageTrainingParticipantsOverridden(gameDamageTrainingParticipantsOverridden ||
                !Objects.equals(original.getGameDamageTrainingParticipants(), group.getGameDamageTrainingParticipants()));

        // Hunting control trainings
        final boolean huntingControlTrainingEventsOverridden = original.isHuntingControlTrainingEventsOverridden();
        group.setHuntingControlTrainingEventsOverridden(huntingControlTrainingEventsOverridden ||
                !Objects.equals(original.getHuntingControlTrainingEvents(), group.getHuntingControlTrainingEvents()));
        final boolean huntingControlTrainingParticipantsOverridden = original.isHuntingControlTrainingParticipantsOverridden();
        group.setHuntingControlTrainingParticipantsOverridden(huntingControlTrainingParticipantsOverridden ||
                !Objects.equals(original.getHuntingControlTrainingParticipants(), group.getHuntingControlTrainingParticipants()));

        updateGroup(statistics, statistics.getOrCreateJhtTraining(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public JHTTrainingStatistics refresh(@Nonnull final JHTTrainingStatistics statistics,
                                           @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final JHTTrainingStatistics copy = new JHTTrainingStatistics(statistics);

        // Shooting test trainings
        if (!copy.isShootingTestTrainingEventsOverridden()) {
            copy.setShootingTestTrainingEvents(resolver.getEventTypeCount(AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS));
        }
        if (!copy.isShootingTestTrainingParticipantsOverridden()) {
            copy.setShootingTestTrainingParticipants(resolver.getEventParticipantsCount(AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS));
        }

        // Hunter exam official trainings
        if (!copy.isHunterExamOfficialTrainingEventsOverridden()) {
            copy.setHunterExamOfficialTrainingEvents(resolver.getEventTypeCount(METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS));
        }
        if (!copy.isHunterExamOfficialTrainingParticipantsOverridden()) {
            copy.setHunterExamOfficialTrainingParticipants(resolver.getEventParticipantsCount(METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS));
        }

        // Game damage trainings
        if (!copy.isGameDamageTrainingEventsOverridden()) {
            copy.setGameDamageTrainingEvents(resolver.getEventTypeCount(RIISTAVAHINKOTARKASTAJA_KOULUTUS));
        }
        if (!copy.isGameDamageTrainingParticipantsOverridden()) {
            copy.setGameDamageTrainingParticipants(resolver.getEventParticipantsCount(RIISTAVAHINKOTARKASTAJA_KOULUTUS));
        }

        // Hunting control trainings
        if (!copy.isHuntingControlTrainingEventsOverridden()) {
            copy.setHuntingControlTrainingEvents(resolver.getEventTypeCount(METSASTYKSENVALVOJA_KOULUTUS));
        }
        if (!copy.isHuntingControlTrainingParticipantsOverridden()) {
            copy.setHuntingControlTrainingParticipants(resolver.getEventParticipantsCount(METSASTYKSENVALVOJA_KOULUTUS));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateHunterTraining(@Nonnull final RhyAnnualStatistics statistics,
                                     @Nonnull final HunterTrainingStatistics group) {

        final HunterTrainingStatistics original = statistics.getOrCreateHunterTraining();

        // Mooselike hunting trainings
        final boolean mooselikeHuntingTrainingEventsOverridden = original.isMooselikeHuntingTrainingEventsOverridden();
        group.setMooselikeHuntingTrainingEventsOverridden(mooselikeHuntingTrainingEventsOverridden ||
                !Objects.equals(original.getMooselikeHuntingTrainingEvents(), group.getMooselikeHuntingTrainingEvents()));
        final boolean mooselikeHuntingTrainingParticipantsOverridden = original.isMooselikeHuntingTrainingParticipantsOverridden();
        group.setMooselikeHuntingTrainingParticipantsOverridden(mooselikeHuntingTrainingParticipantsOverridden ||
                !Objects.equals(original.getMooselikeHuntingTrainingParticipants(), group.getMooselikeHuntingTrainingParticipants()));

        // Mooselike hunting leader trainings
        final boolean mooselikeHuntingLeaderTrainingEventsOverridden = original.isMooselikeHuntingLeaderTrainingEventsOverridden();
        group.setMooselikeHuntingLeaderTrainingEventsOverridden(mooselikeHuntingLeaderTrainingEventsOverridden ||
                !Objects.equals(original.getMooselikeHuntingLeaderTrainingEvents(), group.getMooselikeHuntingLeaderTrainingEvents()));
        final boolean mooselikeHuntingLeaderTrainingParticipantsOverridden = original.isMooselikeHuntingLeaderTrainingParticipantsOverridden();
        group.setMooselikeHuntingLeaderTrainingParticipantsOverridden(mooselikeHuntingLeaderTrainingParticipantsOverridden ||
                !Objects.equals(original.getMooselikeHuntingLeaderTrainingParticipants(), group.getMooselikeHuntingLeaderTrainingParticipants()));

        // Carnivore hunting trainings
        final boolean carnivoreHuntingTrainingEventsOverridden = original.isCarnivoreHuntingTrainingEventsOverridden();
        group.setCarnivoreHuntingTrainingEventsOverridden(carnivoreHuntingTrainingEventsOverridden ||
                !Objects.equals(original.getCarnivoreHuntingTrainingEvents(), group.getCarnivoreHuntingTrainingEvents()));
        final boolean carnivoreHuntingTrainingParticipantsOverridden = original.isCarnivoreHuntingTrainingParticipantsOverridden();
        group.setCarnivoreHuntingTrainingParticipantsOverridden(carnivoreHuntingTrainingParticipantsOverridden ||
                !Objects.equals(original.getCarnivoreHuntingTrainingParticipants(), group.getCarnivoreHuntingTrainingParticipants()));

        // Carnivore hunting leader trainings
        final boolean carnivoreHuntingLeaderTrainingEventsOverridden = original.isCarnivoreHuntingLeaderTrainingEventsOverridden();
        group.setCarnivoreHuntingLeaderTrainingEventsOverridden(carnivoreHuntingLeaderTrainingEventsOverridden ||
                !Objects.equals(original.getCarnivoreHuntingLeaderTrainingEvents(), group.getCarnivoreHuntingLeaderTrainingEvents()));
        final boolean carnivoreHuntingLeaderTrainingParticipantsOverridden = original.isCarnivoreHuntingLeaderTrainingParticipantsOverridden();
        group.setCarnivoreHuntingLeaderTrainingParticipantsOverridden(carnivoreHuntingLeaderTrainingParticipantsOverridden ||
                !Objects.equals(original.getCarnivoreHuntingLeaderTrainingParticipants(), group.getCarnivoreHuntingLeaderTrainingParticipants()));

        // Srva trainings
        final boolean srvaTrainingEventsOverridden = original.isSrvaTrainingEventsOverridden();
        group.setSrvaTrainingEventsOverridden(srvaTrainingEventsOverridden ||
                !Objects.equals(original.getSrvaTrainingEvents(), group.getSrvaTrainingEvents()));
        final boolean srvaTrainingParticipantsOverridden = original.isSrvaTrainingParticipantsOverridden();
        group.setSrvaTrainingParticipantsOverridden(srvaTrainingParticipantsOverridden ||
                !Objects.equals(original.getSrvaTrainingParticipants(), group.getSrvaTrainingParticipants()));

        // Carnivore contact person trainings
        final boolean carnivoreContactPersonTrainingEventsOverridden = original.isCarnivoreContactPersonTrainingEventsOverridden();
        group.setCarnivoreContactPersonTrainingEventsOverridden(carnivoreContactPersonTrainingEventsOverridden ||
                !Objects.equals(original.getCarnivoreContactPersonTrainingEvents(), group.getCarnivoreContactPersonTrainingEvents()));
        final boolean carnivoreContactPersonTrainingParticipantsOverridden = original.isCarnivoreContactPersonTrainingParticipantsOverridden();
        group.setCarnivoreContactPersonTrainingParticipantsOverridden(carnivoreContactPersonTrainingParticipantsOverridden ||
                !Objects.equals(original.getCarnivoreContactPersonTrainingParticipants(), group.getCarnivoreContactPersonTrainingParticipants()));

        // Accident prevention trainings
        final boolean accidentPreventionTrainingEventsOverridden = original.isAccidentPreventionTrainingEventsOverridden();
        group.setAccidentPreventionTrainingEventsOverridden(accidentPreventionTrainingEventsOverridden ||
                !Objects.equals(original.getAccidentPreventionTrainingEvents(), group.getAccidentPreventionTrainingEvents()));
        final boolean accidentPreventionTrainingParticipantsOverridden = original.isAccidentPreventionTrainingParticipantsOverridden();
        group.setAccidentPreventionTrainingParticipantsOverridden(accidentPreventionTrainingParticipantsOverridden ||
                !Objects.equals(original.getAccidentPreventionTrainingParticipants(), group.getAccidentPreventionTrainingParticipants()));

        updateGroup(statistics, statistics.getOrCreateHunterTraining(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HunterTrainingStatistics refresh(@Nonnull final HunterTrainingStatistics statistics,
                                        @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final HunterTrainingStatistics copy = new HunterTrainingStatistics(statistics);

        // Mooselike hunting trainings
        if (!copy.isMooselikeHuntingTrainingEventsOverridden()) {
            copy.setMooselikeHuntingTrainingEvents(resolver.getEventTypeCount(METSASTAJAKOULUTUS_HIRVIELAIMET));
        }
        if (!copy.isMooselikeHuntingTrainingParticipantsOverridden()) {
            copy.setMooselikeHuntingTrainingParticipants(resolver.getEventParticipantsCount(METSASTAJAKOULUTUS_HIRVIELAIMET));
        }

        // Mooselike hunting leader trainings
        if (!copy.isMooselikeHuntingLeaderTrainingEventsOverridden()) {
            copy.setMooselikeHuntingLeaderTrainingEvents(resolver.getEventTypeCount(METSASTYKSENJOHTAJA_HIRVIELAIMET));
        }
        if (!copy.isMooselikeHuntingLeaderTrainingParticipantsOverridden()) {
            copy.setMooselikeHuntingLeaderTrainingParticipants(resolver.getEventParticipantsCount(METSASTYKSENJOHTAJA_HIRVIELAIMET));
        }

        // Carnivore hunting trainings
        if (!copy.isCarnivoreHuntingTrainingEventsOverridden()) {
            copy.setCarnivoreHuntingTrainingEvents(resolver.getEventTypeCount(METSASTAJAKOULUTUS_SUURPEDOT));
        }
        if (!copy.isCarnivoreHuntingTrainingParticipantsOverridden()) {
            copy.setCarnivoreHuntingTrainingParticipants(resolver.getEventParticipantsCount(METSASTAJAKOULUTUS_SUURPEDOT));
        }

        // Carnivore hunting leader trainings
        if (!copy.isCarnivoreHuntingLeaderTrainingEventsOverridden()) {
            copy.setCarnivoreHuntingLeaderTrainingEvents(resolver.getEventTypeCount(METSASTYKSENJOHTAJA_SUURPEDOT));
        }
        if (!copy.isCarnivoreHuntingLeaderTrainingParticipantsOverridden()) {
            copy.setCarnivoreHuntingLeaderTrainingParticipants(resolver.getEventParticipantsCount(METSASTYKSENJOHTAJA_SUURPEDOT));
        }

        // Srva trainings
        if (!copy.isSrvaTrainingEventsOverridden()) {
            copy.setSrvaTrainingEvents(resolver.getEventTypeCount(SRVAKOULUTUS));
        }
        if (!copy.isSrvaTrainingParticipantsOverridden()) {
            copy.setSrvaTrainingParticipants(resolver.getEventParticipantsCount(SRVAKOULUTUS));
        }

        // Carnivore contact person trainings
        if (!copy.isCarnivoreContactPersonTrainingEventsOverridden()) {
            copy.setCarnivoreContactPersonTrainingEvents(resolver.getEventTypeCount(PETOYHDYSHENKILO_KOULUTUS));
        }
        if (!copy.isCarnivoreContactPersonTrainingParticipantsOverridden()) {
            copy.setCarnivoreContactPersonTrainingParticipants(resolver.getEventParticipantsCount(PETOYHDYSHENKILO_KOULUTUS));
        }

        // Accident prevention trainings
        if (!copy.isAccidentPreventionTrainingEventsOverridden()) {
            copy.setAccidentPreventionTrainingEvents(resolver.getEventTypeCount(VAHINKOKOULUTUS));
        }
        if (!copy.isAccidentPreventionTrainingParticipantsOverridden()) {
            copy.setAccidentPreventionTrainingParticipants(resolver.getEventParticipantsCount(VAHINKOKOULUTUS));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateYouthTraining(@Nonnull final RhyAnnualStatistics statistics,
                                     @Nonnull final YouthTrainingStatistics group) {

        final YouthTrainingStatistics original = statistics.getOrCreateYouthTraining();

        //  School trainings
        final boolean schoolTrainingEventsOverridden = original.isSchoolTrainingEventsOverridden();
        group.setSchoolTrainingEventsOverridden(schoolTrainingEventsOverridden ||
                !Objects.equals(original.getSchoolTrainingEvents(), group.getSchoolTrainingEvents()));
        final boolean schoolTrainingParticipantsOverridden = original.isSchoolTrainingParticipantsOverridden();
        group.setSchoolTrainingParticipantsOverridden(schoolTrainingParticipantsOverridden ||
                !Objects.equals(original.getSchoolTrainingParticipants(), group.getSchoolTrainingParticipants()));

        //  College trainings
        final boolean collegeTrainingEventsOverridden = original.isCollegeTrainingEventsOverridden();
        group.setCollegeTrainingEventsOverridden(collegeTrainingEventsOverridden ||
                !Objects.equals(original.getCollegeTrainingEvents(), group.getCollegeTrainingEvents()));
        final boolean collegeTrainingParticipantsOverridden = original.isCollegeTrainingParticipantsOverridden();
        group.setCollegeTrainingParticipantsOverridden(collegeTrainingParticipantsOverridden ||
                !Objects.equals(original.getCollegeTrainingParticipants(), group.getCollegeTrainingParticipants()));

        //  Other youth trainings
        final boolean otherYouthTargetedTrainingEventsOverridden = original.isOtherYouthTargetedTrainingEventsOverridden();
        group.setOtherYouthTargetedTrainingEventsOverridden(otherYouthTargetedTrainingEventsOverridden ||
                !Objects.equals(original.getOtherYouthTargetedTrainingEvents(), group.getOtherYouthTargetedTrainingEvents()));
        final boolean otherYouthTargetedTrainingParticipantsOverridden = original.isOtherYouthTargetedTrainingParticipantsOverridden();
        group.setOtherYouthTargetedTrainingParticipantsOverridden(otherYouthTargetedTrainingParticipantsOverridden ||
                !Objects.equals(original.getOtherYouthTargetedTrainingParticipants(), group.getOtherYouthTargetedTrainingParticipants()));

        updateGroup(statistics, statistics.getOrCreateYouthTraining(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public YouthTrainingStatistics refresh(@Nonnull final YouthTrainingStatistics statistics,
                                            @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final YouthTrainingStatistics copy = new YouthTrainingStatistics(statistics);

        //  School trainings
        if (!copy.isSchoolTrainingEventsOverridden()) {
            copy.setSchoolTrainingEvents(resolver.getEventTypeCount(TILAISUUS_KOULUILLE));
        }
        if (!copy.isSchoolTrainingParticipantsOverridden()) {
            copy.setSchoolTrainingParticipants(resolver.getEventParticipantsCount(TILAISUUS_KOULUILLE));
        }

        //  College trainings
        if (!copy.isCollegeTrainingEventsOverridden()) {
            copy.setCollegeTrainingEvents(resolver.getEventTypeCount(OPPILAITOSTILAISUUS));
        }
        if (!copy.isSchoolTrainingParticipantsOverridden()) {
            copy.setCollegeTrainingParticipants(resolver.getEventParticipantsCount(OPPILAITOSTILAISUUS));
        }

        //  Other youth trainings
        if (!copy.isOtherYouthTargetedTrainingEventsOverridden()) {
            copy.setOtherYouthTargetedTrainingEvents(resolver.getEventTypeCount(NUORISOTILAISUUS));
        }
        if (!copy.isOtherYouthTargetedTrainingParticipantsOverridden()) {
            copy.setOtherYouthTargetedTrainingParticipants(resolver.getEventParticipantsCount(NUORISOTILAISUUS));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateOtherHunterTraining(@Nonnull final RhyAnnualStatistics statistics,
                                          @Nonnull final OtherHunterTrainingStatistics group) {

        final OtherHunterTrainingStatistics original = statistics.getOrCreateOtherHunterTraining();

        //  Small carnivore hunting trainings
        final boolean smallCarnivoreHuntingTrainingEventsOverridden = original.isSmallCarnivoreHuntingTrainingEventsOverridden();
        group.setSmallCarnivoreHuntingTrainingEventsOverridden(smallCarnivoreHuntingTrainingEventsOverridden ||
                !Objects.equals(original.getSmallCarnivoreHuntingTrainingEvents(), group.getSmallCarnivoreHuntingTrainingEvents()));
        final boolean smallCarnivoreHuntingTrainingParticipantsOverridden = original.isSmallCarnivoreHuntingTrainingParticipantsOverridden();
        group.setSmallCarnivoreHuntingTrainingParticipantsOverridden(smallCarnivoreHuntingTrainingParticipantsOverridden ||
                !Objects.equals(original.getSmallCarnivoreHuntingTrainingParticipants(), group.getSmallCarnivoreHuntingTrainingParticipants()));

        //  Game counting trainings
        final boolean gameCountingTrainingEventsOverridden = original.isGameCountingTrainingEventsOverridden();
        group.setGameCountingTrainingEventsOverridden(gameCountingTrainingEventsOverridden ||
                !Objects.equals(original.getGameCountingTrainingEvents(), group.getGameCountingTrainingEvents()));
        final boolean gameCountingTrainingParticipantsOverridden = original.isGameCountingTrainingParticipantsOverridden();
        group.setGameCountingTrainingParticipantsOverridden(gameCountingTrainingParticipantsOverridden ||
                !Objects.equals(original.getGameCountingTrainingParticipants(), group.getGameCountingTrainingParticipants()));

        //  Game population management trainings
        final boolean gamePopulationManagementTrainingEventsOverridden = original.isGamePopulationManagementTrainingEventsOverridden();
        group.setGamePopulationManagementTrainingEventsOverridden(gamePopulationManagementTrainingEventsOverridden ||
                !Objects.equals(original.getGamePopulationManagementTrainingEvents(), group.getGamePopulationManagementTrainingEvents()));
        final boolean gamePopulationManagementTrainingParticipantsOverridden = original.isGamePopulationManagementTrainingParticipantsOverridden();
        group.setGamePopulationManagementTrainingParticipantsOverridden(gamePopulationManagementTrainingParticipantsOverridden ||
                !Objects.equals(original.getGamePopulationManagementTrainingParticipants(), group.getGamePopulationManagementTrainingParticipants()));

        //  Game environmental care trainings
        final boolean gameEnvironmentalCareTrainingEventsOverridden = original.isGameEnvironmentalCareTrainingEventsOverridden();
        group.setGameEnvironmentalCareTrainingEventsOverridden(gameEnvironmentalCareTrainingEventsOverridden ||
                !Objects.equals(original.getGameEnvironmentalCareTrainingEvents(), group.getGameEnvironmentalCareTrainingEvents()));
        final boolean gameEnvironmentalCareTrainingParticipantsOverridden = original.isGameEnvironmentalCareTrainingParticipantsOverridden();
        group.setGameEnvironmentalCareTrainingParticipantsOverridden(gameEnvironmentalCareTrainingParticipantsOverridden ||
                !Objects.equals(original.getGameEnvironmentalCareTrainingParticipants(), group.getGameEnvironmentalCareTrainingParticipants()));

        //  Other game keeping trainings
        final boolean otherGamekeepingTrainingEventsOverridden = original.isOtherGamekeepingTrainingEventsOverridden();
        group.setOtherGamekeepingTrainingEventsOverridden(otherGamekeepingTrainingEventsOverridden ||
                !Objects.equals(original.getOtherGamekeepingTrainingEvents(), group.getOtherGamekeepingTrainingEvents()));
        final boolean otherGamekeepingTrainingParticipantsOverridden = original.isOtherGamekeepingTrainingParticipantsOverridden();
        group.setOtherGamekeepingTrainingParticipantsOverridden(otherGamekeepingTrainingParticipantsOverridden ||
                !Objects.equals(original.getOtherGamekeepingTrainingParticipants(), group.getOtherGamekeepingTrainingParticipants()));

        //  Shooting trainings
        final boolean shootingTrainingEventsOverridden = original.isShootingTrainingEventsOverridden();
        group.setShootingTrainingEventsOverridden(shootingTrainingEventsOverridden ||
                !Objects.equals(original.getShootingTrainingEvents(), group.getShootingTrainingEvents()));
        final boolean shootingTrainingParticipantsOverridden = original.isShootingTrainingParticipantsOverridden();
        group.setShootingTrainingParticipantsOverridden(shootingTrainingParticipantsOverridden ||
                !Objects.equals(original.getShootingTrainingParticipants(), group.getShootingTrainingParticipants()));

        //  Tracker trainings
        final boolean trackerTrainingEventsOverridden = original.isTrackerTrainingEventsOverridden();
        group.setTrackerTrainingEventsOverridden(trackerTrainingEventsOverridden ||
                !Objects.equals(original.getTrackerTrainingEvents(), group.getTrackerTrainingEvents()));
        final boolean trackerTrainingParticipantsOverridden = original.isTrackerTrainingParticipantsOverridden();
        group.setTrackerTrainingParticipantsOverridden(trackerTrainingParticipantsOverridden ||
                !Objects.equals(original.getTrackerTrainingParticipants(), group.getTrackerTrainingParticipants()));

        updateGroup(statistics, statistics.getOrCreateOtherHunterTraining(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public OtherHunterTrainingStatistics refresh(@Nonnull final OtherHunterTrainingStatistics statistics,
                                           @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final OtherHunterTrainingStatistics copy = new OtherHunterTrainingStatistics(statistics);

        if (!copy.isSmallCarnivoreHuntingTrainingEventsOverridden()) {
            copy.setSmallCarnivoreHuntingTrainingEvents(resolver.getEventTypeCount(PIENPETOJEN_PYYNTI_KOULUTUS));
        }
        if (!copy.isSmallCarnivoreHuntingTrainingParticipantsOverridden()) {
            copy.setSmallCarnivoreHuntingTrainingParticipants(resolver.getEventParticipantsCount(PIENPETOJEN_PYYNTI_KOULUTUS));
        }

        if (!copy.isGameCountingTrainingEventsOverridden()) {
            copy.setGameCountingTrainingEvents(resolver.getEventTypeCount(RIISTALASKENTA_KOULUTUS));
        }
        if (!copy.isGameCountingTrainingParticipantsOverridden()) {
            copy.setGameCountingTrainingParticipants(resolver.getEventParticipantsCount(RIISTALASKENTA_KOULUTUS));
        }

        if (!copy.isGamePopulationManagementTrainingEventsOverridden()) {
            copy.setGamePopulationManagementTrainingEvents(resolver.getEventTypeCount(RIISTAKANTOJEN_HOITO_KOULUTUS));
        }
        if (!copy.isGamePopulationManagementTrainingParticipantsOverridden()) {
            copy.setGamePopulationManagementTrainingParticipants(resolver.getEventParticipantsCount(RIISTAKANTOJEN_HOITO_KOULUTUS));
        }

        if (!copy.isGameEnvironmentalCareTrainingEventsOverridden()) {
            copy.setGameEnvironmentalCareTrainingEvents(resolver.getEventTypeCount(RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS));
        }
        if (!copy.isGameEnvironmentalCareTrainingParticipantsOverridden()) {
            copy.setGameEnvironmentalCareTrainingParticipants(resolver.getEventParticipantsCount(RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS));
        }

        if (!copy.isOtherGamekeepingTrainingEventsOverridden()) {
            copy.setOtherGamekeepingTrainingEvents(resolver.getEventTypeCount(MUU_RIISTANHOITOKOULUTUS));
        }
        if (!copy.isOtherGamekeepingTrainingParticipantsOverridden()) {
            copy.setOtherGamekeepingTrainingParticipants(resolver.getEventParticipantsCount(MUU_RIISTANHOITOKOULUTUS));
        }

        if (!copy.isShootingTrainingEventsOverridden()) {
            copy.setShootingTrainingEvents(resolver.getEventTypeCount(AMPUMAKOULUTUS));
        }
        if (!copy.isShootingTrainingParticipantsOverridden()) {
            copy.setShootingTrainingParticipants(resolver.getEventParticipantsCount(AMPUMAKOULUTUS));
        }

        if (!copy.isTrackerTrainingEventsOverridden()) {
            copy.setTrackerTrainingEvents(resolver.getEventTypeCount(JALJESTAJAKOULUTUS));
        }
        if (!copy.isTrackerTrainingParticipantsOverridden()) {
            copy.setTrackerTrainingParticipants(resolver.getEventParticipantsCount(JALJESTAJAKOULUTUS));
        }

        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updatePublicEvents(@Nonnull final RhyAnnualStatistics statistics,
                                   @Nonnull final PublicEventStatistics group) {

        updateGroup(statistics, statistics.getOrCreatePublicEvents(), group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public OtherHuntingRelatedStatistics refresh(@Nonnull final OtherHuntingRelatedStatistics statistics,
                                                 @Nonnull final AnnualStatisticsResolver resolver) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(resolver, "resolver is null");

        final OtherHuntingRelatedStatistics copy = statistics.makeCopy();
        copy.setHarvestPermitApplicationPartners(resolver.getNumberOfHarvestPermitApplicationPartners());
        return copy;
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateOtherHuntingRelated(@Nonnull final RhyAnnualStatistics statistics,
                                          @Nonnull final OtherHuntingRelatedStatistics group) {

        updateGroup(statistics, statistics.getOrCreateOtherHuntingRelated(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void moderatorUpdateOtherHuntingRelated(@Nonnull final RhyAnnualStatistics statistics,
                                                   @Nonnull final OtherHuntingRelatedStatistics group) {

        requireNonNull(statistics, "statistics is null");
        requireNonNull(group, "dto is null");

        // isModerator = true
        statistics.assertIsUpdateable(true, localiser);

        final OtherHuntingRelatedStatistics original = statistics.getOrCreateOtherHuntingRelated();

        final OtherHuntingRelatedStatistics updated = original.makeCopy();
        updated.setMooselikeTaxationPlanningEvents(group.getMooselikeTaxationPlanningEvents());

        final boolean changed = original.merge(updated);

        final Integer newWolfTerritoryWorkgroups = group.getWolfTerritoryWorkgroups();

        final boolean wolfTerritoryWorkgroupsChanged =
                !Objects.equals(newWolfTerritoryWorkgroups, original.getWolfTerritoryWorkgroups());

        if (changed || wolfTerritoryWorkgroupsChanged) {
            if (wolfTerritoryWorkgroupsChanged) {
                original.setWolfTerritoryWorkgroups(newWolfTerritoryWorkgroups);
            }

            original.setLastModified(DateUtil.now());

            addModeratorUpdateEvent(statistics, original);
        }

        refresh(statistics);
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

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateCommunication(@Nonnull final RhyAnnualStatistics statistics,
                                    @Nonnull final CommunicationStatistics group) {

        updateGroup(statistics, statistics.getOrCreateCommunication(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateShootingRanges(@Nonnull final RhyAnnualStatistics statistics,
                                     @Nonnull final ShootingRangeStatistics group) {

        updateGroup(statistics, statistics.getOrCreateShootingRanges(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateLuke(@Nonnull final RhyAnnualStatistics statistics, @Nonnull final LukeStatistics group) {
        updateGroup(statistics, statistics.getOrCreateLuke(), group);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateMetsahallitus(@Nonnull final RhyAnnualStatistics statistics,
                                    @Nonnull final MetsahallitusStatistics group) {

        updateGroup(statistics, statistics.getOrCreateMetsahallitus(), group);
    }

    private static boolean isRefreshable(final RhyAnnualStatistics statistics) {
        return statistics.isNew() || statistics.isUpdateableByCoordinator();
    }

    private <T extends AnnualStatisticsNonComputedFields<T>> void updateGroup(final RhyAnnualStatistics statistics,
                                                                              final T original,
                                                                              final T updated) {
        requireNonNull(statistics, "statistics is null");
        requireNonNull(original, "original is null");

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean isModerator = activeUser.isModeratorOrAdmin();

        statistics.assertIsUpdateable(isModerator, localiser);

        final boolean anyChangesPresent = original.merge(updated);

        refresh(statistics);

        if (anyChangesPresent) {
            if (isModerator) {
                addModeratorUpdateEvent(statistics, original, activeUser);
            } else {
                transitionToInProgressIfNeeded(statistics);
            }
        }
    }

    private void transitionToInProgressIfNeeded(final RhyAnnualStatistics statistics) {
        if (statistics.getState() == NOT_STARTED) {
            stateTransitionService.transitionToInProgress(statistics);
        }
    }

    private void addModeratorUpdateEvent(final RhyAnnualStatistics statistics,
                                         final AnnualStatisticsNonComputedFields<?> fieldset) {

        addModeratorUpdateEvent(statistics, fieldset, activeUserService.requireActiveUser());
    }

    private void addModeratorUpdateEvent(final RhyAnnualStatistics statistics,
                                         final AnnualStatisticsNonComputedFields<?> fieldset,
                                         final SystemUser user) {

        checkArgument(user.isModeratorOrAdmin(), "user must be moderator");

        if (statistics.getYear() >= RhyAnnualStatistics.FIRST_SUBSIDY_AFFECTING_YEAR) {
            moderatorUpdateEventRepository
                    .save(new RhyAnnualStatisticsModeratorUpdateEvent(statistics, fieldset, user));
        }
    }
}
