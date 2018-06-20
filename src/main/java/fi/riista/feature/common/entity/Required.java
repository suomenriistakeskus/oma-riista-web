package fi.riista.feature.common.entity;

public enum Required implements FieldPresence {
    YES, NO, VOLUNTARY;

    @Override
    public boolean nullValueRequired() {
        return this == NO;
    }

    @Override
    public boolean nonNullValueRequired() {
        return this == YES;
    }
}
