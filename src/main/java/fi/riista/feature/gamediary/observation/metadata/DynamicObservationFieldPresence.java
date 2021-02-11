package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.common.entity.FieldPresence;

/**
 * Models observation field presence that cannot be statically verified.
 * Currently, VOLUNTARY_CARNIVORE_AUTHORITY constant indicates that field is
 * voluntary if and only if the authenticated user has an active carnivore
 * authority (petoyhdyshenkilö) occupation in any RHY.
 *
 * YES_DEER_PILOT and VOLUNTARY_DEER_PILOT constants indicates that the fields
 * are visible only if the authenticated user belongs to deer pilot (see
 * DeerPilotService class).
 */
public enum DynamicObservationFieldPresence {

    YES,
    YES_DEER_PILOT,
    VOLUNTARY,
    VOLUNTARY_CARNIVORE_AUTHORITY,
    VOLUNTARY_DEER_PILOT,
    NO;

    public boolean isPresentInAnyContext() {
        return this != NO;
    }

    public boolean isCarnivoreFieldAllowed(final boolean hasCarnivoreAuthority) {
        return toSimpleFieldPresence(hasCarnivoreAuthority, false).isNonNullValueLegal();
    }

    public boolean isDeerHuntingFieldAllowed(final boolean isInDeerPilot) {
        return toSimpleFieldPresence(false, isInDeerPilot).isNonNullValueLegal();
    }

    public FieldPresence toSimpleFieldPresence(final boolean userHasCarnivoreAuthority, final boolean isInDeerPilot) {
        final DynamicObservationFieldPresence enumValue = this;

        return new FieldPresence() {
            @Override
            public boolean nullValueRequired() {
                return enumValue == NO
                        || !userHasCarnivoreAuthority && enumValue == VOLUNTARY_CARNIVORE_AUTHORITY
                        || !isInDeerPilot && (enumValue == YES_DEER_PILOT || enumValue == VOLUNTARY_DEER_PILOT);
            }

            @Override
            public boolean nonNullValueRequired() {
                return enumValue == YES
                        || isInDeerPilot && enumValue == YES_DEER_PILOT;
            }
        };
    }
}
