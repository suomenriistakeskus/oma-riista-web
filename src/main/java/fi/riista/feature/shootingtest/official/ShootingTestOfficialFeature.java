package fi.riista.feature.shootingtest.official;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission;
import fi.riista.feature.shootingtest.ParticipantAsOfficialException;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepository;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.shootingtest.ShootingTestEventAuthorization.ShootingTestEventPermission.ASSIGN_OFFICIALS;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.util.Collect.greatestAfterGroupingBy;
import static fi.riista.util.Collect.idSet;
import static java.util.stream.Collectors.toList;

@Service
public class ShootingTestOfficialFeature {

    @Resource
    private ShootingTestOfficialRepository officialRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ShootingTestOfficialService officialService;

    @Resource
    private ShootingTestOfficialOccupationDTOTransformer officialOccupationDTOTransformer;

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listAvailableOfficials(final long rhyId, final LocalDate eventDate) {
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyId, RhyPermission.VIEW_SHOOTING_TEST_EVENTS);

        return officialOccupationDTOTransformer.apply(findAvailableOfficialOccupations(rhy, eventDate));
    }

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listQualifyingOfficials(final long eventId) {
        return officialOccupationDTOTransformer.apply(findQualifyingOfficialOccupations(getEvent(eventId, READ)));
    }

    @Transactional(readOnly = true)
    public List<ShootingTestOfficialOccupationDTO> listAssignedOfficials(final long eventId) {
        final ShootingTestEvent event = getEvent(eventId, READ);

        final List<ShootingTestOfficial> officials = officialRepository
                .findByShootingTestEvent(event);
        final List<Occupation> officialOccupations = officials
                .stream()
                .sorted(Comparator.comparing(ShootingTestOfficial::getShootingTestResponsible, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ShootingTestOfficial::getOccupation)
                .collect(toList());

        List<ShootingTestOfficialOccupationDTO> officialOccupationDTOS =
                officialOccupationDTOTransformer.apply(officialOccupations);

        return officialOccupationDTOS;
    }

    @Transactional
    public void assignOfficials(final ShootingTestOfficialsDTO dto) {
        final ShootingTestEvent event = getEvent(dto.getShootingTestEventId(), ASSIGN_OFFICIALS);
        event.assertOpen("Cannot assign officials after event was closed");

        officialRepository.deleteInBatch(officialRepository.findByShootingTestEvent(event));
        final List<ShootingTestOfficial> newOfficials = officialService.assignOfficials(dto, event);

        final Set<Long> participantPersonIds = streamParticipantPersons(event).collect(idSet());

        final boolean anyOfficialAsParticipant = newOfficials.stream().anyMatch(official -> {
            return participantPersonIds.contains(official.getOccupation().getPerson().getId());
        });

        if (anyOfficialAsParticipant) {
            throw new ParticipantAsOfficialException();
        }
    }

    private ShootingTestEvent getEvent(final long eventId, final Enum<?> permission) {
        return requireEntityService.requireShootingTestEvent(eventId, permission);
    }

    private List<Occupation> findAvailableOfficialOccupations(final Organisation rhy, final LocalDate eventDate) {
        return occupationRepository
                .findActiveByOrganisationAndOccupationTypeAndDate(rhy, AMPUMAKOKEEN_VASTAANOTTAJA, eventDate)
                .stream()
                // Collect latest occupation for each person having multiple shooting test official occupations.
                .collect(greatestAfterGroupingBy(occ -> occ.getPerson().getId(), HasBeginAndEndDate.DEFAULT_COMPARATOR))
                .values()
                .stream()
                .sorted(OccupationSort.BY_FULL_NAME)
                .collect(toList());
    }

    private List<Occupation> findQualifyingOfficialOccupations(final ShootingTestEvent event) {
        final CalendarEvent calendarEvent = event.getCalendarEvent();
        final Organisation organisation = calendarEvent.getOrganisation();

        final Set<Long> participantPersonIds = streamParticipantPersons(event).collect(idSet());

        return findAvailableOfficialOccupations(organisation, calendarEvent.getDateAsLocalDate())
                .stream()
                .filter(occupation -> !participantPersonIds.contains(occupation.getPerson().getId()))
                .collect(toList());
    }

    private Stream<Person> streamParticipantPersons(final ShootingTestEvent event) {
        return participantRepository
                .findByShootingTestEvent(event)
                .stream()
                .filter(participant -> !participant.isForeignHunter())
                .map(ShootingTestParticipant::getPerson);
    }
}
