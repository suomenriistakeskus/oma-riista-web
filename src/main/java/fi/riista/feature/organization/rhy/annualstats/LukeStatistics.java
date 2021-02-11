package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
import org.hibernate.annotations.Type;
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
public class LukeStatistics
        implements AnnualStatisticsFieldsetReadiness, AnnualStatisticsManuallyEditableFields<LukeStatistics>, Serializable {

    public static final LukeStatistics reduce(@Nullable final LukeStatistics a, @Nullable final LukeStatistics b) {
        final LukeStatistics result = new LukeStatistics();
        result.setWinterGameTriangles(nullableIntSum(a, b, LukeStatistics::getWinterGameTriangles));
        result.setSummerGameTriangles(nullableIntSum(a, b, LukeStatistics::getSummerGameTriangles));
        result.setFieldTriangles(nullableIntSum(a, b, LukeStatistics::getFieldTriangles));
        result.setWaterBirdBroods(nullableIntSum(a, b, LukeStatistics::getWaterBirdBroods));
        result.setWaterBirdCouples(nullableIntSum(a, b, LukeStatistics::getWaterBirdCouples));
        result.setNorthernLaplandWillowGrouseLines(nullableIntSum(a, b, LukeStatistics::getNorthernLaplandWillowGrouseLines));
        result.setCarnivoreContactPersons(nullableIntSum(a, b, LukeStatistics::getCarnivoreContactPersons));
        result.setCarnivoreDnaCollectors(nullableIntSum(a, b, LukeStatistics::getCarnivoreDnaCollectors));
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

    // Lasketut vesilintujen laskentapisteet poikuelaskennoissa
    @Min(0)
    @Column(name = "luke_water_bird_broods")
    private Integer waterBirdBroods;

    // Lasketut vesilintujen laskentapisteet parilaskennoissa
    @Min(0)
    @Column(name = "luke_water_bird_couples")
    private Integer waterBirdCouples;

    // Lasketut Ylä-Lapin riekkojen linjalaskennat
    @Min(0)
    @Column(name = "luke_northern_lapland_willow_grouse_lines")
    private Integer northernLaplandWillowGrouseLines;

    // Suurpetohavaintoja kirjanneiden petoyhdyshenkilöiden määrä, hlö
    @Min(0)
    @Column(name = "luke_carnivore_contact_persons")
    private Integer carnivoreContactPersons;

    // Suurpetojen DNA-näytteitä keränneiden henkilöiden määrä
    @Min(0)
    @Column(name = "luke_carnivore_dna_collectors")
    private Integer carnivoreDnaCollectors;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "luke_game_calculations_last_modified")
    private DateTime lastModified;

    public LukeStatistics() {
    }

    public LukeStatistics(@Nonnull final LukeStatistics that) {
        requireNonNull(that);

        this.winterGameTriangles = that.winterGameTriangles;
        this.summerGameTriangles = that.summerGameTriangles;
        this.fieldTriangles = that.fieldTriangles;
        this.waterBirdBroods = that.waterBirdBroods;
        this.waterBirdCouples = that.waterBirdCouples;
        this.northernLaplandWillowGrouseLines = that.northernLaplandWillowGrouseLines;
        this.carnivoreContactPersons = that.carnivoreContactPersons;
        this.carnivoreDnaCollectors = that.carnivoreDnaCollectors;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.LUKE;
    }

    @Override
    public boolean isEqualTo(@Nonnull final LukeStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(winterGameTriangles, that.winterGameTriangles) &&
                Objects.equals(summerGameTriangles, that.summerGameTriangles) &&
                Objects.equals(fieldTriangles, that.fieldTriangles) &&
                Objects.equals(waterBirdBroods, that.waterBirdBroods) &&
                Objects.equals(waterBirdCouples, that.waterBirdCouples) &&
                Objects.equals(northernLaplandWillowGrouseLines, that.northernLaplandWillowGrouseLines) &&
                Objects.equals(carnivoreContactPersons, that.carnivoreContactPersons) &&
                Objects.equals(carnivoreDnaCollectors, that.carnivoreDnaCollectors);
    }

    @Override
    public void assignFrom(@Nonnull final LukeStatistics that) {
        // Includes manually updateable fields only.

        this.winterGameTriangles = that.winterGameTriangles;
        this.summerGameTriangles = that.summerGameTriangles;
        this.fieldTriangles = that.fieldTriangles;
        this.waterBirdBroods = that.waterBirdBroods;
        this.waterBirdCouples = that.waterBirdCouples;
        this.northernLaplandWillowGrouseLines = that.northernLaplandWillowGrouseLines;
        this.carnivoreContactPersons = that.carnivoreContactPersons;
        this.carnivoreDnaCollectors = that.carnivoreDnaCollectors;
    }

    @Override
    public boolean isReadyForInspection() {
        return true;
    }

    @Override
    public boolean isCompleteForApproval() {
        return F.allNotNull(
                winterGameTriangles, summerGameTriangles, fieldTriangles, waterBirdBroods, waterBirdCouples,
                northernLaplandWillowGrouseLines, carnivoreContactPersons, carnivoreDnaCollectors);
    }

    public Integer sumOfWinterAndSummerGameTriangles() {
        return nullableIntSum(winterGameTriangles, summerGameTriangles);
    }

    public Integer sumOfWaterBirdCalculationLocations() {
        return nullableIntSum(waterBirdBroods, waterBirdCouples);
    }

    @JsonGetter(value = "sum")
    public Integer sumOfAllLukeCalculations() {
        return nullableIntSum(
                winterGameTriangles, summerGameTriangles, fieldTriangles, waterBirdBroods, waterBirdCouples,
                northernLaplandWillowGrouseLines);
    }

    public Integer sumOfAllLukeCalculations2018() {
        return nullableIntSum(
                winterGameTriangles, summerGameTriangles, fieldTriangles, waterBirdBroods, waterBirdCouples);
    }

    public Integer sumOfCarnivorePersons() {
        return nullableIntSum(carnivoreContactPersons, carnivoreDnaCollectors);
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

    public Integer getWaterBirdBroods() {
        return waterBirdBroods;
    }

    public void setWaterBirdBroods(final Integer waterBirdBroods) {
        this.waterBirdBroods = waterBirdBroods;
    }

    public Integer getWaterBirdCouples() {
        return waterBirdCouples;
    }

    public void setWaterBirdCouples(final Integer waterBirdCouples) {
        this.waterBirdCouples = waterBirdCouples;
    }

    public Integer getNorthernLaplandWillowGrouseLines() {
        return northernLaplandWillowGrouseLines;
    }

    public void setNorthernLaplandWillowGrouseLines(final Integer northernLaplandWillowGrouseLines) {
        this.northernLaplandWillowGrouseLines = northernLaplandWillowGrouseLines;
    }

    public Integer getCarnivoreContactPersons() {
        return carnivoreContactPersons;
    }

    public void setCarnivoreContactPersons(final Integer carnivoreContactPersons) {
        this.carnivoreContactPersons = carnivoreContactPersons;
    }

    public Integer getCarnivoreDnaCollectors() {
        return carnivoreDnaCollectors;
    }

    public void setCarnivoreDnaCollectors(final Integer carnivoreDnaCollectors) {
        this.carnivoreDnaCollectors = carnivoreDnaCollectors;
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
