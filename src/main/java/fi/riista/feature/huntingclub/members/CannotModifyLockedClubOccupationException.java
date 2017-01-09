package fi.riista.feature.huntingclub.members;

public class CannotModifyLockedClubOccupationException extends RuntimeException {
    public CannotModifyLockedClubOccupationException(final String msg) {
        super(msg);
    }
}
