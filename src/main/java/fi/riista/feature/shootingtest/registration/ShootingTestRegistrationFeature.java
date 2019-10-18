package fi.riista.feature.shootingtest.registration;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.shootingtest.ParticipantAsOfficialException;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepository;
import fi.riista.feature.shootingtest.official.ShootingTestOfficial;
import fi.riista.feature.shootingtest.official.ShootingTestOfficialRepository;
import fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.COMPLETED;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.DISQUALIFIED_AS_OFFICIAL;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.FOREIGN_HUNTER;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.HUNTING_BAN;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.HUNTING_PAYMENT_DONE;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.HUNTING_PAYMENT_NOT_DONE;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.IN_PROGRESS;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.NO_HUNTER_NUMBER;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.Collect.idSet;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.springframework.util.StringUtils.hasText;

@Service
public class ShootingTestRegistrationFeature {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private ShootingTestOfficialRepository officialRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PersonLookupService personLookupService;

    @Transactional(readOnly = true)
    public ShootingTestRegistrationPersonSearchDTO findFinnishHunterBySsn(final long shootingTestEventId,
                                                                          final String ssn) {
        checkArgument(hasText(ssn), "empty ssn");

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        return personLookupService
                .findBySsnNoFallback(ssn)
                .map(person -> constructRegistrationInfo(person, event))
                .orElseThrow(() -> PersonNotFoundException.bySsn(ssn));
    }

    // TODO Will be removed after an adequate base of mobile app installations is capable of adding foreign hunters.
    @Transactional(readOnly = true)
    public ShootingTestRegistrationPersonSearchDTO findFinnishHunterByHunterNumber(final long shootingTestEventId,
                                                                                   final String hunterNumber) {
        checkArgument(hasText(hunterNumber), "empty hunterNumber");

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        return personLookupService
                .findByHunterNumber(hunterNumber, false)
                .map(person -> constructRegistrationInfo(person, event))
                .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
    }

    @Transactional(readOnly = true)
    public ShootingTestRegistrationPersonSearchDTO findHunterByHunterNumber(final long shootingTestEventId,
                                                                            final String hunterNumber) {
        checkArgument(hasText(hunterNumber), "empty hunterNumber");

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        return findHunterAndTransform(
                hunterNumber, p -> constructRegistrationInfo(p, event));
    }

    private ShootingTestRegistrationPersonSearchDTO constructRegistrationInfo(final Person person,
                                                                              final ShootingTestEvent event) {

        final Set<Long> officialPersonIds = streamOfficialPersons(event).collect(idSet());

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
                        if (!person.hasHunterNumber()) {
                            return NO_HUNTER_NUMBER;
                        }
                        if (person.isForeignPerson()) {
                            return FOREIGN_HUNTER;
                        }
                        return person.getHuntingPaymentDateForNextOrCurrentSeason().isPresent()
                                ? HUNTING_PAYMENT_DONE
                                : HUNTING_PAYMENT_NOT_DONE;
                    });
        }

        return new ShootingTestRegistrationPersonSearchDTO(person, registrationStatus,
                getTestTypes(existingParticipant));
    }


    private static SelectedShootingTestTypesDTO getTestTypes(final Optional<ShootingTestParticipant> participant) {
        return participant
                .map(SelectedShootingTestTypesDTO::create)
                .orElseGet(SelectedShootingTestTypesDTO::new);
    }

    // TODO Will be removed after an adequate base of mobile app installations is capable of adding foreign hunters.
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

        final ShootingTestParticipant participant = participantRepository
                .findByShootingTestEventAndPerson(event, person)
                .map(ShootingTestRegistrationFeature::registerParticipantAgain)
                .orElseGet(() -> createParticipant(person, event));

        setTestTypes(participant, selectedTypes);

        participantRepository.save(participant);
    }

    @Transactional
    public void registerParticipant(final long shootingTestEventId,
                                    @Nonnull final RegisterParticipantDTO registration) {

        requireNonNull(registration);

        final ShootingTestEvent event = getEvent(shootingTestEventId, UPDATE);

        event.assertOpen("Cannot register participant after event was closed");

        final ShootingTestParticipant participant = findHunterAndTransform(registration.getHunterNumber(),
                person -> {

                    if (streamOfficialPersons(event).anyMatch(person::equals)) {
                        throw new ParticipantAsOfficialException();
                    }

                    return participantRepository
                            .findByShootingTestEventAndPerson(event, person)
                            .map(ShootingTestRegistrationFeature::registerParticipantAgain)
                            .orElseGet(() -> createParticipant(person, event));
                });

        setTestTypes(participant, registration.getSelectedTypes());

        participantRepository.save(participant);
    }

    private static ShootingTestParticipant createParticipant(final Person person, final ShootingTestEvent event) {
        if (person.isHuntingBanActiveNow()) {
            cannotRegister(format("Person (id=%d) has active hunting ban", person.getId()));
        }
        if (!person.hasHunterNumber()) {
            cannotRegister(format("Person (id=%d) does not have hunter number", person.getId()));
        }

        return new ShootingTestParticipant(event, person);
    }

    private static ShootingTestParticipant registerParticipantAgain(final ShootingTestParticipant participant) {
        if (!participant.isCompleted()) {
            final long eventId = participant.getShootingTestEvent().getId();

            cannotRegister(format("Person (id=%d) has already registered into shooting test event (id=%d)",
                    participant.getPerson().getId(), eventId));
        }

        participant.registerAgain();
        return participant;
    }

    private static void setTestTypes(final ShootingTestParticipant participant,
                                     final SelectedShootingTestTypesDTO testTypes) {

        participant.setMooseTestIntended(testTypes.isMooseTestIntended());
        participant.setBearTestIntended(testTypes.isBearTestIntended());
        participant.setDeerTestIntended(testTypes.isRoeDeerTestIntended());
        participant.setBowTestIntended(testTypes.isBowTestIntended());
    }

    private static void cannotRegister(final String message) {
        throw new CannotRegisterShootingTestParticipantException(message);
    }

    private ShootingTestEvent getEvent(final long eventId, final Enum<?> permission) {
        return requireEntityService.requireShootingTestEvent(eventId, permission);
    }

    private Stream<Person> streamOfficialPersons(final ShootingTestEvent event) {
        return officialRepository.findByShootingTestEvent(event)
                .stream()
                .map(ShootingTestOfficial::getOccupation)
                .map(Occupation::getPerson);
    }

    private <T> T findHunterAndTransform(final String hunterNumber,
                                         final Function<? super Person, T> personMapper) {

        return personLookupService
                .findByHunterNumber(hunterNumber, true)
                .map(personMapper)
                .orElseThrow(() -> PersonNotFoundException.byHunterNumber(hunterNumber));
    }
}
