package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class SrvaEventStatistics implements Serializable {

    public static final SrvaEventStatistics reduce(@Nullable final SrvaEventStatistics first,
                                                   @Nullable final SrvaEventStatistics second) {

        final SrvaEventStatistics result = new SrvaEventStatistics();

        result.setAccident(reduceSpeciesCounts(first, second, SrvaEventStatistics::getAccident));
        result.setDeportation(reduceSpeciesCounts(first, second, SrvaEventStatistics::getDeportation));
        result.setInjury(reduceSpeciesCounts(first, second, SrvaEventStatistics::getInjury));

        result.setTrafficAccidents(nullableIntSum(first, second, SrvaEventStatistics::getTrafficAccidents));
        result.setRailwayAccidents(nullableIntSum(first, second, SrvaEventStatistics::getRailwayAccidents));
        result.setOtherAccidents(nullableIntSum(first, second, SrvaEventStatistics::getOtherAccidents));

        result.setTotalSrvaWorkHours(nullableIntSum(first, second, SrvaEventStatistics::getTotalSrvaWorkHours));
        result.setSrvaParticipants(nullableIntSum(first, second, SrvaEventStatistics::getSrvaParticipants));

        return result;
    }

    public static SrvaEventStatistics reduce(@Nonnull final Stream<SrvaEventStatistics> items) {
        requireNonNull(items);
        return items.reduce(new SrvaEventStatistics(), SrvaEventStatistics::reduce);
    }

    public static <T> SrvaEventStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                 @Nonnull final Function<? super T, SrvaEventStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    private static SrvaSpeciesCountStatistics reduceSpeciesCounts(final SrvaEventStatistics a,
                                                                  final SrvaEventStatistics b,
                                                                  final Function<SrvaEventStatistics, SrvaSpeciesCountStatistics> extractor) {

        return SrvaSpeciesCountStatistics.reduce(Stream.of(a, b).filter(Objects::nonNull).map(extractor));
    }

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mooses", column = @Column(name = "srva_moose_accidents")),
            @AttributeOverride(name = "whiteTailedDeers", column = @Column(name = "srva_white_tailed_deer_accidents")),
            @AttributeOverride(name = "roeDeers", column = @Column(name = "srva_roe_deer_accidents")),
            @AttributeOverride(
                    name = "wildForestReindeers", column = @Column(name = "srva_wild_forest_reindeer_accidents")),
            @AttributeOverride(name = "fallowDeers", column = @Column(name = "srva_fallow_deer_accidents")),
            @AttributeOverride(name = "wildBoars", column = @Column(name = "srva_wild_boar_accidents")),
            @AttributeOverride(name = "lynxes", column = @Column(name = "srva_lynx_accidents")),
            @AttributeOverride(name = "bears", column = @Column(name = "srva_bear_accidents")),
            @AttributeOverride(name = "wolves", column = @Column(name = "srva_wolf_accidents")),
            @AttributeOverride(name = "wolverines", column = @Column(name = "srva_wolverine_accidents")),
            @AttributeOverride(name = "otherSpecies", column = @Column(name = "srva_other_species_accidents")),
    })
    private SrvaSpeciesCountStatistics accident;

    @Min(0)
    @Column(name = "srva_traffic_accidents")
    private Integer trafficAccidents;

    @Min(0)
    @Column(name = "srva_railway_accidents")
    private Integer railwayAccidents;

    @Min(0)
    @Column(name = "srva_other_accidents")
    private Integer otherAccidents;

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mooses", column = @Column(name = "srva_moose_deportations")),
            @AttributeOverride(
                    name = "whiteTailedDeers", column = @Column(name = "srva_white_tailed_deer_deportations")),
            @AttributeOverride(name = "roeDeers", column = @Column(name = "srva_roe_deer_deportations")),
            @AttributeOverride(
                    name = "wildForestReindeers", column = @Column(name = "srva_wild_forest_reindeer_deportations")),
            @AttributeOverride(name = "fallowDeers", column = @Column(name = "srva_fallow_deer_deportations")),
            @AttributeOverride(name = "wildBoars", column = @Column(name = "srva_wild_boar_deportations")),
            @AttributeOverride(name = "lynxes", column = @Column(name = "srva_lynx_deportations")),
            @AttributeOverride(name = "bears", column = @Column(name = "srva_bear_deportations")),
            @AttributeOverride(name = "wolves", column = @Column(name = "srva_wolf_deportations")),
            @AttributeOverride(name = "wolverines", column = @Column(name = "srva_wolverine_deportations")),
            @AttributeOverride(name = "otherSpecies", column = @Column(name = "srva_other_species_deportations")),
    })
    private SrvaSpeciesCountStatistics deportation;

    @Valid
    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mooses", column = @Column(name = "srva_moose_injuries")),
            @AttributeOverride(name = "whiteTailedDeers", column = @Column(name = "srva_white_tailed_deer_injuries")),
            @AttributeOverride(name = "roeDeers", column = @Column(name = "srva_roe_deer_injuries")),
            @AttributeOverride(
                    name = "wildForestReindeers", column = @Column(name = "srva_wild_forest_reindeer_injuries")),
            @AttributeOverride(name = "fallowDeers", column = @Column(name = "srva_fallow_deer_injuries")),
            @AttributeOverride(name = "wildBoars", column = @Column(name = "srva_wild_boar_injuries")),
            @AttributeOverride(name = "lynxes", column = @Column(name = "srva_lynx_injuries")),
            @AttributeOverride(name = "bears", column = @Column(name = "srva_bear_injuries")),
            @AttributeOverride(name = "wolves", column = @Column(name = "srva_wolf_injuries")),
            @AttributeOverride(name = "wolverines", column = @Column(name = "srva_wolverine_injuries")),
            @AttributeOverride(name = "otherSpecies", column = @Column(name = "srva_other_species_injuries")),
    })
    private SrvaSpeciesCountStatistics injury;

    @Min(0)
    @Column(name = "srva_total_work_hours")
    private Integer totalSrvaWorkHours;

    @Min(0)
    @Column(name = "srva_participants")
    private Integer srvaParticipants;

    public SrvaEventStatistics() {
        this.accident = new SrvaSpeciesCountStatistics();
        this.deportation = new SrvaSpeciesCountStatistics();
        this.injury = new SrvaSpeciesCountStatistics();
    }

    public SrvaEventStatistics(@Nonnull final SrvaSpeciesCountStatistics accident,
                               @Nonnull final SrvaSpeciesCountStatistics deportation,
                               @Nonnull final SrvaSpeciesCountStatistics injury) {

        this.accident = requireNonNull(accident);
        this.deportation = requireNonNull(deportation);
        this.injury = requireNonNull(injury);
    }

    @JsonGetter(value = "allEvents")
    @Nullable
    public Integer countAllSrvaEvents() {
        return nullableIntSum(accident.countAll(), deportation.countAll(), injury.countAll());
    }

    @JsonGetter(value = "allMooselikes")
    @Nullable
    public Integer countMooselikes() {
        return nullableIntSum(accident.countMooselikes(), deportation.countMooselikes(), injury.countMooselikes());
    }

    // Accessors -->

    public SrvaSpeciesCountStatistics getAccident() {
        return accident;
    }

    public void setAccident(final SrvaSpeciesCountStatistics accident) {
        this.accident = accident;
    }

    public Integer getTrafficAccidents() {
        return trafficAccidents;
    }

    public void setTrafficAccidents(final Integer trafficAccidents) {
        this.trafficAccidents = trafficAccidents;
    }

    public Integer getRailwayAccidents() {
        return railwayAccidents;
    }

    public void setRailwayAccidents(final Integer railwayAccidents) {
        this.railwayAccidents = railwayAccidents;
    }

    public Integer getOtherAccidents() {
        return otherAccidents;
    }

    public void setOtherAccidents(final Integer otherAccidents) {
        this.otherAccidents = otherAccidents;
    }

    public SrvaSpeciesCountStatistics getDeportation() {
        return deportation;
    }

    public void setDeportation(final SrvaSpeciesCountStatistics deportation) {
        this.deportation = deportation;
    }

    public SrvaSpeciesCountStatistics getInjury() {
        return injury;
    }

    public void setInjury(final SrvaSpeciesCountStatistics injury) {
        this.injury = injury;
    }

    public Integer getTotalSrvaWorkHours() {
        return totalSrvaWorkHours;
    }

    public void setTotalSrvaWorkHours(final Integer totalSrvaWorkHours) {
        this.totalSrvaWorkHours = totalSrvaWorkHours;
    }

    public Integer getSrvaParticipants() {
        return srvaParticipants;
    }

    public void setSrvaParticipants(final Integer srvaParticipants) {
        this.srvaParticipants = srvaParticipants;
    }
}
