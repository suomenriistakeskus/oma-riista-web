package fi.riista.feature.shootingtest;

import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestAttemptResult;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestType;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.BOW;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static fi.riista.feature.shootingtest.ShootingTestType.ROE_DEER;

public interface ShootingTestFixtureMixin extends OrganisationFixtureMixin {

    default ShootingTestEvent openEvent(final Riistanhoitoyhdistys rhy, final LocalDate date) {
        final ShootingTestEvent event = getEntitySupplier().newShootingTestEvent(rhy, date);

        final Person officialPerson1 = getEntitySupplier().newPerson();
        officialPerson1.setRhyMembership(rhy);
        getEntitySupplier().newShootingTestOfficial(event, officialPerson1);

        final Person officialPerson2 = getEntitySupplier().newPerson();
        officialPerson2.setRhyMembership(rhy);
        getEntitySupplier().newShootingTestOfficial(event, officialPerson2);

        return event;
    }

    default ShootingTestAttempt createParticipantWithOneAttempt(final ShootingTestEvent event) {
        return createParticipantWithOneAttempt(event, getEntitySupplier().newPerson());
    }

    default ShootingTestAttempt createParticipantWithOneAttempt(final ShootingTestEvent event, final Person person) {

        return createParticipantWithOneAttempt(event, person, MOOSE, QUALIFIED);
    }

    default ShootingTestAttempt createParticipantWithOneAttempt(final ShootingTestEvent event,
                                                                final Person person,
                                                                final ShootingTestType type) {

        return createParticipantWithOneAttempt(event, person, type, QUALIFIED);
    }

    default ShootingTestAttempt createParticipantWithOneAttempt(final ShootingTestEvent event,
                                                                final Person person,
                                                                final ShootingTestType type,
                                                                final ShootingTestAttemptResult result) {

        final ShootingTestParticipant participant = getEntitySupplier().newShootingTestParticipant(event, person);
        final ShootingTestAttempt attempt = getEntitySupplier().newShootingTestAttempt(participant, type, result);
        completeParticipation(participant, 1, 1);
        return attempt;
    }

    default List<ShootingTestAttempt> createShootingTestAttemptsForSmokeTestCase(final LocalDate date) {
        final List<ShootingTestAttempt> result = new ArrayList<>();

        withRhy(rhy -> withRhy(rhy2 -> withPerson(person1 -> withPerson(person2 -> withPerson(person3 -> {

            final ShootingTestEvent event1 = openEvent(rhy, date);
            final ShootingTestAttempt attempt1 = createParticipantWithOneAttempt(event1, person1, BEAR);

            final ShootingTestEvent event2 = openEvent(rhy2, date.minusDays(1));
            final ShootingTestParticipant participant2 = getEntitySupplier().newShootingTestParticipant(event1, person2);
            getEntitySupplier().newShootingTestAttempt(participant2, UNQUALIFIED);
            final ShootingTestAttempt attempt2 = getEntitySupplier().newShootingTestAttempt(participant2, QUALIFIED);
            completeParticipation(participant2, 2, 2);

            final ShootingTestAttempt attempt3 = createParticipantWithOneAttempt(event2, person3, ROE_DEER);

            final ShootingTestEvent event3 = openEvent(rhy2, date.minusDays(2));
            final ShootingTestAttempt attempt4 = createParticipantWithOneAttempt(event3, person3, BOW);

            Stream.of(event1, event2, event3).forEach(ShootingTestEvent::close);
            Stream.of(attempt1, attempt2, attempt3, attempt4).forEach(result::add);
        })))));

        return result;
    }

    default void completeParticipation(final ShootingTestParticipant participant,
                                       final int totalAttempts,
                                       final int paidAttempts) {

        participant.updateTotalDueAmount(totalAttempts);
        participant.updatePaymentState(paidAttempts, true);
    }
}
