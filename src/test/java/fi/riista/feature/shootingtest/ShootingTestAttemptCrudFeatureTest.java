package fi.riista.feature.shootingtest;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.REBATED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.TIMED_OUT;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BEAR;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ShootingTestAttemptCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private ShootingTestAttemptCrudFeature feature;

    @Resource
    private ShootingTestAttemptRepository attemptRepository;

    @Resource
    private ShootingTestParticipantRepository participantRepository;

    @Test(expected = TooManyAttemptsException.class)
    public void testCreate_attemptCountLimitedByType() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            for (int i = 0; i < ShootingTest.MAX_ATTEMPTS_PER_TYPE; i++) {
                for (ShootingTestType t : ShootingTestType.values()) {
                    testCreate(participant, t, UNQUALIFIED);
                }
            }

            // adding any other attempt should fail
            testCreate(participant, MOOSE, UNQUALIFIED);
            fail("Should have failed to 6th attempt of same type");
        });
    }

    @Test
    public void testCreate_attemptCountNotLimitedByRebated() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();

        for (int i = 0; i < ShootingTest.MAX_ATTEMPTS_PER_TYPE - 1; i++) {
            model().newShootingTestAttempt(participant, UNQUALIFIED);
        }
        model().newShootingTestAttempt(participant, REBATED);
        model().newShootingTestAttempt(participant, REBATED);

        onSavedAndAuthenticated(createNewModerator(), () -> testCreate(participant, MOOSE, UNQUALIFIED));
    }

    @Test
    public void testCreate_attemptCountAffectsTotalDueAmount() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();

        onSavedAndAuthenticated(createNewModerator(), () -> {

            testCreate(participant, MOOSE, UNQUALIFIED);
            assertTotalDueAmount(20.0, participant);

            testCreate(participant, MOOSE, REBATED);
            assertTotalDueAmount(20.0, participant);

            testCreate(participant, MOOSE, TIMED_OUT);
            assertTotalDueAmount(40.0, participant);

            testCreate(participant, MOOSE, REBATED);
            assertTotalDueAmount(40.0, participant);

            testCreate(participant, MOOSE, QUALIFIED);
            assertTotalDueAmount(60.0, participant);
        });
    }

    @Test(expected = TooManyAttemptsException.class)
    public void testUpdate_attemptCountLimitedByType() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();

        for (int i = 0; i < ShootingTest.MAX_ATTEMPTS_PER_TYPE; i++) {
            model().newShootingTestAttempt(participant, UNQUALIFIED);
        }

        final ShootingTestAttempt attempt = model().newShootingTestAttempt(participant, QUALIFIED);
        attempt.setType(BEAR);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            testUpdate(attempt, MOOSE, QUALIFIED);
            fail("Should have failed to 6th attempt of same type");
        });
    }

    @Test
    public void testUpdate_attemptCountAffectsTotalDueAmount() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();
        final ShootingTestAttempt attempt = model().newShootingTestAttempt(participant, REBATED);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            assertTotalDueAmount(0.0, participant);

            testUpdate(attempt, MOOSE, TIMED_OUT);
            assertTotalDueAmount(20.0, participant);

            testUpdate(attempt, MOOSE, UNQUALIFIED);
            assertTotalDueAmount(20.0, participant);

            testUpdate(attempt, MOOSE, QUALIFIED);
            assertTotalDueAmount(20.0, participant);
        });
    }

    @Test
    public void testDelete_attemptCountAffectsTotalDueAmount() {
        final ShootingTestParticipant participant = model().newShootingTestParticipant();

        final ShootingTestAttempt rebated = model().newShootingTestAttempt(participant, REBATED);
        final ShootingTestAttempt unqualified = model().newShootingTestAttempt(participant, UNQUALIFIED);
        final ShootingTestAttempt qualified = model().newShootingTestAttempt(participant, QUALIFIED);

        participant.updateTotalDueAmount(2);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            assertTotalDueAmount(40.0, participant);

            feature.delete(rebated.getId());
            assertTotalDueAmount(40.0, participant);

            feature.delete(qualified.getId());
            assertTotalDueAmount(20.0, participant);

            assertEquals(singleton(unqualified.getId()), F.getUniqueIds(attemptRepository.findAll()));
        });
    }

    private void testCreate(final ShootingTestParticipant participant,
                            final ShootingTestType type,
                            final ShootingTestAttemptResult result) {

        feature.create(createAddDTO(participant, type, result));
    }

    private void testUpdate(final ShootingTestAttempt attempt,
                            final ShootingTestType type,
                            final ShootingTestAttemptResult result) {

        feature.update(createUpdateDTO(attempt, type, result));
    }

    private ShootingTestAttemptDTO createAddDTO(final ShootingTestParticipant participant,
                                                final ShootingTestType type,
                                                final ShootingTestAttemptResult result) {

        return callInTransaction(() -> {
            final ShootingTestParticipant reloadedParticipant = participantRepository.getOne(participant.getId());

            final ShootingTestAttemptDTO dto = new ShootingTestAttemptDTO();
            dto.setParticipantId(reloadedParticipant.getId());
            dto.setParticipantRev(reloadedParticipant.getConsistencyVersion());
            mutate(dto, type, result);
            return dto;
        });
    }

    private ShootingTestAttemptDTO createUpdateDTO(final ShootingTestAttempt attempt,
                                                   final ShootingTestType type,
                                                   final ShootingTestAttemptResult result) {

        return callInTransaction(() -> {
            final ShootingTestAttempt reloaded = attemptRepository.getOne(attempt.getId());
            final ShootingTestAttemptDTO dto = ShootingTestAttemptDTO.create(reloaded, reloaded.getParticipant());
            mutate(dto, type, result);
            return dto;
        });
    }

    private static void mutate(final ShootingTestAttemptDTO dto,
                               final ShootingTestType type,
                               final ShootingTestAttemptResult result) {

        dto.setType(type);
        dto.setResult(result);
        dto.setHits(type.getNumberOfHitsToQualify() - (result == QUALIFIED ? 0 : 1));
    }

    private void assertTotalDueAmount(final double expected, final ShootingTestParticipant participant) {
        final BigDecimal totalDueAmount = participantRepository.findById(participant.getId()).map(ShootingTestParticipant::getTotalDueAmountOrZero).orElse(null);
        assertTrue(BigDecimal.valueOf(expected).compareTo(totalDueAmount) == 0);
    }
}
