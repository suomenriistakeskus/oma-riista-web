package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.FieldPresence;

/**
 * Models observation field presence that cannot be statically verified.
 * Currently, VOLUNTARY_CARNIVORE_AUTHORITY constant indicates that field is
 * voluntary if and only if the authenticated user has an active carnivore
 * authority (petoyhdyshenkilö) occupation in any RHY.
 */
public enum DynamicObservationFieldPresence {

    YES,
    VOLUNTARY,
    VOLUNTARY_CARNIVORE_AUTHORITY,
    NO;

    public boolean isPresentInAnyContext() {
        return this != NO;
    }

    public boolean nonNullValueRequired() {
        return this == YES;
    }

    public FieldPresence toSimpleFieldPresence(final boolean userHasCarnivoreAuthority) {
        return new FieldPresence() {
            @Override
            public boolean nullValueRequired() {
                final DynamicObservationFieldPresence enumValue = DynamicObservationFieldPresence.this;
                return enumValue == NO || !userHasCarnivoreAuthority && enumValue == VOLUNTARY_CARNIVORE_AUTHORITY;
            }

            @Override
            public boolean nonNullValueRequired() {
                return DynamicObservationFieldPresence.this.nonNullValueRequired();
            }
        };
    }
}
