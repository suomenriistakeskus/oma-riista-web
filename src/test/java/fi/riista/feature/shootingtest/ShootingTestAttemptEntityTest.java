package fi.riista.feature.shootingtest;

import org.junit.Test;

import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.QUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestAttemptResult.UNQUALIFIED;
import static fi.riista.feature.shootingtest.ShootingTestType.BOW;
import static fi.riista.feature.shootingtest.ShootingTestType.MOOSE;
import static org.junit.Assert.assertEquals;

public class ShootingTestAttemptEntityTest {

    @Test
    public void testHitCountForPassedMoose() {
        testHitCountForPassed(MOOSE, 0, false);
        testHitCountForPassed(MOOSE, 1, false);
        testHitCountForPassed(MOOSE, 2, false);
        testHitCountForPassed(MOOSE, 3, false);
        testHitCountForPassed(MOOSE, 4, true);
    }

    @Test
    public void testHitCountForPassedBow() {
        testHitCountForPassed(BOW, 0, false);
        testHitCountForPassed(BOW, 1, false);
        testHitCountForPassed(BOW, 2, false);
        testHitCountForPassed(BOW, 3, true);
        testHitCountForPassed(BOW, 4, true);
    }

    private static void testHitCountForPassed(final ShootingTestType type, final int hits, final boolean ok) {
        assertEquals(ok, newShootingTestAttempt(type, QUALIFIED, hits, null).isHitCountConsistentWithQualified());
    }

    @Test
    public void testHitCountForFailedMoose() {
        testHitCountForFailed(MOOSE, 0, true);
        testHitCountForFailed(MOOSE, 1, true);
        testHitCountForFailed(MOOSE, 2, true);
        testHitCountForFailed(MOOSE, 3, true);
        testHitCountForFailed(MOOSE, 4, false);
    }

    @Test
    public void testHitCountForFailedBow() {
        testHitCountForFailed(BOW, 0, true);
        testHitCountForFailed(BOW, 1, true);
        testHitCountForFailed(BOW, 2, true);
        testHitCountForFailed(BOW, 3, false);
        testHitCountForFailed(BOW, 4, false);
    }

    private static void testHitCountForFailed(final ShootingTestType type, final int hits, final boolean ok) {
        assertEquals(ok, newShootingTestAttempt(type, UNQUALIFIED, hits, null).isHitCountConsistentWithUnqualified());
    }

    private static ShootingTestAttempt newShootingTestAttempt(final ShootingTestType type,
                                                              final ShootingTestAttemptResult result,
                                                              final int hits,
                                                              final String note) {

        final ShootingTestAttempt attempt = new ShootingTestAttempt();
        attempt.setType(type);
        attempt.setResult(result);
        attempt.setHits(hits);
        attempt.setNote(note);
        return attempt;
    }
}
