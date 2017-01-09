package fi.riista.feature.huntingclub.members.club;

import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayRepository;
import fi.riista.feature.huntingclub.members.CannotModifyLockedClubOccupationException;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationRepository;
import fi.riista.feature.huntingclub.members.invitation.QHuntingClubMemberInvitation;
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
 * Prevent contact person from leaving the club if:
 *
 * 1) No other contact person exists
 * 2) Club has stored hunting data (areas, groups, members, hunting days)
 */
@Service
public class ContactPersonCanExitClubService {

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubMemberInvitationRepository huntingClubMemberInvitationRepository;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private GroupHuntingDayRepository groupHuntingDayRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertContactPersonNotLocked(final Occupation occupation) {
        if (isContactPersonLocked(occupation)) {
            throw new CannotModifyLockedClubOccupationException(
                    "Cannot modify locked club contact person occupation id=" + occupation.getId());
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isContactPersonLocked(final Occupation occupation) {
        Objects.requireNonNull(occupation, "occupation is null");

        final HuntingClub club = huntingClubRepository.getOne(occupation.getOrganisation().getId());

        return occupation.getOccupationType() == OccupationType.SEURAN_YHDYSHENKILO &&
                clubHasData(club) &&
                !clubHasAnotherActiveContactPerson(club, occupation.getPerson());
    }

    private boolean clubHasAnotherActiveContactPerson(final Organisation club, final Person person) {
        final List<Occupation> activeContactPersons = occupationRepository.findActiveByOrganisationAndOccupationType(
                club, OccupationType.SEURAN_YHDYSHENKILO);

        return activeContactPersons.stream().anyMatch(o -> !o.getPerson().equals(person));
    }

    private boolean clubHasData(final HuntingClub club) {
        return clubHasActiveMembers(club) ||
                clubHasGroupsWithActiveMembers(club) ||
                clubHasActiveHuntingAreas(club) ||
                clubHasPendingInvitation(club) ||
                groupHuntingDayRepository.clubHasMooseGroupsWithHuntingDays(club) ||
                clubHuntingStatusService.clubHasDiaryEntriesLinkedToHuntingDay(club);
    }

    private boolean clubHasPendingInvitation(final HuntingClub club) {
        final QHuntingClubMemberInvitation invitation = QHuntingClubMemberInvitation.huntingClubMemberInvitation;
        return 0 < huntingClubMemberInvitationRepository.count(invitation.huntingClub.eq(club));
    }

    private boolean clubHasActiveMembers(final HuntingClub club) {
        final QOccupation occupation = QOccupation.occupation;

        return 0 < occupationRepository.count(occupation.organisation.eq(club)
                .and(occupation.validAndNotDeleted())
                .and(occupation.occupationType.ne(OccupationType.SEURAN_YHDYSHENKILO)));
    }

    private boolean clubHasGroupsWithActiveMembers(final HuntingClub club) {
        final QOccupation occupation = QOccupation.occupation;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return 0 < occupationRepository.count(occupation.organisation.in(
                JPAExpressions.selectFrom(group).where(group.parentOrganisation.eq(club)))
                .and(occupation.validAndNotDeleted()));
    }

    private boolean clubHasActiveHuntingAreas(final HuntingClub club) {
        final QHuntingClubArea huntingClubArea = QHuntingClubArea.huntingClubArea;
        return 0 < huntingClubAreaRepository.count(huntingClubArea.club.eq(club)
                .and(huntingClubArea.active.isTrue()));
    }
}
