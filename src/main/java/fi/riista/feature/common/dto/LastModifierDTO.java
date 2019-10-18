package fi.riista.feature.common.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.person.Person;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class LastModifierDTO implements Serializable {

    private final String firstName;
    private final String lastName;

    private final DateTime timestamp;
    private final boolean adminOrModerator;

    public static LastModifierDTO createForAutomatedTask(@Nonnull final DateTime timestamp) {
        return new LastModifierDTO(timestamp);
    }

    public static LastModifierDTO createForAdminOrModerator(@Nonnull final SystemUser user,
                                                            @Nonnull final DateTime timestamp) {

        requireNonNull(user);
        checkArgument(user.isModeratorOrAdmin(), "User expected to have admin or moderator role");

        return new LastModifierDTO(user.getFirstName(), user.getLastName(), timestamp, true);
    }

    public static LastModifierDTO createForPerson(@Nonnull final Person person, @Nonnull final DateTime timestamp) {

        requireNonNull(person);

        return new LastModifierDTO(person.getFirstName(), person.getLastName(), timestamp, false);
    }

    private LastModifierDTO(@Nonnull final DateTime timestamp) {
        this.timestamp = requireNonNull(timestamp);
        this.adminOrModerator = true;
        this.firstName = null;
        this.lastName = null;
    }

    private LastModifierDTO(@Nullable final String firstName,
                            @Nullable final String lastName,
                            @Nonnull final DateTime timestamp,
                            final boolean adminOrModerator) {

        if (!adminOrModerator) {
            checkArgument(!isBlank(firstName), "firstName must not be empty");
            checkArgument(!isBlank(lastName), "lastName must not be empty");
        }

        if (firstName == null || lastName == null) {
            this.firstName = null;
            this.lastName = null;
        } else {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        this.timestamp = requireNonNull(timestamp);
        this.adminOrModerator = adminOrModerator;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof LastModifierDTO)) {
            return false;
        } else {
            final LastModifierDTO that = (LastModifierDTO) o;

            return Objects.equals(this.firstName, that.firstName)
                    && Objects.equals(this.lastName, that.lastName)
                    && Objects.equals(this.timestamp, that.timestamp)
                    && this.adminOrModerator == that.adminOrModerator;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, timestamp, adminOrModerator);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getFullName() {
        return firstName != null && lastName != null ? String.format("%s %s", firstName, lastName) : null;
    }

    @JsonGetter("timestamp")
    public LocalDateTime getTimestampAsLocalDateTime() {
        return timestamp.toLocalDateTime();
    }

    // Accessors -->

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @JsonIgnore
    public DateTime getTimestamp() {
        return timestamp;
    }

    public boolean isAdminOrModerator() {
        return adminOrModerator;
    }
}
