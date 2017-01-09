package fi.riista.feature.huntingclub.register;

public class RegisterHuntingClubException extends RuntimeException {
    public static RegisterHuntingClubException missingRhy(final String clubOfficialCode) {
        return new RegisterHuntingClubException("Could not determine RHY for clubOfficialCode=" + clubOfficialCode);
    }

    public RegisterHuntingClubException(final String message) {
        super(message);
    }
}
