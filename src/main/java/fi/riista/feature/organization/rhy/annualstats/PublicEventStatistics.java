package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
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
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class PublicEventStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsNonComputedFields<PublicEventStatistics>,
        Serializable {

    public static final PublicEventStatistics reduce(@Nullable final PublicEventStatistics a,
                                                     @Nullable final PublicEventStatistics b) {

        final PublicEventStatistics result = new PublicEventStatistics();
        result.setPublicEvents(nullableIntSum(a, b, s -> s.getPublicEvents()));
        result.setPublicEventParticipants(nullableIntSum(a, b, s -> s.getPublicEventParticipants()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static PublicEventStatistics reduce(@Nonnull final Stream<PublicEventStatistics> items) {
        requireNonNull(items);
        return items.reduce(new PublicEventStatistics(), PublicEventStatistics::reduce);
    }

    public static <T> PublicEventStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                   @Nonnull final Function<? super T, PublicEventStatistics> extractor) {
        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Yleisötapahtumat, lkm
    @Min(0)
    @Column(name = "public_events")
    private Integer publicEvents;

    // Yleisötapahtumien osallistujat, lkm
    @Min(0)
    @Column(name = "public_event_participants")
    private Integer publicEventParticipants;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "public_events_last_modified")
    private DateTime lastModified;

    public PublicEventStatistics() {
    }

    public PublicEventStatistics(@Nonnull final PublicEventStatistics that) {
        requireNonNull(that);

        this.publicEvents = that.publicEvents;
        this.publicEventParticipants = that.publicEventParticipants;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.PUBLIC_EVENTS;
    }

    @Override
    public boolean isEqualTo(@Nonnull final PublicEventStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(publicEvents, that.publicEvents) &&
                Objects.equals(publicEventParticipants, that.publicEventParticipants);
    }

    @Override
    public void assignFrom(@Nonnull final PublicEventStatistics that) {
        // Includes manually updateable fields only.

        this.publicEvents = that.publicEvents;
        this.publicEventParticipants = that.publicEventParticipants;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(publicEvents, publicEventParticipants);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getPublicEvents() {
        return publicEvents;
    }

    public void setPublicEvents(final Integer publicEvents) {
        this.publicEvents = publicEvents;
    }

    public Integer getPublicEventParticipants() {
        return publicEventParticipants;
    }

    public void setPublicEventParticipants(final Integer publicEventParticipants) {
        this.publicEventParticipants = publicEventParticipants;
    }

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
