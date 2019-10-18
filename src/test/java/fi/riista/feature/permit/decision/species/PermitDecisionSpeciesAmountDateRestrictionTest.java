package fi.riista.feature.permit.decision.species;

import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.test.DefaultEntitySupplierProvider;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PermitDecisionSpeciesAmountDateRestrictionTest implements DefaultEntitySupplierProvider {

    private PermitDecisionSpeciesAmountDateRestriction restriction;

    private void initTest(final String permitTypeCode) {
        restriction = new PermitDecisionSpeciesAmountDateRestriction(permitTypeCode, 2019, 2019);
    }

    private void initTestForNextYear(final String permitTypeCode) {
        restriction = new PermitDecisionSpeciesAmountDateRestriction(permitTypeCode, 2019, 2020);
    }

    private boolean isValid(final int year, final int month, final int day) {
        final PermitDecisionSpeciesAmountDTO dto = new PermitDecisionSpeciesAmountDTO();
        dto.setBeginDate(new LocalDate(year, month, day));
        return restriction.isValid(dto);
    }

    @Test
    public void testAssertValid_Mooselike() {
        initTest(PermitTypeCode.MOOSELIKE);

        assertFalse(isValid(2019, 7, 31));
        assertTrue(isValid(2019, 8, 1));
        assertTrue(isValid(2020, 7, 31));
        assertFalse(isValid(2020, 8, 1));
    }

    @Test
    public void testAssertValid_MooselikeAmendment() {
        initTest(PermitTypeCode.MOOSELIKE_AMENDMENT);

        assertFalse(isValid(2019, 7, 31));
        assertTrue(isValid(2019, 8, 1));
        assertTrue(isValid(2020, 7, 31));
        assertFalse(isValid(2020, 8, 1));
    }

    @Test
    public void testAssertValid_AnnualUnprotectedBird() {
        initTest(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);

        assertFalse(isValid(2018, 12, 31));
        assertTrue(isValid(2019, 1, 1));
        assertTrue(isValid(2019, 12, 31));
        assertTrue(isValid(2020, 1, 1));
        assertTrue(isValid(2030, 1, 1));
    }

    @Test
    public void testAssertValid_AnnualUnprotectedBird_SecondYear() {
        initTestForNextYear(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);

        assertTrue(isValid(2019, 1, 1));
        assertTrue(isValid(2019, 12, 31));
        assertTrue(isValid(2020, 1, 1));
        assertTrue(isValid(2020, 12, 31));
        assertTrue(isValid(2021, 1, 1));
        assertTrue(isValid(2030, 1, 1));
    }

    @Test
    public void testAssertValid_FowlAndUnprotectedBird() {
        initTest(PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD);

        assertFalse(isValid(2018, 12, 31));
        assertTrue(isValid(2019, 1, 1));
        assertTrue(isValid(2019, 12, 31));
        assertFalse(isValid(2020, 1, 1));
    }

    @Test
    public void testAssertValid_FowlAndUnprotectedBird_SecondYear() {
        initTestForNextYear(PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD);

        assertFalse(isValid(2019, 12, 31));
        assertTrue(isValid(2020, 1, 1));
        assertTrue(isValid(2020, 12, 31));
        assertFalse(isValid(2021, 1, 1));
    }

    @Test
    public void testResolveMinAndMaxBeginDate_Mooselike() {
        initTest(PermitTypeCode.MOOSELIKE);

        assertEquals(new LocalDate(2019, 8, 1), restriction.resolveMinBeginDate());
        assertEquals(new LocalDate(2020, 7, 31), restriction.resolveMaxBeginDate());
    }

    @Test
    public void testResolveMinAndMaxBeginDate_MooselikeAmendment() {
        initTest(PermitTypeCode.MOOSELIKE_AMENDMENT);

        assertEquals(new LocalDate(2019, 8, 1), restriction.resolveMinBeginDate());
        assertEquals(new LocalDate(2020, 7, 31), restriction.resolveMaxBeginDate());
    }

    @Test
    public void testResolveMinAndMaxBeginDate_AnnualUnprotectedBird() {
        initTest(PermitTypeCode.ANNUAL_UNPROTECTED_BIRD);

        assertEquals(new LocalDate(2019, 1, 1), restriction.resolveMinBeginDate());
        assertNull(restriction.resolveMaxBeginDate());
    }

    @Test
    public void testResolveMinAndMaxBeginDate_FowlAndUnprotectedBird() {
        initTest(PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD);

        assertEquals(new LocalDate(2019, 1, 1), restriction.resolveMinBeginDate());
        assertEquals(new LocalDate(2019, 12, 31), restriction.resolveMaxBeginDate());
    }
}
