package fi.riista.feature.shootingtest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public interface ShootingTestParticipantRepositoryCustom {

    Map<Long, ParticipantSummary> getParticipantSummaryByShootingTestEventId(Collection<ShootingTestEvent> events);

    static class ParticipantSummary {

        public static final ParticipantSummary EMPTY = new ParticipantSummary(0, 0, 0, BigDecimal.ZERO);

        final int numberOfAllParticipants;
        final int numberOfCompletedParticipants;
        final int numberOfParticipantsWithNoAttempts;
        final BigDecimal totalPaidAmount;

        public ParticipantSummary(@Nonnull final Integer numberOfAllParticipants,
                                  @Nonnull final Integer numberOfCompletedParticipants,
                                  @Nonnull final Integer numberOfParticipantsWithNoAttempts,
                                  @Nonnull final BigDecimal totalPaidAmount) {

            this.numberOfAllParticipants = numberOfAllParticipants.intValue();
            this.numberOfCompletedParticipants = numberOfCompletedParticipants.intValue();
            this.numberOfParticipantsWithNoAttempts = numberOfParticipantsWithNoAttempts.intValue();
            this.totalPaidAmount = Objects.requireNonNull(totalPaidAmount, "totalPaidAmount is null");
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ParticipantSummary)) {
                return false;
            }

            final ParticipantSummary that = (ParticipantSummary) o;

            return numberOfAllParticipants == that.numberOfAllParticipants
                    && numberOfCompletedParticipants == that.numberOfCompletedParticipants
                    && numberOfParticipantsWithNoAttempts == that.numberOfParticipantsWithNoAttempts
                    && totalPaidAmount.compareTo(that.totalPaidAmount) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    numberOfAllParticipants, numberOfCompletedParticipants, numberOfParticipantsWithNoAttempts,
                    totalPaidAmount);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
