package fi.riista.validation;

public final class Validators {

    public static boolean isValidSsn(final String input) {
        return FinnishSocialSecurityNumberValidator.validate(input, true);
    }

    public static boolean isValidHunterNumber(final String input) {
        return FinnishHunterNumberValidator.validate(input, true);
    }

    public static boolean isValidPermitNumber(final String input) {
        return FinnishHuntingPermitNumberValidator.validate(input, true);
    }

    public static boolean isValidHuntingClubOfficialCode(final String input) {
        return input != null && input.length() == 7 && input.matches("\\d+");
    }

    public static boolean isValidBusinessId(final String input) {
        return FinnishBusinessIdValidator.validate(input, true);
    }

    private Validators() {
        throw new AssertionError();
    }
}
