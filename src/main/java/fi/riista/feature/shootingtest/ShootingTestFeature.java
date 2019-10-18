package fi.riista.feature.shootingtest;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialService;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialsDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.VIEW_PARTICIPANTS;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class ShootingTestFeature {

    @Resource
    private CalendarEventRepository calendarEventRepository;

    @Resource
    private ShootingTestEventRepository shootingTestEventRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Resource
    private ShootingTestAttemptRepository attemptRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private ShootingTestOfficialService officialService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ShootingTestCalendarEventDTOTransformer calendarEventTransformer;

    @Resource
    private ShootingTestParticipantDTOTransformer shootingTestParticipantDTOTransformer;

    @Transactional(readOnly = true)
    public List<ShootingTestCalendarEventDTO> listCalendarEvents(final long rhyId) {
        final Riistanhoitoyhdistys rhy = getRhyAssertingReadPermissionForShootingTestEvents(rhyId);

        final boolean isCoordinatorOrModerator =
                activeUserService.isModeratorOrAdmin() || userAuthorizationHelper.isCoordinator(rhy);

        return listCalendarEvents(singleton(rhy), !isCoordinatorOrModerator);
    }

    @Transactional(readOnly = true)
    public List<ShootingTestCalendarEventDTO> listRecentCalendarEventsForAllRhys() {
        final Person person = activeUserService.requireActivePerson();

        final Set<Organisation> rhys = occupationRepository
                .findActiveByPerson(person)
                .stream()
                .filter(occ -> {
                    final OccupationType occType = occ.getOccupationType();
                    return occType == AMPUMAKOKEEN_VASTAANOTTAJA || occType == TOIMINNANOHJAAJA;
                })
                .map(Occupation::getOrganisation)
                .collect(toSet());

        return listCalendarEvents(rhys, true);
    }

    private List<ShootingTestCalendarEventDTO> listCalendarEvents(final Collection<Organisation> rhys,
                                                                  final boolean shortList) {

        final LocalDate beginDate = ShootingTest.getBeginDateOfShootingTestEventList(shortList);
        final LocalDate endDate = DateUtil.today();

        return calendarEventTransformer.apply(calendarEventRepository.findBy(
                rhys, CalendarEventType.shootingTestTypes(), beginDate.toDate(), endDate.toDate()));
    }

    @Transactional(readOnly = true)
    public ShootingTestCalendarEventDTO getCalendarEvent(final long calendarEventId) {
        final CalendarEvent calendarEvent = calendarEventRepository.getOne(calendarEventId);

        getRhyAssertingReadPermissionForShootingTestEvents(calendarEvent.getOrganisation().getId());

        return calendarEventTransformer.apply(calendarEvent);
    }

    @Transactional
    public void openEvent(final ShootingTestOfficialsDTO dto) {
        final CalendarEvent calendarEvent = calendarEventRepository.getOne(dto.getCalendarEventId());
        final ShootingTestEvent event = new ShootingTestEvent(calendarEvent);

        activeUserService.assertHasPermission(event, CREATE);
        shootingTestEventRepository.save(event);

        officialService.assignOfficials(dto, event);
    }

    @Transactional
    public void closeEvent(final long shootingTestEventId) {
        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        final List<ShootingTestParticipant> unfinishedParticipants =
                participantRepository.findByShootingTestEventAndCompleted(event, false);

        if (!unfinishedParticipants.isEmpty()) {
            throw new IllegalShootingTestEventStateException("Cannot close event when unfinished participants exist");
        }

        event.close();
    }

    @Transactional
    public void reopenEvent(final long shootingTestEventId) {
        getEvent(shootingTestEventId, UPDATE).reopen();
    }

    @Transactional(readOnly = true)
    public List<ShootingTestParticipantDTO> listParticipants(final long shootingEventId, final boolean unfinishedOnly) {
        final ShootingTestEvent event = getEvent(shootingEventId, VIEW_PARTICIPANTS);

        final List<ShootingTestParticipant> participants = unfinishedOnly
                ? participantRepository.findByShootingTestEventAndCompleted(event, false)
                : participantRepository.findByShootingTestEvent(event);

        return shootingTestParticipantDTOTransformer.apply(participants);
    }

    @Transactional(readOnly = true)
    public ShootingTestParticipantDTO getParticipant(final long participantId) {
        return shootingTestParticipantDTOTransformer.apply(getParticipant(participantId, READ));
    }

    @Transactional(readOnly = true)
    public ShootingTestParticipantDetailsDTO getDetailedAttemptsOfParticipant(final long participantId) {
        final ShootingTestParticipant participant = getParticipant(participantId, READ);

        final ShootingTestParticipantDetailsDTO dto = new ShootingTestParticipantDetailsDTO();
        dto.setId(participantId);
        dto.setRev(participant.getConsistencyVersion());

        final Person person = participant.getPerson();
        dto.setLastName(person.getLastName());
        dto.setFirstName(person.getFirstName());
        dto.setHunterNumber(person.getHunterNumber());
        dto.setDateOfBirth(person.parseDateOfBirth());


        dto.setRegistrationTime(DateUtil.toDateTimeNullSafe(participant.getRegistrationTime()));
        dto.setCompleted(participant.isCompleted());

        dto.setAttempts(attemptRepository.findByParticipant(participant)
                .stream()
                .map(ShootingTestParticipantDetailsDTO.AttemptDTO::create)
                .collect(toList()));

        return dto;
    }

    @Transactional
    public void completePayment(final IdRevisionDTO dto) {
        final ShootingTestParticipant participant = getParticipantAndCheckRevision(dto.getId(), UPDATE, dto.getRev());

        participant.getShootingTestEvent().assertOpen("Cannot complete payment after event was closed");
        participant.setCompleted();
    }

    @Transactional
    public void updatePayment(final ParticipantPaymentUpdateDTO dto) {
        final ShootingTestParticipant participant = getParticipantAndCheckRevision(dto.getId(), UPDATE, dto.getRev());

        participant.getShootingTestEvent().assertOpen("Cannot update payment after event was closed");
        participant.updatePaymentState(dto.getPaidAttempts(), dto.isCompleted());
    }

    private Riistanhoitoyhdistys getRhyAssertingReadPermissionForShootingTestEvents(final long rhyId) {
        return requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.VIEW_SHOOTING_TEST_EVENTS);
    }

    private ShootingTestEvent getEvent(final long shootingTestEventId, final Enum<?> permission) {
        return requireEntityService.requireShootingTestEvent(shootingTestEventId, permission);
    }

    private ShootingTestParticipant getParticipant(final long participantId, final Enum<?> permission) {
        return requireEntityService.requireParticipant(participantId, permission);
    }

    private ShootingTestParticipant getParticipantAndCheckRevision(final long participantId,
                                                                   final Enum<?> permission,
                                                                   final int revision) {

        final ShootingTestParticipant participant = requireEntityService.requireParticipant(participantId, permission);
        DtoUtil.assertNoVersionConflict(participant, revision);
        return participant;
    }
}
