package fi.riista.feature.organization.person;

public class PersonIsDeceasedException extends IllegalStateException {
    public PersonIsDeceasedException() {
    }

    public PersonIsDeceasedException(final String s) {
        super(s);
    }
}
