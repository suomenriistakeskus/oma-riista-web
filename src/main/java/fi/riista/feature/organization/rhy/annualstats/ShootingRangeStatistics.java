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
public class ShootingRangeStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsNonComputedFields<ShootingRangeStatistics>,
        Serializable {

    public static final ShootingRangeStatistics reduce(@Nullable final ShootingRangeStatistics a,
                                                       @Nullable final ShootingRangeStatistics b) {

        final ShootingRangeStatistics result = new ShootingRangeStatistics();
        result.setMooseRanges(nullableIntSum(a, b, ShootingRangeStatistics::getMooseRanges));
        result.setShotgunRanges(nullableIntSum(a, b, ShootingRangeStatistics::getShotgunRanges));
        result.setRifleRanges(nullableIntSum(a, b, ShootingRangeStatistics::getRifleRanges));
        result.setOtherShootingRanges(nullableIntSum(a, b, ShootingRangeStatistics::getOtherShootingRanges));
        result.setLastModified(nullsafeMax(a, b, ShootingRangeStatistics::getLastModified));
        return result;
    }

    public static ShootingRangeStatistics reduce(@Nonnull final Stream<ShootingRangeStatistics> items) {
        requireNonNull(items);
        return items.reduce(new ShootingRangeStatistics(), ShootingRangeStatistics::reduce);
    }

    public static <T> ShootingRangeStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                     @Nonnull final Function<? super T, ShootingRangeStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Hirviratojen lukumäärä
    @Min(0)
    @Column(name = "moose_ranges")
    private Integer mooseRanges;

    // Haulikkoratojen lukumäärä
    @Min(0)
    @Column(name = "shotgun_ranges")
    private Integer shotgunRanges;

    // Luodikkoratojen lukumäärä
    @Min(0)
    @Column(name = "rifle_ranges")
    private Integer rifleRanges;

    // Muiden ampumaratojen (jousi, SRVA) lukumäärä
    @Min(0)
    @Column(name = "other_shooting_ranges")
    private Integer otherShootingRanges;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "shooting_ranges_last_modified")
    private DateTime lastModified;

    public ShootingRangeStatistics() {
    }

    public ShootingRangeStatistics(@Nonnull final ShootingRangeStatistics that) {
        requireNonNull(that);

        this.mooseRanges = that.mooseRanges;
        this.shotgunRanges = that.shotgunRanges;
        this.rifleRanges = that.rifleRanges;
        this.otherShootingRanges = that.otherShootingRanges;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.SHOOTING_RANGES;
    }

    @Override
    public boolean isEqualTo(@Nonnull final ShootingRangeStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(mooseRanges, that.mooseRanges) &&
                Objects.equals(shotgunRanges, that.shotgunRanges) &&
                Objects.equals(rifleRanges, that.rifleRanges) &&
                Objects.equals(otherShootingRanges, that.otherShootingRanges);
    }

    @Override
    public void assignFrom(@Nonnull final ShootingRangeStatistics that) {
        // Includes manually updateable fields only.

        this.mooseRanges = that.mooseRanges;
        this.shotgunRanges = that.shotgunRanges;
        this.rifleRanges = that.rifleRanges;
        this.otherShootingRanges = that.otherShootingRanges;
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(mooseRanges, shotgunRanges, rifleRanges, otherShootingRanges);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    // Accessors -->

    public Integer getMooseRanges() {
        return mooseRanges;
    }

    public void setMooseRanges(final Integer mooseRanges) {
        this.mooseRanges = mooseRanges;
    }

    public Integer getShotgunRanges() {
        return shotgunRanges;
    }

    public void setShotgunRanges(final Integer shotgunRanges) {
        this.shotgunRanges = shotgunRanges;
    }

    public Integer getRifleRanges() {
        return rifleRanges;
    }

    public void setRifleRanges(final Integer rifleRanges) {
        this.rifleRanges = rifleRanges;
    }

    public Integer getOtherShootingRanges() {
        return otherShootingRanges;
    }

    public void setOtherShootingRanges(final Integer otherShootingRanges) {
        this.otherShootingRanges = otherShootingRanges;
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
