package fi.riista.validation;


import com.google.common.base.Strings;
import org.joda.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FinnishSocialSecurityNumberValidator
        implements ConstraintValidator<FinnishSocialSecurityNumber, String> {

    private static final char[] CHECKSUM_CHARS = "0123456789ABCDEFHJKLMNPRSTUVWXY".toCharArray();

    private boolean checksumVerified;

    public FinnishSocialSecurityNumberValidator() {
        this(true);
    }

    public FinnishSocialSecurityNumberValidator(boolean checksumVerified) {
        this.checksumVerified = checksumVerified;
    }

    @Override
    public void initialize(FinnishSocialSecurityNumber constraintAnnotation) {
        this.checksumVerified = constraintAnnotation.checksumVerified();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validate(value, checksumVerified);
    }

    public static boolean validate(String value, final boolean checksumVerified) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        if (value.length() != 11) {
            return false;
        }

        value = value.toUpperCase();

        try {
            LocalDate birthDate = parseBirthDate(value);
            int personNumber = parseInt(value, 7, 3);

            if (checksumVerified) {
                char checksum = value.charAt(10);
                char calculatedChecksum = calculateChecksum(birthDate, personNumber);
                return checksum == calculatedChecksum;
            }

            return true;

        } catch (RuntimeException ignore) {
            return false;
        }
    }

    private static int parseInt(String s, int index, int len) {
        return Integer.parseInt(s.substring(index, index + len));
    }

    public static LocalDate parseBirthDate(String ssn) {
        int century = resolveCentury(ssn.charAt(6));
        int year = century + parseInt(ssn, 4, 2);
        int month = parseInt(ssn, 2, 2);
        int day = parseInt(ssn, 0, 2);
        return new LocalDate(year, month, day);
    }

    private static int resolveCentury(char centuryChar) {
        switch (centuryChar) {
            case '+':
                return 1800;
            case '-':
            case 'Y':
            case 'X':
            case 'W':
            case 'V':
            case 'U':
                return 1900;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return 2000;
            default:
                throw new IllegalArgumentException("Illegal century character.");
        }
    }

    public static char calculateChecksum(LocalDate date, int personNumber) {
        int t = date.getDayOfMonth() * 10000 + date.getMonthOfYear() * 100 +
                date.getYearOfCentury();
        int index = (t * 1000 + personNumber) % 31;
        return CHECKSUM_CHARS[index];
    }
}
