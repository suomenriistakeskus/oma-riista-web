package fi.riista.validation;


import com.google.common.collect.Iterators;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Finnish business id is 7 numbers + '-' + checksum.
 * Numbers can start with one zero.
 */
public class FinnishBusinessIdValidator implements ConstraintValidator<FinnishBusinessId, String> {
    private static final Pattern REGEX_PATTERN = Pattern.compile("[0-9]{7}-[0-9]");
    private static final int VALID_LENGTH = 7 + 2;

    private boolean verifyChecksum;

    public FinnishBusinessIdValidator() {
        this(true);
    }

    public FinnishBusinessIdValidator(boolean verifyChecksum) {
        this.verifyChecksum = verifyChecksum;
    }

    @Override
    public void initialize(final FinnishBusinessId finnishBusinessId) {
        this.verifyChecksum = finnishBusinessId.verifyChecksum();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        return validate(value, this.verifyChecksum);
    }

    public static boolean validate(final String value, final boolean verifyChecksum) {
        if (!verifyChecksum) {
            return true;
        }
        if (!StringUtils.hasText(value)) {
            return true;
        }

        if (value.length() != VALID_LENGTH) {
            return false;
        }

        if (!REGEX_PATTERN.matcher(value).matches()) {
            return false;
        }

        final char calculatedChecksum = calculateChecksum(value);
        if (calculatedChecksum == '1') {
            return false;
        }
        final char checksum = value.charAt(value.length() - 1);
        return checksum == calculatedChecksum;
    }

    public static char calculateChecksum(final String s) {
        int sum = 0;

        final Iterator<Integer> weightsIterator = Iterators.cycle(7, 9, 10, 5, 8, 4, 2);
        for (int i = 0; i < s.length() - 2; i++) {
            sum += Character.getNumericValue(s.charAt(i)) * weightsIterator.next();
        }

        final int remainder = sum % 11;

        if (remainder == 0) {
            return '0';
        } else if (remainder == 1) {
            return 'x';
        } else {
            return Character.forDigit(11 - remainder, 10);
        }
    }
}
