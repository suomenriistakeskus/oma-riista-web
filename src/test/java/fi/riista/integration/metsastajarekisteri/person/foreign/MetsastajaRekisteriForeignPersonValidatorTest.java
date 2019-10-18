package fi.riista.integration.metsastajarekisteri.person.foreign;

import com.google.common.base.Throwables;
import fi.riista.integration.metsastajarekisteri.exception.IllegalAgeException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.validation.Validators;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.integration.metsastajarekisteri.InnofactorConstants.MAX_PERSON_AGE;
import static fi.riista.integration.metsastajarekisteri.InnofactorConstants.MIN_FOREIGN_PERSON_AGE;
import static fi.riista.integration.metsastajarekisteri.InnofactorConstants.RHY_AHVENANMAA;
import static fi.riista.integration.metsastajarekisteri.InnofactorConstants.RHY_FOREIGN_MEMBER_CODE;
import static fi.riista.util.DateUtil.today;
import static java.lang.String.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MetsastajaRekisteriForeignPersonValidatorTest {

    private final MetsastajaRekisteriForeignPersonValidator validator = new MetsastajaRekisteriForeignPersonValidator();

    private MetsastajaRekisteriPerson person;

    @Before
    public void init() {
        person = new MetsastajaRekisteriPerson();
        person.setFirstName("aa");
        person.setLastName("bb");
        person.setHunterNumber("11111111");
        person.setDateOfBirth(today().minusYears(50));
        person.setMembershipRhyOfficialCode(RHY_FOREIGN_MEMBER_CODE);
    }

    private MetsastajaRekisteriPerson validate() {
        try {
            return validator.process(this.person);
        } catch (final Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private void assertValidForeignHunter() {
        assertNotNull(validate());
    }

    private void assertFilteredOut() {
        assertNull(validate());
    }

    @Test
    public void testValid() {
        assertValidForeignHunter();

        assertNotNull(person.getFirstName());
        assertNotNull(person.getLastName());
        assertNotNull(person.getHunterNumber());
        assertNotNull(person.getDateOfBirth());
        assertNull(person.getHunterExamDate());

        final String ssn = person.getSsn();
        assertTrue(ssn == null || !Validators.isValidSsn(ssn));
    }

    // Hunter number

    @Test
    public void testMissingHunterNumber() {
        person.setHunterNumber(null);
        assertFilteredOut();
    }

    @Test
    public void testEmptyHunterNumber() {
        person.setHunterNumber(" ");
        assertFilteredOut();
    }

    @Test(expected = InvalidHunterNumberException.class)
    public void testInvalidHunterNumber() {
        person.setHunterNumber("11111112");
        validate();
    }

    // SSN

    @Test
    public void testSsnEndPart() {
        // Innofactor removes duplicates by decrementing the end part from 9999.
        // Allow 10 foreigners for single date.
        for (int i = 9999; i > 0; i--) {
            person.setSsn("111111-" + i);

            if (i < 9990) {
                assertFilteredOut();
            } else {
                assertValidForeignHunter();
            }
        }
    }

    @Test
    public void testMissingSSN() {
        person.setSsn(null);
        assertValidForeignHunter();
    }

    @Test
    public void testEmptySsn() {
        person.setSsn(" ");
        assertValidForeignHunter();
    }

    @Test
    public void testValidFinnishSSN() {
        person.setSsn("170357-585Y");
        assertFilteredOut();
    }

    @Test
    public void testValidArtificialFinnishSSN() {
        person.setSsn("240998-911D");
        assertFilteredOut();
    }

    @Test
    public void testInvalidFinnishSsn() {
        person.setSsn("111111-103X");
        assertFilteredOut();
    }

    // First name and lastName

    @Test
    public void testMissingFirstName() {
        person.setFirstName(null);
        assertFilteredOut();
    }

    @Test
    public void testMissingLastName() {
        person.setLastName(null);
        assertFilteredOut();
    }

    @Test(expected = InvalidPersonName.class)
    public void testShortFirstName() {
        person.setFirstName("a");
        assertFilteredOut();
    }

    // Date of birth

    @Test
    public void testMissingDateOfBirth() {
        person.setDateOfBirth(null);
        assertFilteredOut();
    }

    @Test(expected = IllegalAgeException.class)
    public void testTooYoungPerson() {
        person.setDateOfBirth(today().minusYears(MIN_FOREIGN_PERSON_AGE).plusDays(1));
        validate();
    }

    @Test(expected = IllegalAgeException.class)
    public void testTooOldPerson() {
        person.setDateOfBirth(today().minusYears(MAX_PERSON_AGE + 1));
        validate();
    }

    // RHY code

    @Test
    public void testMissingRhyCode() {
        person.setMembershipRhyOfficialCode(null);
        assertFilteredOut();
    }

    @Test
    public void testSomeFinnishRhyCode() {
        person.setMembershipRhyOfficialCode("001");
        assertFilteredOut();
    }

    @Test
    public void testÅlandRhyCode() {
        person.setMembershipRhyOfficialCode(RHY_AHVENANMAA);
        assertValidForeignHunter();
    }

    @Test
    public void testForeignRhyCode() {
        person.setMembershipRhyOfficialCode(RHY_FOREIGN_MEMBER_CODE);
        assertValidForeignHunter();
    }

    @Test
    public void testAllRhyCodes_visitingForeigner() {
        for (int i = 1; i < 1000; i++) {
            final String rhyCode = format("%03d", i);
            person.setMembershipRhyOfficialCode(rhyCode);

            if (isValidRhyCodeForVisitingForeigner(rhyCode)) {
                assertValidForeignHunter();
            } else {
                assertFilteredOut();
            }
        }
    }

    @Test
    public void testAllRhyCodes_foreignerWithHunterExam() {
        for (int i = 1; i < 1000; i++) {
            final String rhyCode = format("%03d", i);
            person.setMembershipRhyOfficialCode(rhyCode);
            person.setHunterExamDate(today());

            assertValidForeignHunter();
        }
    }

    private static final boolean isValidRhyCodeForVisitingForeigner(final String rhyCode) {
        return rhyCode.equals(RHY_FOREIGN_MEMBER_CODE) || rhyCode.equals(RHY_AHVENANMAA);
    }

}
