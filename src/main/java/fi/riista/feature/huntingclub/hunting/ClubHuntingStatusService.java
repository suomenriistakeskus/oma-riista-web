package fi.riista.feature.huntingclub.hunting;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ClubHuntingStatusService {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubPermitService huntingClubPermitService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GroupHuntingStatusDTO getGroupStatus(final @Nonnull HuntingClubGroup group) {
        Objects.requireNonNull(group, "group is null");

        final GroupHuntingStatusDTO result = new GroupHuntingStatusDTO();

        if (group.getHarvestPermit() == null) {
            result.setCanEditPermit(true);
            return result;
        }

        result.setFromMooseDataCard(group.isFromMooseDataCard());

        if (!isGroupHuntingDataLocked(group)) {
            result.setCanCreateHarvest(true);
            result.setCanEditHuntingDay(true);
            result.setCanEditDiaryEntry(true);

            if (group.getSpecies().isMoose()) {
                result.setCanEditPermit(!groupHuntingDayRepository.groupHasHuntingDays(group));
                result.setCanCreateObservation(true);
                result.setCanCreateHuntingDay(true);

            } else if (!groupHasDiaryEntriesLinkedToHuntingDay(group)) {
                result.setCanEditPermit(true);
            }
        }

        return result;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isDiaryEntryLocked(final @Nonnull GameDiaryEntry diaryEntry) {
        Objects.requireNonNull(diaryEntry, "diaryEntry is null");
        return diaryEntry.getHuntingDayOfGroup() != null &&
                isGroupHuntingDataLocked(diaryEntry.getHuntingDayOfGroup().getGroup());
    }

    private boolean isGroupHuntingDataLocked(final @Nonnull HuntingClubGroup group) {
        if (huntingClubPermitService.hasClubHuntingFinished(group)) {
            return true;
        }

        if (activeUserService.isModeratorOrAdmin()) {
            return false;
        }

        if (group.isFromMooseDataCard() || !groupHasHuntingLeader(group) || group.getHarvestPermit() == null) {
            return true;
        }

        if (harvestPermitLockedByDateService.isPermitLockedByDateForHuntingYear(group.getHarvestPermit(), group.getHuntingYear())) {
            return true;
        }

        final EnumSet<OccupationType> leaderRoles = EnumSet.of(
                OccupationType.SEURAN_YHDYSHENKILO,
                OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final Set<OccupationType> activePersonRoles =
                findActiveClubAndGroupOccupationTypes(group, activeUserService.requireActiveUser().getPerson());

        return Sets.intersection(leaderRoles, activePersonRoles).isEmpty();
    }

    private Set<OccupationType> findActiveClubAndGroupOccupationTypes(final @Nonnull HuntingClubGroup group,
                                                                      final Person person) {
        Objects.requireNonNull(group, "group is null");
        final Organisation club = group.getParentOrganisation();
        Objects.requireNonNull(club, "club is null");

        if (person == null) {
            return Collections.emptySet();
        }

        final List<Occupation> groupOccupations = occupationRepository.findActiveByOrganisationAndPerson(group, person);
        final List<Occupation> clubOccupations = occupationRepository.findActiveByOrganisationAndPerson(club, person);

        return Stream.concat(clubOccupations.stream(), groupOccupations.stream())
                .map(Occupation::getOccupationType)
                .collect(Collectors.toSet());
    }

    private boolean groupHasHuntingLeader(final HuntingClubGroup group) {
        final QOccupation occupation = QOccupation.occupation;
        return 0 < occupationRepository.count(occupation.organisation.eq(group)
                .and(occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA))
                .and(occupation.validAndNotDeleted()));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean groupHasDiaryEntriesLinkedToHuntingDay(final @Nonnull HuntingClubGroup group) {
        return groupHuntingDayRepository.groupHasHarvestLinkedToHuntingDay(group) ||
                groupHuntingDayRepository.groupHasObservationLinkedToHuntingDay(group);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean clubHasDiaryEntriesLinkedToHuntingDay(final @Nonnull HuntingClub club) {
        return groupHuntingDayRepository.clubHasHarvestLinkedToHuntingDay(club) ||
                groupHuntingDayRepository.clubHasObservationLinkedToHuntingDay(club);
    }
}
