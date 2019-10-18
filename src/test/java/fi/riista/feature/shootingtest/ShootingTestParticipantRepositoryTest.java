package fi.riista.feature.shootingtest;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepositoryCustom.ParticipantSummary;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

import static fi.riista.feature.shootingtest.ShootingTest.ATTEMPT_PRICE;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShootingTestParticipantRepositoryTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestParticipantRepository repo;

    @Test
    public void testGetParticipantSummaryByShootingTestEventId_whenEmptyInput() {
        final Map<Long, ParticipantSummary> result = repo.getParticipantSummaryByShootingTestEventId(emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetParticipantSummaryByShootingTestEventId_withOneParticipantWithNoAttempts() {
        withRhy(rhy -> {
            final ShootingTestEvent event = openEvent(rhy, today());
            model().newShootingTestParticipant(event);

            persistInNewTransaction();

            final Map<Long, ParticipantSummary> expected = ImmutableMap.of(
                    event.getId(), new ParticipantSummary(1, 0, 1, BigDecimal.ZERO));

            assertEquals(expected, repo.getParticipantSummaryByShootingTestEventId(asList(event)));
        });
    }

    @Test
    public void testGetParticipantSummaryByShootingTestEventId_withOneCompletedParticipantHavingOneAttempt() {
        withRhy(rhy -> {
            final ShootingTestEvent event = openEvent(rhy, today());
            createParticipantWithOneAttempt(event);

            persistInNewTransaction();

            final Map<Long, ParticipantSummary> expected = ImmutableMap.of(
                    event.getId(), new ParticipantSummary(1, 1, 0, ShootingTest.ATTEMPT_PRICE));

            assertEquals(expected, repo.getParticipantSummaryByShootingTestEventId(asList(event)));
        });
    }

    @Test
    public void testGetParticipantSummaryByShootingTestEventId_withMultipleParticipants() {
        withRhy(rhy -> withPerson(person -> withPerson(person2 -> withPerson(person3 -> {
            final ShootingTestEvent event = openEvent(rhy, today());

            final ShootingTestParticipant participant = model().newShootingTestParticipant(event, person);
            model().newShootingTestAttempt(participant, MOOSE, UNQUALIFIED);
            model().newShootingTestAttempt(participant, MOOSE, QUALIFIED);
            model().newShootingTestAttempt(participant, BEAR, QUALIFIED);
            completeParticipation(participant, 3, 3);

            createParticipantWithOneAttempt(event, person2);

            model().newShootingTestParticipant(event, person3);

            persistInNewTransaction();

            final Map<Long, ParticipantSummary> expected = ImmutableMap.of(
                    event.getId(), new ParticipantSummary(3, 2, 1, ATTEMPT_PRICE.multiply(new BigDecimal(4))));

            assertEquals(expected, repo.getParticipantSummaryByShootingTestEventId(asList(event)));
        }))));
    }

    @Test
    public void testGetParticipantSummaryByShootingTestEventId_verifyEventFiltering() {
        withRhy(rhy -> {
            final ShootingTestEvent event1 = openEvent(rhy, today());
            createParticipantWithOneAttempt(event1);

            final ShootingTestEvent event2 = openEvent(rhy, today());
            createParticipantWithOneAttempt(event2);

            final ShootingTestEvent event3 = openEvent(rhy, today());
            createParticipantWithOneAttempt(event3);

            persistInNewTransaction();

            final Map<Long, ParticipantSummary> expected = ImmutableMap.of(
                    event1.getId(), new ParticipantSummary(1, 1, 0, ATTEMPT_PRICE),
                    event2.getId(), new ParticipantSummary(1, 1, 0, ATTEMPT_PRICE));

            assertEquals(expected, repo.getParticipantSummaryByShootingTestEventId(asList(event1, event2)));
        });
    }
}
