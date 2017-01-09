package fi.riista.validation;


import com.google.common.base.Strings;
import com.google.common.collect.Iterators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Iterator;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Finnish creditor reference is 3-19 numbers + checksum.
 * Numbers can not begin with 0.
 */
public class FinnishCreditorReferenceValidator implements ConstraintValidator<FinnishCreditorReference, String> {

    private static final Pattern REGEX_PATTERN = Pattern.compile("[1-9][0-9]{3,19}");
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 20;

    private boolean verifyChecksum;

    public FinnishCreditorReferenceValidator() {
        this(true);
    }

    public FinnishCreditorReferenceValidator(boolean verifyChecksum) {
        this.verifyChecksum = verifyChecksum;
    }

    @Override
    public void initialize(FinnishCreditorReference finnishCreditorReference) {
        this.verifyChecksum = finnishCreditorReference.verifyChecksum();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        return validate(value, this.verifyChecksum);
    }

    public static boolean validate(final String value, final boolean verifyChecksum) {
        final Function<String, String> valueToRegex = v -> v.replace(" ", "");
        final Function<String, String> valueToChecksum = valueToRegex;

        return validate(verifyChecksum, value, valueToRegex, valueToChecksum, MIN_LENGTH, MAX_LENGTH, REGEX_PATTERN);
    }

    public static boolean validate(final boolean verifyChecksum,
                                   final String value,
                                   final Function<String, String> valueToRegexFn,
                                   final Function<String, String> valueToChecksumFn,
                                   final int valueToRegexMinLength,
                                   final int valueToRegexMaxLength,
                                   final Pattern regEx) {
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }
        final String valueToRegex = valueToRegexFn.apply(value);
        if (valueToRegex.length() < valueToRegexMinLength || valueToRegex.length() > valueToRegexMaxLength) {
            return false;
        }

        if (!regEx.matcher(valueToRegex).matches()) {
            return false;
        }

        if (!verifyChecksum) {
            return true;
        }

        final String valueToChecksum = valueToChecksumFn.apply(value);
        final char calculatedChecksum = calculateChecksum(valueToChecksum.substring(0, valueToChecksum.length() - 1));

        final char checksum = valueToChecksum.charAt(valueToChecksum.length() - 1);
        return checksum == calculatedChecksum;
    }

    public static char calculateChecksum(final String s) {
        int sum = 0;

        final Iterator<Integer> weightsIterator = Iterators.cycle(7, 3, 1);
        for (int i = s.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(s.charAt(i)) * weightsIterator.next();
        }

        final int remainder = sum % 10;

        if (remainder == 0) {
            return '0';
        }
        return Character.forDigit(10 - remainder, 10);
    }
}
