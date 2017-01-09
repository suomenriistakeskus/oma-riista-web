package fi.riista.validation;


import com.google.common.base.Strings;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String>, Validator {

    public static final String DEFAULT_REGION = "FI";

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
    }

    @Override
    public boolean isValid(String inputValue, ConstraintValidatorContext context) {
        if (Strings.isNullOrEmpty(inputValue)) {
            return true;
        }

        try {
            validateAndFormat(inputValue, DEFAULT_REGION);

            return true;

        } catch (NumberParseException e) {
            return false;
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return String.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String number = String.class.cast(target);
        try {
            validateAndFormat(number);
        } catch (NumberParseException e) {
            errors.reject(e.getMessage());
        }
    }

    public static String validateAndFormat(String number) throws NumberParseException {
        return validateAndFormat(number, DEFAULT_REGION);
    }

    public static String validateAndFormat(String number, String region) throws NumberParseException {
        Phonenumber.PhoneNumber phoneNumber = validate(number, region);
        return format(phoneNumber);
    }

    private static Phonenumber.PhoneNumber validate(String number, String region) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, region);
        if (!phoneUtil.isValidNumber(phoneNumber)) {
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Not a valid phone number:" + number);
        }
        return phoneNumber;
    }

    private static String format(Phonenumber.PhoneNumber phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }
}
