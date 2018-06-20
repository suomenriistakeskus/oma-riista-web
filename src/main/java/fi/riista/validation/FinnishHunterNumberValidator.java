package fi.riista.validation;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Finnish hunter number is 7 numbers + checksum.
 * Numbers can not begin with 0.
 * Checksum is calculated just as finnish creditor reference (suomalainen viitenumero).
 */
public class FinnishHunterNumberValidator implements ConstraintValidator<FinnishHunterNumber, String> {
    private static final Pattern REGEX_PATTERN = Pattern.compile("[1-9][0-9]{7}");
    private static final int VALID_LENGTH = 8;

    private boolean verifyChecksum;

    public FinnishHunterNumberValidator() {
        this(true);
    }

    public FinnishHunterNumberValidator(boolean verifyChecksum) {
        this.verifyChecksum = verifyChecksum;
    }

    @Override
    public void initialize(FinnishHunterNumber finnishHunterNumber) {
        this.verifyChecksum = finnishHunterNumber.verifyChecksum();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        return validate(value, this.verifyChecksum);
    }

    public static boolean validate(final String value, final boolean verifyChecksum) {
        final Function<String, String> valueToRegexFn = Function.identity();
        final Function<String, String> valueToChecksumFn = Function.identity();

        return FinnishCreditorReferenceValidator.validate(
                verifyChecksum, value, valueToRegexFn, valueToChecksumFn, VALID_LENGTH, VALID_LENGTH, REGEX_PATTERN);
    }
}
