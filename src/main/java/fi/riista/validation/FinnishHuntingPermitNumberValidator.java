package fi.riista.validation;

import fi.riista.util.Patterns;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Finnish hunting permit number is for example 2013-3-450-00260-2, where
 * <ul>
 * <li>2013: year when permit is given, 4 digits</li>
 * <li>3: how many years permit is valid, 1,2,3,4 or 5</li>
 * <li>450: RKA code, zero padded to 3 digits</li>
 * <li>00260: running permit number counter, zero padded to 5 digits</li>
 * <li>2: checksum, calculated just as finnish creditor reference (suomalainen viitenumero)</li>
 * </ul>
 */
public class FinnishHuntingPermitNumberValidator implements ConstraintValidator<FinnishHuntingPermitNumber, String> {

    private static final Pattern REGEX_PATTERN = Pattern.compile(Patterns.PERMIT_NUMBER);
    private static final int VALID_LENGTH = 18;

    private boolean verifyChecksum;

    public FinnishHuntingPermitNumberValidator() {
        this(true);
    }

    public FinnishHuntingPermitNumberValidator(final boolean verifyChecksum) {
        this.verifyChecksum = verifyChecksum;
    }

    @Override
    public void initialize(final FinnishHuntingPermitNumber finnishHuntingPermitNumber) {
        this.verifyChecksum = finnishHuntingPermitNumber.verifyChecksum();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        return validate(value, this.verifyChecksum);
    }

    public static boolean validate(final String value, final boolean verifyChecksum) {
        final Function<String, String> valueToRegexFn = v -> v.replace(" ", "");
        final Function<String, String> valueToChecksumFn = FinnishHuntingPermitNumberValidator::onlyDigits;

        return FinnishCreditorReferenceValidator.validate(
                verifyChecksum, value, valueToRegexFn, valueToChecksumFn, VALID_LENGTH, VALID_LENGTH, REGEX_PATTERN);
    }

    public static char calculateChecksum(final String s) {
        return FinnishCreditorReferenceValidator.calculateChecksum(onlyDigits(s));
    }

    private static String onlyDigits(final String value) {
        return value.replaceAll("-", "");
    }
}
