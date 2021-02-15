package fi.riista.feature.gamediary.observation;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;

import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObservationModifierServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private ObservationModifierService service;

    @Test(expected = RuntimeException.class)
    public void testGetObservationCreatorInfo_unauthenticated() {
        service.getObservationCreatorInfo(today());
    }

    @Test
    public void testGetObservationCreatorInfo_moderator() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final ObservationModifierInfo info = service.getObservationCreatorInfo(today());

            assertTrue(info.isModerator());
            assertFalse(info.isAuthorOrObserver());
            assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        });
    }

    @Test
    public void testGetObservationCreatorInfo_ordinaryPerson() {
        withPerson(person -> {
            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationModifierInfo info = service.getObservationCreatorInfo(today());

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            });
        });
    }

    @Test
    public void testGetObservationCreatorInfo_personHasActiveCarnivoreContactOccupation() {
        withRhy(rhy -> withPerson(person -> {

            model().newOccupation(rhy, person, PETOYHDYSHENKILO);

            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationModifierInfo info = service.getObservationCreatorInfo(today());

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            });
        }));
    }

    @Test
    public void testGetObservationCreatorInfo_personHasExpiredCarnivoreContactOccupation() {
        withRhy(rhy -> withPerson(person -> {

            final LocalDate today = today();
            createExpiredOccupation(person, today);

            onSavedAndAuthenticated(createUser(person), () -> {
                final ObservationModifierInfo info = service.getObservationCreatorInfo(today);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            });
        }));
    }

    @Test(expected = RuntimeException.class)
    public void testGetObservationModifierInfo_unauthenticated() {
        final Observation observation = model().newObservation();

        persistInNewTransaction();

        service.getObservationModifierInfo(observation, today());
    }

    @Test
    public void testGetObservationModifierInfo_moderator() {
        final Observation observation = model().newObservation();

        onSavedAndAuthenticated(createNewModerator(), tx(() -> {
            final ObservationModifierInfo info = service.getObservationModifierInfo(observation, today());

            assertTrue(info.isModerator());
            assertFalse(info.isAuthorOrObserver());
            assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        }));
    }

    @Test
    public void testGetObservationModifierInfo_ordinaryPersonAsAuthor() {
        withPerson(author -> withPerson(observer -> {

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(author), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        }));
    }

    @Test
    public void testGetObservationModifierInfo_ordinaryPersonAsObserver() {
        withPerson(author -> withPerson(observer -> {

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(observer), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        }));
    }

    @Test
    public void testGetObservationModifierInfo_ordinaryPersonNotAuthorNeitherObserver() {
        final Observation observation = model().newObservation();

        onSavedAndAuthenticated(createUser(model().newPerson()), tx(() -> {
            final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
            final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

            assertFalse(info.isModerator());
            assertFalse(info.isAuthorOrObserver());
            assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
        }));
    }

    @Test
    public void testGetObservationModifierInfo_activeCarnivoreContactPersonAsAuthor() {
        withRhy(rhy -> withPerson(author -> withPerson(observer -> {

            model().newOccupation(rhy, author, PETOYHDYSHENKILO);

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(author), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        })));
    }

    @Test
    public void testGetObservationModifierInfo_activeCarnivoreContactPersonAsObserver() {
        withRhy(rhy -> withPerson(author -> withPerson(observer -> {

            model().newOccupation(rhy, observer, PETOYHDYSHENKILO);

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(observer), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        })));
    }

    @Test
    public void testGetObservationModifierInfo_activeCarnivoreContactPersonAsAuthorAndObserver_inPast() {
        final LocalDate today = today();

        withRhy(rhy -> withPerson(author -> {

            final LocalDate observationDate = today.minusDays(5);
            final Observation observation = model().newObservation(model().newGameSpecies(), author, observationDate);

            model().newOccupation(rhy, author, PETOYHDYSHENKILO, today.minusYears(1), observationDate);

            onSavedAndAuthenticated(createUser(author), tx(() -> {
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        }));
    }

    @Test
    public void testGetObservationModifierInfo_activeCarnivoreContactPersonNotAuthorNeitherObserver() {
        withRhy(rhy -> withPerson(carnivorePerson -> {

            model().newOccupation(rhy, carnivorePerson, PETOYHDYSHENKILO);

            final Observation observation = model().newObservation();

            onSavedAndAuthenticated(createUser(carnivorePerson), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertFalse(info.isAuthorOrObserver());
                assertTrue(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        }));
    }

    @Test
    public void testGetObservationModifierInfo_expiredCarnivoreContactPersonAsAuthor() {
        withRhy(rhy -> withPerson(author -> withPerson(observer -> {

            final LocalDate today = today();
            createExpiredOccupation(author, today);

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(author), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        })));
    }

    @Test
    public void testGetObservationModifierInfo_expiredCarnivoreContactPersonAsObserver() {
        withRhy(rhy -> withPerson(author -> withPerson(observer -> {

            final LocalDate today = today();
            createExpiredOccupation(observer, today);

            final Observation observation = model().newObservation(model().newGameSpecies(), author, observer);

            onSavedAndAuthenticated(createUser(observer), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertTrue(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        })));
    }

    @Test
    public void testGetObservationModifierInfo_expiredCarnivoreContactPersonNotAuthorNeitherObserver() {
        withRhy(rhy -> withPerson(carnivorePerson -> {

            final LocalDate today = today();
            createExpiredOccupation(carnivorePerson, today);

            final Observation observation = model().newObservation();

            onSavedAndAuthenticated(createUser(carnivorePerson), tx(() -> {
                final LocalDate observationDate = observation.getPointOfTimeAsLocalDate();
                final ObservationModifierInfo info = service.getObservationModifierInfo(observation, observationDate);

                assertFalse(info.isModerator());
                assertFalse(info.isAuthorOrObserver());
                assertFalse(info.isCarnivoreAuthorityInAnyRhyAtObservationDate());
            }));
        }));
    }

    private void createExpiredOccupation(final Person person, final LocalDate observationDate) {
        model().newOccupation(
                model().newRiistanhoitoyhdistys(),
                person,
                PETOYHDYSHENKILO,
                observationDate.minusYears(1),
                observationDate.minusDays(1));
    }
}
