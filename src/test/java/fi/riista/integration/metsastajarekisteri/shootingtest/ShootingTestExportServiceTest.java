package fi.riista.integration.metsastajarekisteri.shootingtest;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.shootingtest.ShootingTest;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestFixtureMixin;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.REBATED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.TIMED_OUT;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.BOW;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static fi.riista.feature.shootingtest.ShootingTestType.ROE_DEER;
import static fi.riista.integration.metsastajarekisteri.shootingtest.ShootingTestExportQueries.MR_SHOOTING_TEST_ORDERING;
import static fi.riista.util.DateUtil.today;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ShootingTestExportServiceTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestExportService service;

    @Resource
    private ShootingTestExportTestHelper helper;

    @PostConstruct
    public void init() {
        // Set batch size lower than default in order to test integrity of batch aggregation.
        service.setBatchSize(3);
    }

    @Test
    public void testExportShootingTestData_whenEventsNotPresent() {
        withRhy(rhy -> {
            persistInNewTransaction();
            assertResult(today());
        });
    }

    @Test
    public void testExportShootingTestData_smokeTest() {
        final LocalDate today = today();

        final List<ShootingTestAttempt> attempts = IntStream
                .rangeClosed(0, 5)
                .mapToObj(today::minusWeeks)
                .map(this::createShootingTestAttemptsForSmokeTestCase)
                .flatMap(List::stream)
                .collect(toList());

        persistInNewTransaction();
        assertResult(today, attempts);
    }

    @Test
    public void testExportShootingTestData_verifyingEventsFilteredByDate() {
        final LocalDate reportDate = today().minusDays(1);
        final LocalDate firstValidDateForReportDate = reportDate.minusYears(3);

        withRhy(rhy -> {

            final ShootingTestEvent event1 = openEvent(rhy, firstValidDateForReportDate.minusDays(1));
            createParticipantWithOneAttempt(event1);

            final ShootingTestEvent event2 = openEvent(rhy, firstValidDateForReportDate);
            final ShootingTestAttempt attempt2 = createParticipantWithOneAttempt(event2);

            final ShootingTestEvent event3 = openEvent(rhy, reportDate);
            final ShootingTestAttempt attempt3 = createParticipantWithOneAttempt(event3);

            final ShootingTestEvent event4 = openEvent(rhy, reportDate.plusDays(1));
            createParticipantWithOneAttempt(event4);

            Stream.of(event1, event2, event3, event4).forEach(ShootingTestEvent::close);

            persistInNewTransaction();
            assertResult(reportDate, attempt2, attempt3);
        });
    }

    @Test
    public void testExportShootingTestData_verifyingAlsoIncompletedEventsIncluded() {
        final LocalDate today = today();

        withRhy(rhy -> {

            final ShootingTestEvent event1 = openEvent(rhy, today.minusDays(1));
            final ShootingTestAttempt incompletedAttempt = createParticipantWithOneAttempt(event1);
            // event1 not closed

            final ShootingTestEvent event2 = openEvent(rhy, today.minusDays(2));
            final ShootingTestAttempt completedAttempt = createParticipantWithOneAttempt(event2);
            event2.close();

            persistInNewTransaction();
            assertResult(today, incompletedAttempt, completedAttempt);
        });
    }

    @Test
    public void testExportShootingTestData_verifyingOnlyQualifiedAttemptsIncluded() {
        final LocalDate today = today();

        withRhy(rhy -> withPerson(person1 -> withPerson(person2 -> withPerson(person3 -> withPerson(person4 -> {

            final ShootingTestEvent event1 = openEvent(rhy, today.minusDays(1));
            final ShootingTestParticipant participant1 = model().newShootingTestParticipant(event1, person1);
            model().newShootingTestAttempt(participant1, MOOSE, REBATED);
            model().newShootingTestAttempt(participant1, MOOSE, TIMED_OUT);
            model().newShootingTestAttempt(participant1, MOOSE, UNQUALIFIED);
            final ShootingTestAttempt mooseQualified = model().newShootingTestAttempt(participant1, MOOSE, QUALIFIED);
            completeParticipation(participant1, 3, 3);

            final ShootingTestEvent event2 = openEvent(rhy, today.minusDays(2));
            createParticipantWithOneAttempt(event2, person2, BEAR, TIMED_OUT);

            final ShootingTestEvent event3 = openEvent(rhy, today.minusDays(3));
            createParticipantWithOneAttempt(event3, person3, ROE_DEER, UNQUALIFIED);

            final ShootingTestEvent event4 = openEvent(rhy, today.minusDays(4));
            model().newShootingTestParticipant(event4, model().newPerson());
            // No any attempt added into event4.

            Stream.of(event1, event2, event3, event4).forEach(ShootingTestEvent::close);

            persistInNewTransaction();
            assertResult(today, mooseQualified);
        })))));
    }

    @Test
    public void testExportShootingTestData_verifyingOnlyFullyPaidParticipantsIncluded() {
        final LocalDate today = today();

        withRhy(rhy -> withPerson(person -> {

            final ShootingTestEvent event1 = openEvent(rhy, today.minusDays(1));
            final ShootingTestParticipant participant = model().newShootingTestParticipant(event1, person);
            model().newShootingTestAttempt(participant, MOOSE, UNQUALIFIED);
            model().newShootingTestAttempt(participant, MOOSE, QUALIFIED);
            completeParticipation(participant, 2, 1);
            event1.close();

            final ShootingTestEvent event2 = openEvent(rhy, today.minusDays(2));
            final ShootingTestAttempt attempt = createParticipantWithOneAttempt(event2, person, ROE_DEER, QUALIFIED);
            event2.close();

            persistInNewTransaction();
            assertResult(today, attempt);
        }));
    }

    @Test
    public void testExportShootingTestData_verifyingOnlyMostRecentTestIncludedForEachType() {
        final LocalDate today = today();

        withRhy(rhy -> withRhy(rhy2 -> withPerson(person1 -> withPerson(person2 -> {

            final ShootingTestEvent event1 = openEvent(rhy, today.minusDays(1));
            final ShootingTestAttempt attempt1 = createParticipantWithOneAttempt(event1, person1, MOOSE, QUALIFIED);

            final ShootingTestEvent event2 = openEvent(rhy2, today.minusDays(2));
            final ShootingTestParticipant participant2 = model().newShootingTestParticipant(event2, person2);
            final ShootingTestAttempt attempt2 = model().newShootingTestAttempt(participant2, MOOSE);
            final ShootingTestAttempt attempt3 = model().newShootingTestAttempt(participant2, BEAR);
            final ShootingTestAttempt attempt4 = model().newShootingTestAttempt(participant2, ROE_DEER);
            final ShootingTestAttempt attempt5 = model().newShootingTestAttempt(participant2, BOW);
            completeParticipation(participant2, 4, 4);

            // The following events, participants and tests should not appear in result.

            final ShootingTestEvent event3 = openEvent(rhy, today.minusDays(3));
            final ShootingTestParticipant participant3 = model().newShootingTestParticipant(event3, person2);
            model().newShootingTestAttempt(participant3, MOOSE);
            model().newShootingTestAttempt(participant3, BEAR);
            model().newShootingTestAttempt(participant3, ROE_DEER);
            model().newShootingTestAttempt(participant3, BOW);
            completeParticipation(participant3, 4, 4);

            final ShootingTestEvent event4 = openEvent(rhy, today.minusDays(4));
            createParticipantWithOneAttempt(event4, person1, MOOSE);

            final ShootingTestEvent event5 = openEvent(rhy2, today.minusDays(5));
            createParticipantWithOneAttempt(event5, person1, MOOSE);

            Stream.of(event1, event2, event3, event4, event5).forEach(ShootingTestEvent::close);

            persistInNewTransaction();
            assertResult(today, attempt1, attempt2, attempt3, attempt4, attempt5);

        }))));
    }

    @Test
    public void testExportShootingTestData_verifyingBothFinnishAndForeignHuntersIncluded() {
        final LocalDate today = today();

        withRhy(rhy -> {

            final ShootingTestEvent event1 = openEvent(rhy, today);
            final ShootingTestAttempt attempt1 = createParticipantWithOneAttempt(event1);
            final ShootingTestAttempt attempt2 = createForeignParticipantWithOneAttempt(event1);

            final ShootingTestEvent event2 = openEvent(rhy, today.minusDays(1));
            final ShootingTestAttempt attempt3 = createParticipantWithOneAttempt(event2);

            final ShootingTestEvent event3 = openEvent(rhy, today.minusDays(2));
            final ShootingTestAttempt attempt4 = createForeignParticipantWithOneAttempt(event3);

            Stream.of(event1, event2, event3).forEach(ShootingTestEvent::close);

            persistInNewTransaction();
            assertResult(today, attempt1, attempt2, attempt3, attempt4);
        });
    }

    private static List<MR_Person> createExpectedPersonList(final List<ShootingTestAttempt> attempts) {
        final Map<String, List<MR_ShootingTest>> map = attempts
                .stream()
                .collect(groupingBy(
                        attempt -> {
                            final ShootingTestParticipant participant = attempt.getParticipant();

                            return participant.getPerson().getHunterNumber();
                        },
                        mapping(attempt -> {

                            final ShootingTestParticipant participant = attempt.getParticipant();
                            final ShootingTestEvent event = participant.getShootingTestEvent();
                            final CalendarEvent calEvent = event.getCalendarEvent();

                            final LocalDate validityBegin = calEvent.getDateAsLocalDate();

                            return new MR_ShootingTest()
                                    .withType(attempt.getType().toExportType())
                                    .withValidityBegin(validityBegin)
                                    .withValidityEnd(validityBegin.plus(ShootingTest.VALIDITY_PERIOD))
                                    .withRHY(calEvent.getOrganisation().getOfficialCode())
                                    .withEventId(event.getId())
                                    .withParticipantId(participant.getId())
                                    .withExecutionId(attempt.getId());

                        }, toList())));

        return map.entrySet()
                .stream()
                .map(entry -> {
                    return new MR_Person()
                            .withHunterNumber(entry.getKey())
                            .withValidTests(new MR_ShootingTestList().withShootingTest(entry
                                    .getValue()
                                    .stream()
                                    .sorted(MR_SHOOTING_TEST_ORDERING)
                                    .collect(toList())));
                })
                .sorted(comparing(MR_Person::getHunterNumber))
                .collect(toList());
    }

    private void assertResult(final LocalDate registerDate, final ShootingTestAttempt... attempts) {
        assertResult(registerDate, Arrays.asList(attempts));
    }

    private void assertResult(final LocalDate registerDate, final List<ShootingTestAttempt> attempts) {
        assertEquals(createExpectedPersonList(attempts), createXmlAndUnmarshal(registerDate).getPersons().getPerson());
    }

    private MR_ShootingTestRegistry createXmlAndUnmarshal(final LocalDate registerDate) {
        return callInTransaction(() -> {
            try {
                final byte[] xmlBytes = service.exportShootingTestData(registerDate);

                final MR_ShootingTestRegistry result = helper.unmarshal(xmlBytes);
                assertEquals(registerDate, result.getRegisterDate());
                return result;

            } catch (final IOException | XMLStreamException e) {
                throw new RuntimeException("Failed on unmarshalling XML", e);
            }
        });
    }
}
