package fi.riista.feature.huntingclub.members.group;

import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.members.CannotModifyLockedClubOccupationException;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Prevent hunting leader from leaving club group if:
 *
 * 1) No other hunting leaders exists
 * 2) Group has stored hunting data (hunting days)
 */
@Service
public class HuntingLeaderCanExitGroupService {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertHuntingLeaderNotLocked(final Occupation occupation) {
        if (isHuntingLeaderLocked(occupation)) {
            throw new CannotModifyLockedClubOccupationException(
                    "Cannot modify locked group hunting leader occupation id=" + occupation.getId());
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isHuntingLeaderLocked(final Occupation occupation) {
        Objects.requireNonNull(occupation, "occupation is null");

        return occupation.getOccupationType() == OccupationType.RYHMAN_METSASTYKSENJOHTAJA &&
                !groupHasAnotherActiveHuntingLeader(occupation.getOrganisation(), occupation.getPerson()) &&
                groupHasData(occupation.getOrganisation());
    }

    private boolean groupHasAnotherActiveHuntingLeader(final Organisation group, final Person person) {
        final List<Occupation> activeContactPersons = occupationRepository.findActiveByOrganisationAndOccupationType(
                group, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        return activeContactPersons.stream().anyMatch(o -> !o.getPerson().equals(person));
    }

    private boolean groupHasData(final Organisation org) {
        final HuntingClubGroup group = huntingClubGroupRepository.getOne(org.getId());

        return groupHasActiveMembers(group) ||
                // only moose groups have hunting day data stored by user
                (group.getSpecies().isMoose() && groupHuntingDayRepository.groupHasHuntingDays(group)) ||
                clubHuntingStatusService.groupHasDiaryEntriesLinkedToHuntingDay(group);
    }

    private boolean groupHasActiveMembers(final HuntingClubGroup group) {
        final QOccupation occupation = QOccupation.occupation;

        return 0 < occupationRepository.count(occupation.organisation.eq(group)
                .and(occupation.validAndNotDeleted())
                .and(occupation.occupationType.ne(OccupationType.RYHMAN_METSASTYKSENJOHTAJA)));
    }
}
