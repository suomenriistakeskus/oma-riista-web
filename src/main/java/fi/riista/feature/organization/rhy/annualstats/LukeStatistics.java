package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
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
public class LukeStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<LukeStatistics>, Serializable {

    public static final LukeStatistics reduce(@Nullable final LukeStatistics a, @Nullable final LukeStatistics b) {
        final LukeStatistics result = new LukeStatistics();
        result.setWinterGameTriangles(nullsafeSumAsInt(a, b, LukeStatistics::getWinterGameTriangles));
        result.setSummerGameTriangles(nullsafeSumAsInt(a, b, LukeStatistics::getSummerGameTriangles));
        result.setFieldTriangles(nullsafeSumAsInt(a, b, LukeStatistics::getFieldTriangles));
        result.setWaterBirds(nullsafeSumAsInt(a, b, LukeStatistics::getWaterBirds));
        result.setLastModified(nullsafeMax(a, b, LukeStatistics::getLastModified));
        return result;
    }

    public static LukeStatistics reduce(@Nonnull final Stream<LukeStatistics> items) {
        requireNonNull(items);
        return items.reduce(new LukeStatistics(), LukeStatistics::reduce);
    }

    public static <T> LukeStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                            @Nonnull final Function<? super T, LukeStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Lasketut talviriistakolmiot
    @Min(0)
    @Column(name = "luke_winter_game_triangles")
    private Integer winterGameTriangles;

    // Lasketut kesäriistakolmiot
    @Min(0)
    @Column(name = "luke_summer_game_triangles")
    private Integer summerGameTriangles;

    // Lasketut peltokolmiot
    @Min(0)
    @Column(name = "luke_field_triangles")
    private Integer fieldTriangles;

    // Vesilintujen laskentapisteet
    @Min(0)
    @Column(name = "luke_water_birds")
    private Integer waterBirds;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "luke_game_calculations_last_modified")
    private DateTime lastModified;

    public LukeStatistics() {
    }

    public LukeStatistics(@Nonnull final LukeStatistics that) {
        Objects.requireNonNull(that);

        this.winterGameTriangles = that.winterGameTriangles;
        this.summerGameTriangles = that.summerGameTriangles;
        this.fieldTriangles = that.fieldTriangles;
        this.waterBirds = that.waterBirds;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final LukeStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(winterGameTriangles, other.winterGameTriangles) &&
                Objects.equals(summerGameTriangles, other.summerGameTriangles) &&
                Objects.equals(fieldTriangles, other.fieldTriangles) &&
                Objects.equals(waterBirds, other.waterBirds);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return true;
    }

    @Override
    public boolean isCompleteForApproval() {
        return F.allNotNull(winterGameTriangles, summerGameTriangles, fieldTriangles, waterBirds);
    }

    public int sumOfWinterAndSummerGameTriangles() {
        return NumberUtils.getIntValueOrZero(winterGameTriangles) + NumberUtils.getIntValueOrZero(summerGameTriangles);
    }

    @JsonGetter(value = "sum")
    public int sumOfAllLukeCalculations() {
        return Stream
                .of(winterGameTriangles, summerGameTriangles, fieldTriangles, waterBirds)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    // Accessors -->

    public Integer getWinterGameTriangles() {
        return winterGameTriangles;
    }

    public void setWinterGameTriangles(final Integer winterGameTriangles) {
        this.winterGameTriangles = winterGameTriangles;
    }

    public Integer getSummerGameTriangles() {
        return summerGameTriangles;
    }

    public void setSummerGameTriangles(final Integer summerGameTriangles) {
        this.summerGameTriangles = summerGameTriangles;
    }

    public Integer getFieldTriangles() {
        return fieldTriangles;
    }

    public void setFieldTriangles(final Integer fieldTriangles) {
        this.fieldTriangles = fieldTriangles;
    }

    public Integer getWaterBirds() {
        return waterBirds;
    }

    public void setWaterBirds(final Integer waterBirds) {
        this.waterBirds = waterBirds;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
