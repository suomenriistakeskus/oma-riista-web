package fi.riista.feature.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class BaseEntityEventDTO implements Serializable {

    private final Actor actor;
    private final DateTime eventTime;
    private final Long eventId;

    public BaseEntityEventDTO(@Nullable final String firstName,
                              @Nullable final String lastName,
                              @Nonnull final DateTime eventTime,
                              @Nonnull final Long eventId) {

        this.actor = firstName != null || lastName != null ? new Actor(firstName, lastName) : null;
        this.eventTime = requireNonNull(eventTime);
        this.eventId = eventId;
    }

    // Accessors -->

    public Actor getActor() {
        return actor;
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    public Long getEventId() {
        return eventId;
    }

    public static class Actor {

        private final String firstName;
        private final String lastName;

        Actor(@Nullable final String firstName, @Nullable final String lastName) {
            checkArgument(firstName != null || lastName != null, "either firstname or lastname must be present");

            this.firstName = firstName;
            this.lastName = lastName;
        }

        @JsonProperty
        public String getFullName() {
            if (firstName == null) {
                return lastName;
            } else if (lastName == null) {
                return firstName;
            }

            return firstName + " " + lastName;
        }

        // Accessors -->

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
