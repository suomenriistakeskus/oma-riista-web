package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.DtoUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.Collect.indexingByIdOf;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
public class RhyAnnualStatisticsFeature {

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

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<RhyAnnualStatisticsProgressDTO> getAnnualStatisticsProgress(final int year) {
        final Map<Long, RhyAnnualStatistics> rhyIdToAnnualStats =
                annualStatsRepo.findByYear(year).stream().collect(indexingByIdOf(RhyAnnualStatistics::getRhy));

        final Map<Long, Set<Occupation>> rhyIdToCoordinators =
                occupationRepo.findActiveByOccupationTypeGroupByOrganisationId(TOIMINNANOHJAAJA);

        return rhyRepo.findAll()
                .stream()
                .map(rhy -> {
                    final long rhyId = rhy.getId();
                    final RhyAnnualStatistics annualStats = rhyIdToAnnualStats.get(rhyId);
                    final Set<Occupation> coordinators = rhyIdToCoordinators.computeIfAbsent(rhyId, key -> emptySet());
                    final Person coordinatorPerson = coordinators.stream()
                            .sorted(HasBeginAndEndDate.DEFAULT_COMPARATOR)
                            .findFirst()
                            .map(Occupation::getPerson)
                            .orElse(null);

                    return RhyAnnualStatisticsProgressDTO.create(rhy, annualStats, coordinatorPerson);
                })
                .sorted(comparing(RhyAnnualStatisticsProgressDTO::getRhyCode))
                .collect(toList());
    }

    @Transactional
    public void submitForInspection(final IdRevisionDTO dto) {
        final RhyAnnualStatistics annualStats = getAnnualStatisticsAndCheckRevision(dto.getId(), UPDATE, dto.getRev());
        stateTransitionService.transitionToSubmittedForInspection(annualStats);
    }

    @Transactional
    public void approve(final IdRevisionDTO dto) {
        final RhyAnnualStatistics annualStats = getAnnualStatisticsAndCheckRevision(
                dto.getId(), RhyAnnualStatisticsAuthorization.Permission.APPROVE, dto.getRev());

        if (annualStats.getYear() < 2018) {
            throw new AnnualStatisticsLockedException("Cannot approve RHY annual statistics before year 2018");
        }

        stateTransitionService.transitionToApproved(annualStats);
    }

    private RhyAnnualStatistics getAnnualStatisticsAndCheckRevision(final long annualStatisticsId,
                                                                    final Enum<?> permission,
                                                                    final int revision) {

        final RhyAnnualStatistics annualStats =
                requireEntityService.requireRhyAnnualStatistics(annualStatisticsId, permission);
        DtoUtil.assertNoVersionConflict(annualStats, revision);
        return annualStats;
    }
}
