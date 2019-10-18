package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummaryLockingCondition.builder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MooseHuntingSummaryLockingConditionTest {

    @Test
    public void testPermitHolderFinishedHunting() {
        final boolean locked = builder()
                .withPermitHolderFinishedHunting(returning(true))
                .withActionRequestedByModerator(shouldNotBeEvaluated("moderator"))
                .withDataOriginatingFromMooseDataCard(shouldNotBeEvaluated("moose data card"))
                .withPermitLockDatePassed(shouldNotBeEvaluated("permit lock date"))
                .withUserHavingOccupationGrantingPermission(shouldNotBeEvaluated("occupation"))
                .build()
                .isLocked();

        assertTrue(locked);
    }

    @Test
    public void testPermitHolderFinishedHunting_isModerator() {
        final Predicate<Boolean> isLockedWhenOccupationGrantsUpdatePermission = truthValue -> builder()
                .withUserHavingOccupationGrantingPermission(returning(truthValue))
                .withPermitHolderFinishedHunting(returning(false))
                .withActionRequestedByModerator(returning(true))
                .withDataOriginatingFromMooseDataCard(shouldNotBeEvaluated("moose data card"))
                .withPermitLockDatePassed(shouldNotBeEvaluated("permit lock date"))
                .build()
                .isLocked();

        assertTrue(isLockedWhenOccupationGrantsUpdatePermission.test(false));
        assertFalse(isLockedWhenOccupationGrantsUpdatePermission.test(true));
    }

    @Test
    public void testPermitHolderNotFinishedHunting_notModerator_mooseDataCardPresent() {
        final boolean locked = builder()
                .withPermitHolderFinishedHunting(returning(false))
                .withActionRequestedByModerator(returning(false))
                .withDataOriginatingFromMooseDataCard(returning(true))
                .withPermitLockDatePassed(shouldNotBeEvaluated("permit lock date"))
                .withUserHavingOccupationGrantingPermission(shouldNotBeEvaluated("occupation"))
                .build()
                .isLocked();

        assertTrue(locked);
    }

    @Test
    public void testPermitHolderNotFinishedHunting_notModerator_mooseDataCardNotPresent() {
        final BiFunction<BooleanSupplier, BooleanSupplier, Boolean> resolveLockCondition =
                (permitLockDatePassed, editingPermittedByOccupation) -> builder()
                        .withPermitHolderFinishedHunting(returning(false))
                        .withActionRequestedByModerator(returning(false))
                        .withDataOriginatingFromMooseDataCard(returning(false))
                        .withPermitLockDatePassed(permitLockDatePassed)
                        .withUserHavingOccupationGrantingPermission(editingPermittedByOccupation)
                        .build()
                        .isLocked();

        assertTrue(resolveLockCondition.apply(returning(true), shouldNotBeEvaluated("occupation")));

        assertFalse(resolveLockCondition.apply(returning(false), returning(true)));
        assertTrue(resolveLockCondition.apply(returning(false), returning(false)));
    }

    private static BooleanSupplier returning(final boolean value) {
        return () -> value;
    }

    private static BooleanSupplier shouldNotBeEvaluated(final String str) {
        return () -> {
            throw new IllegalStateException(str + " condition not expected to be evaluated");
        };
    }
}
