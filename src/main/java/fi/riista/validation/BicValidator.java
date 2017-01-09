package fi.riista.validation;

import org.iban4j.CountryCode;
import org.iban4j.Iban4jException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BicValidator implements ConstraintValidator<Bic, String> {

    private boolean allowOnlyFinnish;

    public BicValidator() {
        this(true);
    }

    public BicValidator(final boolean allowOnlyFinnish) {
        this.allowOnlyFinnish = allowOnlyFinnish;
    }

    @Override
    public void initialize(final Bic iban) {
        this.allowOnlyFinnish = iban.allowOnlyFinnish();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        try {
            final org.iban4j.Bic bic = getValidBicOrThrow(value);

            if (!allowOnlyFinnish) {
                return true;
            }
            return CountryCode.FI == bic.getCountryCode();

        } catch (final Iban4jException e) {
            return false;
        }
    }

    private static org.iban4j.Bic getValidBicOrThrow(final String value) {
        // will throw exception if not valid
        return org.iban4j.Bic.valueOf(value);
    }
}
