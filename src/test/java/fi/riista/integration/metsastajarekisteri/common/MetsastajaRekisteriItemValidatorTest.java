package fi.riista.integration.metsastajarekisteri.common;

import com.google.common.base.Throwables;
import fi.riista.integration.metsastajarekisteri.MetsastajaRekisteriPerson;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterDateFieldException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterInvoiceReferenceException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.exception.InvalidSsnException;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MetsastajaRekisteriItemValidatorTest {

    private MetsastajaRekisteriItemValidator validator;
    private MetsastajaRekisteriPerson person;

    @Before
    public void init() {
        validator = new MetsastajaRekisteriItemValidator();
        person = new MetsastajaRekisteriPerson();

        person.setSsn("120286+101T");
        person.setFirstName("aa");
        person.setLastName("bb");
        person.setHunterNumber("11111111");
        person.setHunterExamDate(new LocalDate(2005, 3, 1));
        person.setHunterExamExpirationDate(new LocalDate(2020, 3, 1));
        person.setHuntingCardStart(new LocalDate(2015, 8, 1));
        person.setHuntingCardEnd(new LocalDate(2016, 7, 31));
        person.setHuntingBanStart(null);
        person.setHuntingBanEnd(null);
        person.setHomeMunicipalityCode("201");
        person.setLanguageCode("fi");
        person.setMagazineLanguageCode("fi");
        person.setCountryCode("FI");
    }

    private MetsastajaRekisteriPerson validate() {
        try {
            return validator.process(this.person);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private void assertValidPerson() {
        assertNotNull(validate());
    }

    private void assertInvalidPerson() {
        assertNull(validate());
    }

    @Test
    public void testValid() {
        assertValidPerson();
        assertNotNull(person.getHomeMunicipalityCode());
        assertNotNull(person.getLanguageCode());
        assertNotNull(person.getMagazineLanguageCode());
        assertNotNull(person.getCountryCode());
        assertNotNull(person.getHunterNumber());
        assertNotNull(person.getSsn());
        assertNotNull(person.getHuntingCardStart());
        assertNotNull(person.getHuntingCardEnd());
        assertNotNull(person.getHunterExamDate());
        assertNotNull(person.getHunterExamExpirationDate());
    }

    // First name and lastName

    @Test
    public void testMissingFirstName() {
        person.setFirstName(null);
        assertInvalidPerson();
    }

    @Test
    public void testMissingLastName() {
        person.setLastName(null);
        assertInvalidPerson();
    }

    @Test(expected = InvalidPersonName.class)
    public void testShortFirstName() {
        person.setFirstName("a");
        assertInvalidPerson();
    }

    @Test(expected = InvalidPersonName.class)
    public void testShortLastName() {
        person.setLastName("b");
        assertInvalidPerson();
    }

    // SSN

    @Test
    public void testMissingSSN() {
        person.setSsn(null);
        assertInvalidPerson();
    }

    @Test
    public void testEmptySsn() {
        person.setSsn(" ");
        assertInvalidPerson();
    }

    @Test(expected = InvalidSsnException.class)
    public void testInvalidSsn() {
        person.setSsn("111111-103X");
        validate();
    }

    // Hunter number

    @Test
    public void testMissingHunterNumber() {
        person.setHunterNumber(null);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterNumberException.class)
    public void testEmptyHunterNumber() {
        person.setHunterNumber(" ");
        validate();
    }

    @Test(expected = InvalidHunterNumberException.class)
    public void testInvalidHunterNumber() {
        person.setHunterNumber("11111112");
        validate();
    }

    // Hunting card start & end

    @Test
    public void testValidHuntingCardRange_BothEmpty() {
        person.setHuntingCardStart(null);
        person.setHuntingCardEnd(null);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testInvalidHuntingCardRange_StartIsEmpty() {
        person.setHuntingCardStart(null);
        validate();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testInvalidHuntingCardRange_EndIsEmpty() {
        person.setHuntingCardEnd(null);
        validate();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testInvalidHuntingCardRange_StartAfterEnd() {
        LocalDate tmp = person.getHuntingCardEnd();
        person.setHuntingCardEnd(person.getHuntingCardStart());
        person.setHuntingCardStart(tmp);
        validate();
    }

    // Hunting exam start & end

    @Test
    public void testValidHunterExam_BothEmpty() {
        person.setHunterExamDate(null);
        person.setHunterExamExpirationDate(null);
        assertValidPerson();
    }

    @Test
    public void testValidHunterExam_StartEmpty() {
        person.setHunterExamDate(null);
        assertValidPerson();
    }

    @Test
    public void testValidHunterExam_EndEmpty() {
        person.setHunterExamExpirationDate(null);
        assertValidPerson();
    }

    @Test
    public void testValidHunterExam_SpecialStartValue() {
        person.setHunterExamDate(new LocalDate(1990, 6, 19));
        assertValidPerson();
        assertNull(person.getHunterExamDate());
    }

    // Hunting ban

    @Test
    public void testValidHuntingBan_BothSet() {
        person.setHuntingBanStart(new LocalDate(2015, 1, 1));
        person.setHuntingBanEnd(new LocalDate(2016, 1, 31));
        assertValidPerson();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testInvalidHuntingBan_StartEmpty() {
        person.setHuntingBanStart(null);
        person.setHuntingBanEnd(new LocalDate(2016, 1, 31));
        validate();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testInvalidHuntingBan_EndEmpty() {
        person.setHuntingBanStart(new LocalDate(2015, 1, 1));
        validate();
    }

    // Municipality code

    @Test
    public void testInvalidMunicipalityCode_TwoDigits() {
        person.setHomeMunicipalityCode("12");
        assertValidPerson();
        assertNull(person.getHomeMunicipalityCode());
    }

    @Test
    public void testInvalidMunicipalityCode_InvalidCharacter() {
        person.setHomeMunicipalityCode("12#");
        assertValidPerson();
        assertNull(person.getHomeMunicipalityCode());
    }

    // Payment dates

    @Test
    public void testValidPaymentDateOne_BothSet() {
        person.setHuntingPaymentOneDay(today());
        person.setHuntingPaymentOneYear(2014);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testValidPaymentDateOne_SeasonMissing() {
        person.setHuntingPaymentOneDay(today());
        person.setHuntingPaymentOneYear(null);
        validate();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testValidPaymentDateOne_DateMissing() {
        person.setHuntingPaymentOneDay(null);
        person.setHuntingPaymentOneYear(2014);
        validate();
    }

    @Test
    public void testValidPaymentDateTwo_BothSet() {
        person.setHuntingPaymentTwoDay(today());
        person.setHuntingPaymentTwoYear(2014);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testValidPaymentDateTwo_SeasonMissing() {
        person.setHuntingPaymentTwoDay(today());
        person.setHuntingPaymentTwoYear(null);
        validate();
    }

    @Test(expected = InvalidHunterDateFieldException.class)
    public void testValidPaymentDateTwo_DateMissing() {
        person.setHuntingPaymentTwoDay(null);
        person.setHuntingPaymentTwoYear(2014);
        validate();
    }

    // Invoice references
    @Test
    public void testValidInvoiceReferenceCurrent_BothSet() {
        person.setInvoiceReferenceCurrent("1234567");
        person.setInvoiceReferenceCurrentYear(2014);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterInvoiceReferenceException.class)
    public void testValidInvoiceReferenceCurrent_MissingValue() {
        person.setInvoiceReferenceCurrentYear(2014);
        validate();
    }

    @Test(expected = InvalidHunterInvoiceReferenceException.class)
    public void testValidInvoiceReferenceCurrent_MissingYear() {
        person.setInvoiceReferenceCurrent("1234567");
        validate();
    }

    @Test
    public void testValidInvoiceReferencePrevious_BothSet() {
        person.setInvoiceReferencePrevious("1234567");
        person.setInvoiceReferencePreviousYear(2014);
        assertValidPerson();
    }

    @Test(expected = InvalidHunterInvoiceReferenceException.class)
    public void testValidInvoiceReferencePrevious_MissingValue() {
        person.setInvoiceReferencePreviousYear(2014);
        validate();
    }

    @Test(expected = InvalidHunterInvoiceReferenceException.class)
    public void testValidInvoiceReferencePrevious_MissingYear() {
        person.setInvoiceReferencePrevious("1234567");
        validate();
    }

}
