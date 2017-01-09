package fi.riista.feature.common.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;

import org.junit.Test;

import javax.validation.ValidationException;

public class RequiredTest {

    private static final String FIELD_NAME = "fieldName";

    @Test
    public void testNullifyIfNeeded_YES() {
        assertEquals("ok", Required.YES.nullifyIfNeeded("ok", FIELD_NAME));
        assertEquals("", Required.YES.nullifyIfNeeded("", FIELD_NAME));

        expectRequiredFieldMissingException(Required.getDefaultMessageForMissingField(FIELD_NAME), () -> {
            Required.YES.nullifyIfNeeded(null, FIELD_NAME);
        });
    }

    @Test
    public void testNullifyIfNeeded_VOLUNTARY() {
        assertEquals("vol", Required.VOLUNTARY.nullifyIfNeeded("vol", FIELD_NAME));
        assertEquals("", Required.VOLUNTARY.nullifyIfNeeded("", FIELD_NAME));
        assertNull(Required.VOLUNTARY.nullifyIfNeeded(null, FIELD_NAME));
    }

    @Test
    public void testNullifyIfNeeded_NO() {
        assertNull(Required.NO.nullifyIfNeeded(null, FIELD_NAME));
        assertNull(Required.NO.nullifyIfNeeded("", FIELD_NAME));
        assertNull(Required.NO.nullifyIfNeeded("all values are returned as null", FIELD_NAME));
    }

    @Test
    public void testIsValidValue_YES() {
        assertTrue(Required.YES.isValidValue("ok"));
        assertTrue(Required.YES.isValidValue(""));
        assertFalse(Required.YES.isValidValue(null));
    }

    @Test
    public void testIsValidValue_VOLUNTARY() {
        assertTrue(Required.VOLUNTARY.isValidValue("ok"));
        assertTrue(Required.VOLUNTARY.isValidValue(""));
        assertTrue(Required.VOLUNTARY.isValidValue(null));
    }

    @Test
    public void testIsValidValue_NO() {
        assertFalse(Required.NO.isValidValue("ok"));
        assertFalse(Required.NO.isValidValue(""));
        assertTrue(Required.NO.isValidValue(null));
    }

    @Test
    public void testAssertValue_YES() {
        Required.YES.assertValue("ok", FIELD_NAME);
        Required.YES.assertValue("", FIELD_NAME);

        expectRequiredFieldMissingException(Required.getDefaultMessageForMissingField(FIELD_NAME), () -> {
            Required.YES.assertValue(null, FIELD_NAME);
        });
    }

    @Test
    public void testAssertValue_VOLUNTARY() {
        Required.VOLUNTARY.assertValue("ok", FIELD_NAME);
        Required.VOLUNTARY.assertValue("", FIELD_NAME);
        Required.VOLUNTARY.assertValue(null, FIELD_NAME);
    }

    @Test
    public void testAssertValue_NO() {
        Required.NO.assertValue(null, FIELD_NAME);

        expectProhibitedFieldFoundException(Required.getDefaultMessageForProhibitedField(FIELD_NAME), () -> {
            Required.NO.assertValue("ok", FIELD_NAME);
        });

        expectProhibitedFieldFoundException(Required.getDefaultMessageForProhibitedField(FIELD_NAME), () -> {
            Required.NO.assertValue("", FIELD_NAME);
        });
    }

    @Test
    public void testWeight() {
        final Double weight = 1.0;

        assertEquals(weight, Required.YES.validateWeight(weight, HuntingMethod.SHOT));
        assertEquals(weight, Required.YES.validateWeight(weight, HuntingMethod.CAPTURED_ALIVE));
        assertNull(Required.YES.validateWeight(weight, HuntingMethod.SHOT_BUT_LOST));

        assertEquals(weight, Required.VOLUNTARY.validateWeight(weight, HuntingMethod.SHOT));
        assertEquals(weight, Required.VOLUNTARY.validateWeight(weight, HuntingMethod.CAPTURED_ALIVE));
        assertNull(Required.VOLUNTARY.validateWeight(weight, HuntingMethod.SHOT_BUT_LOST));

        assertNull(Required.NO.validateWeight(weight, HuntingMethod.SHOT));
        assertNull(Required.NO.validateWeight(weight, HuntingMethod.CAPTURED_ALIVE));
        assertNull(Required.NO.validateWeight(weight, HuntingMethod.SHOT_BUT_LOST));
    }

    @Test
    public void testValidateHuntingParty() {
        final String huntingParty = "huntingpartyname";

        assertEquals(huntingParty, Required.YES.validateHuntingParty(huntingParty, HuntingAreaType.HUNTING_SOCIETY));
        assertNull(Required.YES.validateHuntingParty(huntingParty, HuntingAreaType.PROPERTY));

        assertEquals(
                huntingParty,
                Required.VOLUNTARY.validateHuntingParty(huntingParty, HuntingAreaType.HUNTING_SOCIETY));
        assertNull(Required.VOLUNTARY.validateHuntingParty(huntingParty, HuntingAreaType.PROPERTY));

        assertNull(Required.NO.validateHuntingParty(huntingParty, HuntingAreaType.HUNTING_SOCIETY));
        assertNull(Required.NO.validateHuntingParty(huntingParty, HuntingAreaType.PROPERTY));
    }

    @Test
    public void testHuntingPartyNull_YES_HUNTING_SOCIETY() {
        final String emptyHuntingParty = "";
        assertValidationException(true, emptyHuntingParty, Required.YES, HuntingAreaType.HUNTING_SOCIETY);
        assertValidationException(true, emptyHuntingParty, Required.VOLUNTARY, HuntingAreaType.HUNTING_SOCIETY);
        assertValidationException(false, emptyHuntingParty, Required.NO, HuntingAreaType.HUNTING_SOCIETY);

        final String nullHuntingParty = null;
        assertValidationException(true, nullHuntingParty, Required.YES, HuntingAreaType.HUNTING_SOCIETY);
        assertValidationException(true, nullHuntingParty, Required.VOLUNTARY, HuntingAreaType.HUNTING_SOCIETY);
        assertValidationException(false, nullHuntingParty, Required.NO, HuntingAreaType.HUNTING_SOCIETY);
    }

    private static void expectRequiredFieldMissingException(final String expectedMessage, final Runnable task) {
        expectExceptionWithMessage(Required.RequiredFieldMissing.class, expectedMessage, task);
    }

    private static void expectProhibitedFieldFoundException(final String expectedMessage, final Runnable task) {
        expectExceptionWithMessage(Required.ProhibitedFieldFound.class, expectedMessage, task);
    }

    private static void expectExceptionWithMessage(
            final Class<? extends ValidationException> exceptionClass,
            final String expectedMessage,
            final Runnable task) {

        try {
            task.run();
            fail();
        } catch (final ValidationException e) {
            assertEquals(exceptionClass, e.getClass());
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    private static void assertValidationException(
            final boolean shouldFail,
            final String huntingParty,
            final Required r,
            final HuntingAreaType huntingAreaType) {

        try {
            r.validateHuntingParty(huntingParty, huntingAreaType);
            if (shouldFail) {
                fail("Exception should have happend");
            }
        } catch (final ValidationException iex) {
            if (!shouldFail) {
                fail("Unexpected exception: " + iex);
            }
        } catch (final Exception e) {
            fail("Unexpected exception thrown: " + e);
        }
    }

}
