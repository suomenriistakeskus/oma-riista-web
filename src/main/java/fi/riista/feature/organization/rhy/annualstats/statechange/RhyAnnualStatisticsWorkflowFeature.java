package fi.riista.feature.organization.rhy.annualstats.statechange;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.dto.BaseEntityEventDTO;
import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.service.BaseEntityEventService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsLockedException;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsRepository;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.feature.organization.rhy.annualstats.audit.RhyAnnualStatisticsNotificationService;
import fi.riista.feature.organization.rhy.annualstats.QRhyAnnualStatistics;
import fi.riista.util.Collect;
import fi.riista.util.DtoUtil;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.RiistakeskusAuthorization.Permission.BATCH_APPROVE_ANNUAL_STATISTICS;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsAuthorization.Permission.CHANGE_APPROVAL_STATUS;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.APPROVED;
import static fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState.UNDER_INSPECTION;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.Collect.indexingByIdOf;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Service
public class RhyAnnualStatisticsWorkflowFeature {

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepo;

    @Resource
    private RhyAnnualStatisticsRepository annualStatsRepo;

    @Resource
    private OccupationRepository occupationRepo;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private RhyAnnualStatisticsStateTransitionService stateTransitionService;

    @Resource
    private RhyAnnualStatisticsNotificationService notificationService;

    @Resource
    private BaseEntityEventService baseEventService;

    @Resource
    private MessageSource messageSource;

    @Resource
    private JPAQueryFactory queryFactory;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<RhyAnnualStatisticsProgressDTO> listAnnualStatistics(final int year) {
        final Map<Long, BaseEntityEventDTO> rhyIdToBaseEventEntityDTO = getRhyIdToBaseEntityEventDTO(year);

        final Map<Long, RhyAnnualStatistics> rhyIdToAnnualStats =
                annualStatsRepo.findByYear(year).stream().collect(indexingByIdOf(RhyAnnualStatistics::getRhy));

        final Map<Long, Set<Occupation>> rhyIdToCoordinators =
                occupationRepo.findActiveByOccupationTypeGroupByOrganisationId(TOIMINNANOHJAAJA);

        final Set<String> officialCodesOfRhysNotExistingAtGivenYear =
                MergedRhyMapping.getOfficialCodesOfRhysNotExistingAtYear(year);

        return rhyRepo.findAll()
                .stream()
                .filter(rhy -> !officialCodesOfRhysNotExistingAtGivenYear.contains(rhy.getOfficialCode()))
                .map(rhy -> {

                    final long rhyId = rhy.getId();
                    final RhyAnnualStatistics annualStats = rhyIdToAnnualStats.get(rhyId);

                    final Set<Occupation> coordinators = rhyIdToCoordinators.computeIfAbsent(rhyId, key -> emptySet());
                    final Person coordinatorPerson = coordinators.stream()
                            .sorted(HasBeginAndEndDate.DEFAULT_COMPARATOR)
                            .findFirst()
                            .map(Occupation::getPerson)
                            .orElse(null);

                    BaseEntityEventDTO baseEntityEventDTO = null;
                    if(rhyIdToBaseEventEntityDTO != null) {
                        baseEntityEventDTO = rhyIdToBaseEventEntityDTO.get(rhyId);
                    }

                    return RhyAnnualStatisticsProgressDTO.create(rhy, annualStats, coordinatorPerson, baseEntityEventDTO);
                })
                .sorted(comparing(RhyAnnualStatisticsProgressDTO::getRhyCode))
                .collect(toList());
    }

    private Map<Long, BaseEntityEventDTO> getRhyIdToBaseEntityEventDTO(final int year) {
        final QRhyAnnualStatisticsStateChangeEvent EVENT = QRhyAnnualStatisticsStateChangeEvent.rhyAnnualStatisticsStateChangeEvent;
        final QRhyAnnualStatistics STATS = QRhyAnnualStatistics.rhyAnnualStatistics;

        final Map<Long, List<RhyAnnualStatisticsStateChangeEvent>> rhyIdToStateChangeEvents =
                queryFactory.select(STATS.rhy.id, EVENT)
                        .from(EVENT)
                        .join(EVENT.statistics, STATS)
                        .where(STATS.year.eq(year))
                        .where(STATS.state.in(UNDER_INSPECTION, APPROVED))
                        .where(EVENT.state.eq(UNDER_INSPECTION))
                        .orderBy(EVENT.eventTime.desc())
                        .transform(GroupBy.groupBy(STATS.rhy.id).as(GroupBy.list(EVENT)));

        final List<RhyAnnualStatisticsStateChangeEvent> eventList = rhyIdToStateChangeEvents.values().stream()
                .map(e -> e.get(0))
                .collect(toList());
        final Map<Long, BaseEntityEventDTO> eventIdToEventDTOs = baseEventService.getBaseEntityEventDTOList(eventList).stream()
                .collect(Collect.indexingBy(BaseEntityEventDTO::getEventId));

        Map<Long, BaseEntityEventDTO> rhyIdToBaseEntityEventDTO = new HashMap<>();
        rhyIdToStateChangeEvents.forEach((rhyId, events) -> {
            rhyIdToBaseEntityEventDTO.put(rhyId, eventIdToEventDTOs.get(events.get(0).getId()));
        });

        return rhyIdToBaseEntityEventDTO;
    }

    @Transactional
    public void submitForInspection(@Nonnull final IdRevisionDTO dto) {
        requireNonNull(dto);

        final RhyAnnualStatistics annualStats = getAnnualStatisticsAndCheckRevision(dto.getId(), UPDATE, dto.getRev());

        stateTransitionService.transitionToSubmittedForInspection(annualStats);
        notificationService.sendSubmitConfirmationNotification(annualStats);
    }

    @Transactional
    public void approve(@Nonnull final IdRevisionDTO dto) {
        requireNonNull(dto);

        final RhyAnnualStatistics annualStats =
                getAnnualStatisticsAndCheckRevision(dto.getId(), CHANGE_APPROVAL_STATUS, dto.getRev());

        validateYear(annualStats.getYear());

        stateTransitionService.transitionToApproved(annualStats);
    }

    @Transactional
    public void batchApprove(final int year, @Nonnull final List<Long> annualStatisticsIds) {
        requireNonNull(annualStatisticsIds);

        requireEntityService.requireRiistakeskus(BATCH_APPROVE_ANNUAL_STATISTICS);

        validateYear(year);

        final List<RhyAnnualStatistics> annualStats = annualStatsRepo
                .findAll(annualStatisticsIds)
                .stream()
                .filter(stats -> {
                    if (stats.getYear() != year) {
                        throw new IllegalArgumentException(format(
                                "RHY annual statistics with ID=%d is not for given year %d", stats.getId(), year));
                    }

                    return stats.isCompleteForApproval();
                })
                .collect(toList());

        annualStats.forEach(stateTransitionService::transitionToApproved);
    }

    @Transactional
    public void cancelApproval(@Nonnull final IdRevisionDTO dto) {
        requireNonNull(dto);

        final RhyAnnualStatistics annualStats =
                getAnnualStatisticsAndCheckRevision(dto.getId(), CHANGE_APPROVAL_STATUS, dto.getRev());

        validateYear(annualStats.getYear());

        stateTransitionService.cancelApproval(annualStats);
    }

    private RhyAnnualStatistics getAnnualStatisticsAndCheckRevision(final long annualStatisticsId,
                                                                    final Enum<?> permission,
                                                                    final int revision) {
        final RhyAnnualStatistics annualStats =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, permission);
        DtoUtil.assertNoVersionConflict(annualStats, revision);
        return annualStats;
    }

    private static void validateYear(final int year) {
        if (year < 2018) {
            throw new AnnualStatisticsLockedException("Cannot perform operation on RHY annual statistics before year 2018");
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public AnnualStatisticsSendersExcelView exportAnnualStatisticsSendersView(final int year, final Locale locale, final RhyAnnualStatisticsState statisticsState) {
        checkArgument(statisticsState.equals(UNDER_INSPECTION) || statisticsState.equals(APPROVED), "Statistics must be either under inspection or approved");
        final List<RhyAnnualStatisticsProgressDTO> statisticsProgressList = listAnnualStatistics(year)
                .stream()
                .filter(dto -> dto.getSubmitEvent() != null)
                .filter(dto -> dto.getAnnualStatsState() == statisticsState)
                .collect(toList());
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);

        return new AnnualStatisticsSendersExcelView(year, statisticsProgressList, locale, localiser, statisticsState);
    }
}
