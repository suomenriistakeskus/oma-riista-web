package fi.riista.integration.metsastajarekisteri.common;

import fi.riista.integration.metsastajarekisteri.MetsastajaRekisteriPerson;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterDateFieldException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterInvoiceReferenceException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidHunterNumberException;
import fi.riista.integration.metsastajarekisteri.exception.InvalidPersonName;
import fi.riista.integration.metsastajarekisteri.exception.InvalidSsnException;
import fi.riista.validation.FinnishHunterNumberValidator;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.regex.Pattern;

public class MetsastajaRekisteriItemValidator implements
        ItemProcessor<MetsastajaRekisteriPerson, MetsastajaRekisteriPerson> {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriItemValidator.class);

    private static final LocalDate INVALID_JOIN_DATE = new LocalDate(1990, 6, 19);

    private static final FinnishSocialSecurityNumberValidator ssnValidator = new FinnishSocialSecurityNumberValidator();

    private static final FinnishHunterNumberValidator hunterNumberValidator = new FinnishHunterNumberValidator();

    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

    private static final Pattern TWO_LOWER_CASE_LETTERS = Pattern.compile("^\\p{Lower}{2}$");

    private static final Pattern TWO_UPPER_CASE_LETTERS = Pattern.compile("^\\p{Upper}{2}$");

    private static final Pattern THREE_DIGITS = Pattern.compile("^\\d{3}$");

    private static boolean nullOrMatches(String value, Pattern pattern) {
        return value == null || pattern.matcher(value).matches();
    }

    public static <A, B> boolean onlyOther(A a, B b) {
        return (a != null && b == null) ||
                (a == null && b != null);
    }

    public static boolean invalidRange(LocalDate begin, LocalDate end) {
        return onlyOther(begin, end) || (begin != null && end != null && begin.isAfter(end));
    }

    private static boolean saneDate(LocalDate day) {
        return day != null && day.getYear() > 1900 && day.getYear() < 2100;
    }

    private static boolean validEmail(final String email) {
        return StringUtils.isNotBlank(email) && EMAIL_VALIDATOR.isValid(email, null);
    }

    private static boolean validName(final String name) {
        return name != null && name.length() > 1;
    }

    private static String rangeToText(LocalDate begin, LocalDate end) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(begin != null ? begin.toString() : "<empty>");
        sb.append(" to ");
        sb.append(end != null ? end.toString() : "<empty>");
        sb.append("]");
        return sb.toString();
    }

    private static boolean shouldSkip(MetsastajaRekisteriPerson person) {
        // Invalid SSN
        if (StringUtils.isBlank(person.getSsn())) {
            // Skip empty value
            return true;
        }

        if (person.getSsn().length() < 11) {
            // Skip too short values
            return true;
        }

        if (person.getSsn().charAt(7) == '9') {
            // Skip -9xx series as invalid
            return true;
        }

        if (person.getFirstName() == null || person.getLastName() == null) {
            // Skip person with missing name
            return true;
        }

        return false;
    }

    @Override
    public MetsastajaRekisteriPerson process(final MetsastajaRekisteriPerson person) throws Exception {
        if (shouldSkip(person)) {
            return null;
        }

        if (!ssnValidator.isValid(person.getSsn(), null)) {
            LOG.warn("Invalid SSN: {}", person.getSsn());

            throw new InvalidSsnException("Invalid ssn " + person.getSsn());
        }

        if (!hunterNumberValidator.isValid(person.getHunterNumber(), null)) {
            LOG.warn("Invalid hunterNumber: {}", person.getHunterNumber());

            throw new InvalidHunterNumberException("Invalid hunterNumber " + person.getHunterNumber());
        }

        if (!validName(person.getFirstName())) {
            throw new InvalidPersonName("Invalid firstName=" + person.getFirstName()
                    + " for hunterNumber=" + person.getHunterNumber());
        }

        if (!validName(person.getLastName())) {
            throw new InvalidPersonName("Invalid lastName=" + person.getLastName()
                    + " for hunterNumber=" + person.getHunterNumber());
        }

        // Check hunter exam dates
        if (INVALID_JOIN_DATE.equals(person.getHunterExamDate())) {
            // Allow special date as empty value
            person.setHunterExamDate(null);

        } else if (person.getHunterExamDate() != null && !saneDate(person.getHunterExamDate())) {
            throw new InvalidHunterDateFieldException(
                    "Invalid hunterExamDate=" + person.getHunterExamDate());
        }

        if (person.getHunterExamExpirationDate() != null && !saneDate(person.getHunterExamExpirationDate())) {
            throw new InvalidHunterDateFieldException(
                    "Invalid hunterExamExpirationDate=" + person.getHunterExamExpirationDate());
        }

        if (person.getHunterExamDate() != null && person.getHunterExamExpirationDate() != null &&
                invalidRange(person.getHunterExamDate(), person.getHunterExamExpirationDate())) {
            throw new InvalidHunterDateFieldException(
                    "Invalid hunterExam date range for hunterNumber=" + person.getHunterNumber() +
                            ". Range " + rangeToText(person.getHunterExamDate(), person.getHunterExamExpirationDate()));
        }

        // Check hunting card dates
        if (person.getHuntingCardStart() != null && !saneDate(person.getHuntingCardStart())) {
            throw new InvalidHunterDateFieldException("Invalid huntingCardStart=" + person.getHuntingCardStart());
        }

        if (person.getHuntingCardEnd() != null && !saneDate(person.getHuntingCardEnd())) {
            throw new InvalidHunterDateFieldException("Invalid huntingCardEnd=" + person.getHuntingCardEnd());
        }

        if (invalidRange(person.getHuntingCardStart(), person.getHuntingCardEnd())) {
            throw new InvalidHunterDateFieldException(
                    "Invalid huntingCard date range for hunterNumber=" + person.getHunterNumber() +
                            ". Range " + rangeToText(person.getHuntingCardStart(), person.getHuntingCardEnd()));
        }

        // Check hunting ban dates
        if (person.getHuntingBanStart() != null && !saneDate(person.getHuntingBanStart())) {
            throw new InvalidHunterDateFieldException("Invalid huntingBanStart=" + person.getHuntingBanStart());
        }

        if (person.getHuntingBanEnd() != null && !saneDate(person.getHuntingBanEnd())) {
            throw new InvalidHunterDateFieldException("Invalid huntingBanEnd=" + person.getHuntingBanEnd());
        }

        if (invalidRange(person.getHuntingBanStart(), person.getHuntingBanEnd())) {
            throw new InvalidHunterDateFieldException(
                    "Invalid huntingBand date range for hunterNumber=" + person.getHunterNumber() +
                            ". Range " + rangeToText(person.getHuntingBanStart(), person.getHuntingBanEnd()));
        }

        // Check payment dates
        if (onlyOther(person.getHuntingPaymentOneDay(), person.getHuntingPaymentOneYear())) {
            throw new InvalidHunterDateFieldException("Payment date one or season missing for hunterNumber="
                    + person.getHunterNumber());
        }

        if (onlyOther(person.getHuntingPaymentTwoDay(), person.getHuntingPaymentTwoYear())) {
            throw new InvalidHunterDateFieldException("Payment date two or season missing for hunterNumber="
                    + person.getHunterNumber());
        }

        // Check invoice references
        if (onlyOther(person.getInvoiceReferenceCurrent(), person.getInvoiceReferenceCurrentYear())) {
            throw new InvalidHunterInvoiceReferenceException(
                    "Current invoice reference or year is missing for hunterNumber=" + person.getHunterNumber());
        }

        if (onlyOther(person.getInvoiceReferencePrevious(), person.getInvoiceReferencePreviousYear())) {
            throw new InvalidHunterInvoiceReferenceException(
                    "Previous invoice reference or year is missing for hunterNumber=" + person.getHunterNumber());
        }

        // Non-fatal errors
        if (!validEmail(person.getEmail())) {
            person.setEmail(null);
        }

        if (person.getHomeMunicipalityCode() != null) {
            if ("000".equals(person.getHomeMunicipalityCode())) {
                person.setHomeMunicipalityCode(null);

            } else if (!nullOrMatches(person.getHomeMunicipalityCode(), THREE_DIGITS)) {
                LOG.warn("Invalid municipalityCode {}", person.getHomeMunicipalityCode());

                person.setHomeMunicipalityCode(null);
            }
        }

        if (!nullOrMatches(person.getLanguageCode(), TWO_LOWER_CASE_LETTERS)) {
            LOG.warn("Invalid languageCode {}", person.getLanguageCode());

            person.setLanguageCode(null);
        }

        if (!nullOrMatches(person.getMagazineLanguageCode(), TWO_LOWER_CASE_LETTERS)) {
            LOG.warn("Invalid magazineLanguageCode {}", person.getMagazineLanguageCode());

            person.setMagazineLanguageCode(null);
        }

        if (!nullOrMatches(person.getCountryCode(), TWO_UPPER_CASE_LETTERS)) {
            LOG.warn("Invalid countryCode {}", person.getCountryCode());

            person.setCountryCode(null);
        }

        return person;
    }
}
