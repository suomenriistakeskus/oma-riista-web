package fi.riista.validation;

import org.iban4j.CountryCode;
import org.iban4j.Iban4jException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IbanValidator implements ConstraintValidator<Iban, String> {

    private boolean allowOnlyFinnish;

    public IbanValidator() {
        this(true);
    }

    public IbanValidator(final boolean allowOnlyFinnish) {
        this.allowOnlyFinnish = allowOnlyFinnish;
    }

    @Override
    public void initialize(final Iban iban) {
        this.allowOnlyFinnish = iban.allowOnlyFinnish();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        try {
            final org.iban4j.Iban iban = getValidIbanOrThrow(value);

            if (!allowOnlyFinnish) {
                return true;
            }
            return CountryCode.FI == iban.getCountryCode();

        } catch (final Iban4jException e) {
            return false;
        }
    }

    private static org.iban4j.Iban getValidIbanOrThrow(final String value) {
        // will throw exception if not valid
        return org.iban4j.Iban.valueOf(value.replace(" ", ""));
    }
}
