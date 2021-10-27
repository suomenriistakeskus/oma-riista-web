package fi.riista.feature.huntingclub.hunting;

import com.google.common.collect.Sets;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static java.util.Objects.requireNonNull;

@Service
public class ClubHuntingStatusService {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GroupHuntingStatusDTO getGroupStatus(final @Nonnull HuntingClubGroup group) {
        requireNonNull(group);

        final GroupHuntingStatusDTO result = new GroupHuntingStatusDTO();

        if (group.getHarvestPermit() == null) {
            result.setCanEditPermit(true);
            return result;
        }

        result.setFromMooseDataCard(group.isFromMooseDataCard());

        final SystemUser activeUser = activeUserService.requireActiveUser();

        if (!isGroupHuntingDataLocked(group, activeUser)) {
            result.setCanCreateHarvest(true);
            result.setCanEditHuntingDay(true);
            result.setCanEditDiaryEntry(true);

            final GameSpecies species = group.getSpecies();

            if (species.isMoose()) {
                result.setCanEditPermit(!groupHuntingDayRepository.groupHasHuntingDays(group));
                result.setCanCreateObservation(true);
                result.setCanCreateHuntingDay(true);
            } else {
                result.setCanEditHuntingDay(false);
                result.setCanEditPermit(!groupHasDiaryEntriesLinkedToHuntingDay(group));
            }
        }

        return result;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isHarvestLocked(final @Nonnull Harvest harvest) {
        requireNonNull(harvest);

        final SystemUser activeUser = activeUserService.requireActiveUser();

        return harvest.getHuntingDayOfGroup() != null &&
                isGroupHuntingDataLocked(harvest.getHuntingDayOfGroup().getGroup(), activeUser);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isGroupHuntingDataLocked(final @Nonnull HuntingClubGroup group,
                                            final @Nonnull SystemUser activeUser) {

        if (huntingFinishingService.hasPermitPartnerFinishedHunting(group)) {
            return true;
        }

        if (activeUser.isModeratorOrAdmin()) {
            return false;
        }

        if (group.isFromMooseDataCard() || !groupHasHuntingLeader(group) || group.getHarvestPermit() == null) {
            return true;
        }

        if (harvestPermitLockedByDateService.isPermitLocked(group)) {
            return true;
        }

        final EnumSet<OccupationType> leaderRoles = EnumSet.of(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA);

        final Set<OccupationType> activePersonRoles =
                findActiveClubAndGroupOccupationTypes(group, activeUser.getPerson());

        return Sets.intersection(leaderRoles, activePersonRoles).isEmpty();
    }

    private Set<OccupationType> findActiveClubAndGroupOccupationTypes(final @Nonnull HuntingClubGroup group,
                                                                      final Person person) {
        requireNonNull(group, "group is null");
        final Organisation club = group.getParentOrganisation();
        requireNonNull(club, "club is null");

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
                .and(occupation.occupationType.eq(RYHMAN_METSASTYKSENJOHTAJA))
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
