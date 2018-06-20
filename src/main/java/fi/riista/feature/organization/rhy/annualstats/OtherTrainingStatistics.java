package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class OtherTrainingStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<OtherTrainingStatistics>, Serializable {

    public static final OtherTrainingStatistics reduce(@Nullable final OtherTrainingStatistics a,
                                                       @Nullable final OtherTrainingStatistics b) {

        final OtherTrainingStatistics result = new OtherTrainingStatistics();
        result.setOtherTrainingEvents(nullsafeSumAsInt(a, b, s -> s.getOtherTrainingEvents()));
        result.setOtherTrainingParticipants(nullsafeSumAsInt(a, b, s -> s.getOtherTrainingParticipants()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static OtherTrainingStatistics reduce(@Nonnull final Stream<OtherTrainingStatistics> items) {
        requireNonNull(items);
        return items.reduce(new OtherTrainingStatistics(), OtherTrainingStatistics::reduce);
    }

    public static <T> OtherTrainingStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                     @Nonnull final Function<? super T, OtherTrainingStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Muut koulutus- ja yleisötapahtumat, lkm
    @Min(0)
    @Column(name = "other_training_events")
    private Integer otherTrainingEvents;

    // Muiden koulutus- ja yleisötapahtumien osallistujat, lkm
    @Min(0)
    @Column(name = "other_training_participants")
    private Integer otherTrainingParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "other_training_last_modified")
    private DateTime lastModified;

    public OtherTrainingStatistics() {
    }

    public OtherTrainingStatistics(@Nonnull final OtherTrainingStatistics that) {
        Objects.requireNonNull(that);

        this.otherTrainingEvents = that.otherTrainingEvents;
        this.otherTrainingParticipants = that.otherTrainingParticipants;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final OtherTrainingStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(otherTrainingEvents, other.otherTrainingEvents) &&
                Objects.equals(otherTrainingParticipants, other.otherTrainingParticipants);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(otherTrainingEvents, otherTrainingParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getOtherTrainingEvents() {
        return otherTrainingEvents;
    }

    public void setOtherTrainingEvents(final Integer otherTrainingEvents) {
        this.otherTrainingEvents = otherTrainingEvents;
    }

    public Integer getOtherTrainingParticipants() {
        return otherTrainingParticipants;
    }

    public void setOtherTrainingParticipants(final Integer otherTrainingParticipants) {
        this.otherTrainingParticipants = otherTrainingParticipants;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
