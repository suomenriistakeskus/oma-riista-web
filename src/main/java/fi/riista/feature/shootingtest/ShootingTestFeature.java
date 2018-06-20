package fi.riista.feature.shootingtest;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.dto.IdRevisionDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.calendar.CalendarEventRepository;
import fi.riista.feature.organization.calendar.CalendarEventType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import org.joda.time.LocalDate;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.COMPLETED;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.DISQUALIFIED_AS_OFFICIAL;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.HUNTING_BAN;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.HUNTING_PAYMENT_DONE;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.HUNTING_PAYMENT_NOT_DONE;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.IN_PROGRESS;
import static fi.riista.feature.shootingtest.PersonShootingTestRegistrationDTO.ShootingTestRegistrationCheckStatus.NO_HUNTER_NUMBER;
import static fi.riista.feature.shootingtest.ShootingTestEvent.DAYS_UPDATEABLE_BY_OFFICIAL;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.ASSIGN_OFFICIALS;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.Collect.greatestAfterGroupingBy;
import static fi.riista.util.Collect.idSet;
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
    private ShootingTestOfficialRepository officialRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Resource
    private ShootingTestAttemptRepository attemptRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private ShootingTestCalendarEventDTOTransformer calendarEventTransformer;

    @Resource
    private ShootingTestOfficialOccupationDTOTransformer shootingTestOfficialOccupationDTOTransformer;

    @Resource
    private ShootingTestParticipantDTOTransformer shootingTestParticipantDTOTransformer;

    @Resource
    private ShootingTestStatisticsService shootingTestStatisticsService;

    @Resource
    private MessageSource messageSource;

    @Transactional(readOnly = true)
    public List<ShootingTestCalendarEventDTO> listCalendarEvents(final long rhyId) {
        final Riistanhoitoyhdistys rhy = getRhyAssertingReadPermissionForShootingTestEvents(rhyId);

        final boolean isCoordinatorOrModerator =
                activeUserService.isModeratorOrAdmin() || userAuthorizationHelper.isCoordinator(rhy);

        return listCalendarEvents(singleton(rhy), isCoordinatorOrModerator);
    }

    @Transactional(readOnly = true)
    public List<ShootingTestCalendarEventDTO> listRecentCalendarEventsForAllRhys() {
        final Person person = activeUserService.requireActivePerson();

        final List<Organisation> rhys = occupationRepository.findActiveByPerson(person)
                .stream()
                .filter(occ -> occ.getOccupationType() == AMPUMAKOKEEN_VASTAANOTTAJA)
                .map(Occupation::getOrganisation)
                .collect(toList());

        return listCalendarEvents(rhys, false);
    }

    private List<ShootingTestCalendarEventDTO> listCalendarEvents(final Collection<Organisation> rhys,
                                                                  final boolean isCoordinatorOrModerator) {

        final LocalDate endDate = DateUtil.today();
        final LocalDate beginDate = isCoordinatorOrModerator
                ? endDate.withDayOfYear(1)
                : endDate.minus(DAYS_UPDATEABLE_BY_OFFICIAL);

        return calendarEventTransformer.apply(calendarEventRepository.findBy(
                rhys, CalendarEventType.shootingTestTypes(), beginDate.toDate(), endDate.toDate()));
    }

    @Transactional(readOnly = true)
    public ShootingTestCalendarEventDTO getCalendarEvent(final long calendarEventId) {
        final CalendarEvent calendarEvent = calendarEventRepository.getOne(calendarEventId);

        getRhyAssertingReadPermissionForShootingTestEvents(calendarEvent.getOrganisation().getId());

        return calendarEventTransformer.apply(calendarEvent);
    }

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listAvailableOfficials(final long rhyId) {
        final Organisation rhy = getRhyAssertingReadPermissionForShootingTestEvents(rhyId);

        return shootingTestOfficialOccupationDTOTransformer.transform(findAvailableOfficialOccupations(rhy));
    }

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listQualifyingOfficials(final long eventId) {
        final ShootingTestEvent event = getEvent(eventId, READ);

        final Set<Long> participantPersonIds = streamParticipantPersons(event).map(Person::getId).collect(toSet());

        final List<Occupation> officials = findAvailableOfficialOccupations(event.getCalendarEvent().getOrganisation())
                .stream()
                .filter(occupation -> !participantPersonIds.contains(occupation.getPerson().getId()))
                .collect(toList());

        return shootingTestOfficialOccupationDTOTransformer.transform(officials);
    }

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listAssignedOfficials(final long eventId) {
        final ShootingTestEvent event = getEvent(eventId, READ);

        final List<Occupation> officials = officialRepository.findByShootingTestEvent(event)
                .stream()
                .map(ShootingTestOfficial::getOccupation)
                .collect(toList());

        return shootingTestOfficialOccupationDTOTransformer.transform(officials);
    }

    @Transactional
    public void openEvent(final ShootingTestOfficialsDTO dto) {
        final CalendarEvent calendarEvent = calendarEventRepository.getOne(dto.getCalendarEventId());
        checkArgument(calendarEvent.getCalendarEventType().isShootingTest());

        final ShootingTestEvent event = new ShootingTestEvent(calendarEvent);
        activeUserService.assertHasPermission(event, CREATE);
        shootingTestEventRepository.save(event);

        assignOfficials(dto, event);
    }

    @Transactional
    public void assignOfficials(final ShootingTestOfficialsDTO dto) {
        final ShootingTestEvent event = getEvent(dto.getShootingTestEventId(), ASSIGN_OFFICIALS);
        event.assertOpen("Cannot assign officials after event was closed");

        officialRepository.deleteInBatch(event.getOfficials());
        final List<ShootingTestOfficial> newOfficials = assignOfficials(dto, event);

        final Set<Long> participantPersonIds = streamParticipantPersons(event).collect(idSet());

        final boolean anyOfficialAsParticipant = newOfficials.stream().anyMatch(official -> {
            return participantPersonIds.contains(official.getOccupation().getPerson().getId());
        });

        if (anyOfficialAsParticipant) {
            throw new ParticipantAsOfficialException();
        }
    }

    private List<ShootingTestOfficial> assignOfficials(final ShootingTestOfficialsDTO dto,
                                                       final ShootingTestEvent event) {

        checkArgument(dto.getOccupationIds().size() >= 2, "Shooting test event must have at least 2 officials");

        return dto.getOccupationIds()
                .stream()
                .map(occupationId -> addNewOfficial(occupationId, event))
                .collect(toList());
    }

    private ShootingTestOfficial addNewOfficial(final long occupationId, final ShootingTestEvent event) {
        final Occupation occupation = getAndAssertOccupation(occupationId, event.getCalendarEvent());
        return officialRepository.save(new ShootingTestOfficial(event, occupation));
    }

    private Occupation getAndAssertOccupation(final long occupationId, final CalendarEvent calendarEvent) {
        final Occupation occupation = occupationRepository.getOne(occupationId);
        final LocalDate eventDate = DateUtil.toLocalDateNullSafe(calendarEvent.getDate());

        checkArgument(occupation.isActiveWithinPeriod(eventDate, eventDate), "Occupation must be active on event date");
        checkArgument(occupation.getOccupationType() == AMPUMAKOKEEN_VASTAANOTTAJA,
                "Occupation must be of type " + AMPUMAKOKEEN_VASTAANOTTAJA.name());
        checkArgument(Objects.equals(occupation.getOrganisation().getId(), calendarEvent.getOrganisation().getId()),
                "Occupation organisation should match to shootingTestEvent's organisation");

        return occupation;
    }

    @Transactional
    public void closeEvent(final long eventId) {
        final ShootingTestEvent event = getEvent(eventId, UPDATE);

        final List<ShootingTestParticipant> unfinishedParticipants =
                participantRepository.findByShootingTestEventAndCompleted(event, false);

        if (!unfinishedParticipants.isEmpty()) {
            throw new IllegalShootingTestEventStateException("Cannot close event when unfinished participants exist");
        }

        event.close();
    }

    @Transactional
    public void reopenEvent(final long eventId) {
        getEvent(eventId, UPDATE).reopen();
    }

    @Transactional(readOnly = true)
    public PersonShootingTestRegistrationDTO findPersonByHunterNumberForRegistration(final long shootingTestEventId,
                                                                                     final String hunterNumber) {

        checkArgument(StringUtils.hasText(hunterNumber), "empty hunterNumber");
        return getPersonForRegistration(shootingTestEventId, () -> personLookupService.findByHunterNumber(hunterNumber));
    }

    @Transactional(readOnly = true)
    public PersonShootingTestRegistrationDTO findPersonBySsnForRegistration(final long shootingTestEventId,
                                                                            final String ssn) {

        checkArgument(StringUtils.hasText(ssn), "empty ssn");
        return getPersonForRegistration(shootingTestEventId, () -> personLookupService.findBySsnNoFallback(ssn));
    }

    private PersonShootingTestRegistrationDTO getPersonForRegistration(final long shootingTestEventId,
                                                                       final Supplier<Optional<Person>> personSupplier) {

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);
        final Set<Long> officialPersonIds = streamOfficialPersons(event).collect(idSet());

        return personSupplier.get()
                .map(person -> {
                    final Optional<ShootingTestParticipant> existingParticipant =
                            participantRepository.findByShootingTestEventAndPerson(event, person);

                    final ShootingTestRegistrationCheckStatus registrationStatus;

                    if (officialPersonIds.contains(person.getId())) {
                        registrationStatus = DISQUALIFIED_AS_OFFICIAL;
                    } else {
                        registrationStatus = existingParticipant
                                .map(participant -> participant.isCompleted() ? COMPLETED : IN_PROGRESS)
                                .orElseGet(() -> {
                                    if (person.isHuntingBanActiveNow()) {
                                        return HUNTING_BAN;
                                    }
                                    if (person.getHunterNumber() == null) {
                                        return NO_HUNTER_NUMBER;
                                    }

                                    return person.getHuntingPaymentDateForNextOrCurrentSeason().isPresent()
                                            ? HUNTING_PAYMENT_DONE
                                            : HUNTING_PAYMENT_NOT_DONE;
                                });
                    }

                    final SelectedShootingTestTypesDTO selectedShootingTestTypes = existingParticipant
                            .map(SelectedShootingTestTypesDTO::create)
                            .orElseGet(SelectedShootingTestTypesDTO::new);

                    return new PersonShootingTestRegistrationDTO(person, registrationStatus, selectedShootingTestTypes);
                })
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public void registerParticipant(final long shootingTestEventId,
                                    final long personId,
                                    final SelectedShootingTestTypesDTO selectedTypes) {

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        event.assertOpen("Cannot register participant after event was closed");

        final Person person = personRepository.getOne(personId);

        if (streamOfficialPersons(event).anyMatch(person::equals)) {
            throw new ParticipantAsOfficialException();
        }

        final Optional<ShootingTestParticipant> existingParticipant =
                participantRepository.findByShootingTestEventAndPerson(event, person);

        final ShootingTestParticipant participant = existingParticipant
                .map(p -> {
                    if (!p.isCompleted()) {
                        throw new CannotRegisterShootingTestParticipantException(String.format(
                                "Person (id=%d) has already registered into shooting test event (id=%d)",
                                personId, shootingTestEventId));
                    }

                    p.reRegister();
                    return p;
                })
                .orElseGet(() -> {
                    if (person.isHuntingBanActiveNow()) {
                        throw new CannotRegisterShootingTestParticipantException(String.format(
                                "Person (id=%d) has active hunting ban", personId));
                    }
                    if (person.getHunterNumber() == null) {
                        throw new CannotRegisterShootingTestParticipantException(String.format(
                                "Person (id=%d) does not have hunter number", personId));
                    }

                    return new ShootingTestParticipant(event, person);
                });

        participant.setMooseTestIntended(selectedTypes.isMooseTestIntended());
        participant.setBearTestIntended(selectedTypes.isBearTestIntended());
        participant.setDeerTestIntended(selectedTypes.isRoeDeerTestIntended());
        participant.setBowTestIntended(selectedTypes.isBowTestIntended());

        participantRepository.save(participant);
    }

    @Transactional(readOnly = true)
    public List<ShootingTestParticipantDTO> listParticipants(final long shootingEventId, final boolean unfinishedOnly) {
        final ShootingTestEvent event = getEvent(shootingEventId, READ);
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

    @Transactional(readOnly = true)
    public ShootingTestStatisticsDTO getStatistics(final long rhyId, final int calendarYear) {
        final Riistanhoitoyhdistys rhy = getRhyAssertingReadPermissionForShootingTestEvents(rhyId);
        return shootingTestStatisticsService.calculate(rhy, calendarYear);
    }

    @Transactional(readOnly = true)
    public View exportStatisticsToExcel(final long rhyId, final int calendarYear, final Locale locale) {
        final Riistanhoitoyhdistys rhy = getRhyAssertingReadPermissionForShootingTestEvents(rhyId);
        final EnumLocaliser localiser = new EnumLocaliser(messageSource, locale);
        final ShootingTestStatisticsDTO dto = shootingTestStatisticsService.calculate(rhy, calendarYear);

        return new ShootingTestExcelView(localiser, calendarYear, rhy.getNameLocalisation(), dto);
    }

    private Riistanhoitoyhdistys getRhyAssertingReadPermissionForShootingTestEvents(final long rhyId) {
        return requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.VIEW_SHOOTING_TEST_EVENTS);
    }

    private ShootingTestEvent getEvent(final long eventId, final Enum<?> permission) {
        return requireEntityService.requireShootingTestEvent(eventId, permission);
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

    private List<Occupation> findAvailableOfficialOccupations(final Organisation rhy) {
        return occupationRepository.findActiveByOrganisationAndOccupationType(rhy, AMPUMAKOKEEN_VASTAANOTTAJA)
                .stream()
                // Collect latest occupation for each person having multiple shooting test official occupations.
                .collect(greatestAfterGroupingBy(occ -> occ.getPerson().getId(), HasBeginAndEndDate.DEFAULT_COMPARATOR))
                .values()
                .stream()
                .sorted(OccupationSort.BY_FULL_NAME)
                .collect(toList());
    }

    private Stream<Person> streamOfficialPersons(final ShootingTestEvent event) {
        return officialRepository.findByShootingTestEvent(event)
                .stream()
                .map(official -> official.getOccupation().getPerson());
    }

    private Stream<Person> streamParticipantPersons(final ShootingTestEvent event) {
        return participantRepository.findByShootingTestEvent(event).stream().map(ShootingTestParticipant::getPerson);
    }
}
