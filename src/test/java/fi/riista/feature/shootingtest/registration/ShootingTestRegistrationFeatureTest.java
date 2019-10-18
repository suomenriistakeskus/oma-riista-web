package fi.riista.feature.shootingtest.registration;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestFixtureMixin;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.DISQUALIFIED_AS_OFFICIAL;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.FOREIGN_HUNTER;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.HUNTING_PAYMENT_NOT_DONE;
import static fi.riista.feature.shootingtest.registration.ShootingTestRegistrationPersonSearchDTO.ShootingTestRegistrationCheckStatus.NO_HUNTER_NUMBER;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShootingTestRegistrationFeatureTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestRegistrationFeature feature;

    @Resource
    private ShootingTestParticipantRepository participantRepo;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFindFinnishHunterBySsn_hunterNumberRequired() {
        withRhy(rhy -> withPerson(person -> withPerson(officialPerson -> {

            person.setHunterNumber(null);

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final ShootingTestRegistrationPersonSearchDTO result =
                        feature.findFinnishHunterBySsn(event.getId(), person.getSsn());

                assertResultFinnishPerson(person, result);
                assertEquals(NO_HUNTER_NUMBER, result.getRegistrationStatus());
            });
        })));
    }

    @Test
    public void testFindFinnishHunterByHunterNumber_shouldFindFinnishHunter() {
        withRhy(rhy -> withPerson(hunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final ShootingTestRegistrationPersonSearchDTO result =
                        feature.findFinnishHunterByHunterNumber(event.getId(), hunter.getHunterNumber());

                assertResultFinnishPerson(hunter, result);
                assertEquals(HUNTING_PAYMENT_NOT_DONE, result.getRegistrationStatus());
            });
        })));
    }

    @Test
    public void testFindFinnishHunterByHunterNumber_shouldNotReturnForeignHunter() {
        withRhy(rhy -> withForeignPerson(foreignHunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                thrown.expect(PersonNotFoundException.class);
                feature.findFinnishHunterByHunterNumber(event.getId(), foreignHunter.getHunterNumber());
            });
        })));
    }

    @Test
    public void testFindHunterByHunterNumber_shouldFindFinnishHunter() {
        withRhy(rhy -> withPerson(hunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final ShootingTestRegistrationPersonSearchDTO result =
                        feature.findHunterByHunterNumber(event.getId(), hunter.getHunterNumber());

                assertResultFinnishPerson(hunter, result);
                assertEquals(HUNTING_PAYMENT_NOT_DONE, result.getRegistrationStatus());
            });
        })));
    }

    @Test
    public void testFindHunterByHunterNumber_shouldFindForeignHunter() {
        withRhy(rhy -> withForeignPerson(foreignHunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final ShootingTestRegistrationPersonSearchDTO result =
                        feature.findHunterByHunterNumber(event.getId(), foreignHunter.getHunterNumber());

                assertResultForeignPerson(foreignHunter, result);
            });
        })));
    }

    @Test
    public void testFindHunterByHunterNumber_shouldNotReturnDeletedForeignHunter() {
        withRhy(rhy -> withForeignPerson(foreignHunter -> withPerson(officialPerson -> {

            foreignHunter.setDeletionCode(Person.DeletionCode.D);

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {
                thrown.expect(PersonNotFoundException.class);
                feature.findHunterByHunterNumber(event.getId(), foreignHunter.getHunterNumber());
            });
        })));
    }

    @Test
    public void testFindHunterByHunterNumber_officialDisqualified() {
        withRhy(rhy -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                final ShootingTestRegistrationPersonSearchDTO result =
                        feature.findHunterByHunterNumber(event.getId(), officialPerson.getHunterNumber());

                assertResultFinnishPerson(officialPerson, result);
                assertEquals(DISQUALIFIED_AS_OFFICIAL, result.getRegistrationStatus());
            });
        }));
    }

    private static void assertResultFinnishPerson(final Person person,
                                                  final ShootingTestRegistrationPersonSearchDTO result) {
        assertEquals(person.getId(), Long.valueOf(result.getId()));
        assertEquals(person.getFirstName(), result.getFirstName());
        assertEquals(person.getLastName(), result.getLastName());
        assertEquals(person.getHunterNumber(), result.getHunterNumber());
        assertEquals(person.parseDateOfBirth(), result.getDateOfBirth());
        assertFalse(result.isForeignPerson());
    }

    private static void assertResultForeignPerson(final Person person, final ShootingTestRegistrationPersonSearchDTO result) {
        assertEquals(person.getId(), Long.valueOf(result.getId()));
        assertEquals(person.getFirstName(), result.getFirstName());
        assertEquals(person.getLastName(), result.getLastName());
        assertEquals(person.getHunterNumber(), result.getHunterNumber());
        assertEquals(person.getDateOfBirth(), result.getDateOfBirth());
        assertTrue(result.isForeignPerson());
        assertEquals(FOREIGN_HUNTER, result.getRegistrationStatus());
    }

    @Test
    public void testRegisterParticipant_withPersonId() {
        withRhy(rhy -> withPerson(hunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                feature.registerParticipant(event.getId(), hunter.getId(), new SelectedShootingTestTypesDTO());

                runInTransaction(() -> {
                    final List<ShootingTestParticipant> participants = participantRepo.findByShootingTestEvent(event);
                    assertEquals(1, participants.size());

                    final ShootingTestParticipant participant = participants.get(0);
                    assertFalse(participant.isForeignHunter());
                    assertEquals(hunter.getId(), participant.getPerson().getId());
                });
            });
        })));
    }

    @Test
    public void testRegisterParticipant_withHunterNumberOfFinnishPerson() {
        withRhy(rhy -> withPerson(hunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                feature.registerParticipant(event.getId(), RegisterParticipantDTO.create(hunter.getHunterNumber()));

                runInTransaction(() -> {
                    final List<ShootingTestParticipant> participants = participantRepo.findByShootingTestEvent(event);
                    assertEquals(1, participants.size());

                    final ShootingTestParticipant participant = participants.get(0);
                    assertFalse(participant.isForeignHunter());
                    assertEquals(hunter.getId(), participant.getPerson().getId());
                });
            });
        })));
    }

    @Test
    public void testRegisterParticipant_withHunterNumberOfForeignPerson() {
        withRhy(rhy -> withForeignPerson(foreignHunter -> withPerson(officialPerson -> {

            final ShootingTestEvent event = openEvent(rhy, today());
            createOfficial(officialPerson, event);

            onSavedAndAuthenticated(createUser(officialPerson), () -> {

                feature.registerParticipant(
                        event.getId(), RegisterParticipantDTO.create(foreignHunter.getHunterNumber()));

                runInTransaction(() -> {
                    final List<ShootingTestParticipant> participants = participantRepo.findByShootingTestEvent(event);
                    assertEquals(1, participants.size());

                    final ShootingTestParticipant participant = participants.get(0);
                    assertTrue(participant.isForeignHunter());
                    assertEquals(foreignHunter.getId(), participant.getPerson().getId());
                });
            });
        })));
    }
}
