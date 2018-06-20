package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class SrvaSpeciesCountStatistics implements Serializable {

    public static final SrvaSpeciesCountStatistics reduce(@Nullable final SrvaSpeciesCountStatistics first,
                                                          @Nullable final SrvaSpeciesCountStatistics second) {

        final SrvaSpeciesCountStatistics result = new SrvaSpeciesCountStatistics();
        result.setMooses(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getMooses));
        result.setWhiteTailedDeers(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getWhiteTailedDeers));
        result.setRoeDeers(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getRoeDeers));
        result.setWildForestReindeers(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getWildForestReindeers));
        result.setFallowDeers(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getFallowDeers));
        result.setWildBoars(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getWildBoars));
        result.setLynxes(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getLynxes));
        result.setBears(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getBears));
        result.setWolves(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getWolves));
        result.setWolverines(nullsafeSumAsInt(first, second, SrvaSpeciesCountStatistics::getWolverines));
        return result;
    }

    public static SrvaSpeciesCountStatistics reduce(@Nonnull final Stream<SrvaSpeciesCountStatistics> items) {
        requireNonNull(items);
        return items.reduce(new SrvaSpeciesCountStatistics(), SrvaSpeciesCountStatistics::reduce);
    }

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_MOOSE)
    private Integer mooses;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER)
    private Integer whiteTailedDeers;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_ROE_DEER)
    private Integer roeDeers;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER)
    private Integer wildForestReindeers;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_FALLOW_DEER)
    private Integer fallowDeers;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_WILD_BOAR)
    private Integer wildBoars;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_LYNX)
    private Integer lynxes;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_BEAR)
    private Integer bears;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_WOLF)
    private Integer wolves;

    @Min(0)
    @Column
    @JsonProperty("" + GameSpecies.OFFICIAL_CODE_WOLVERINE)
    private Integer wolverines;

    public SrvaSpeciesCountStatistics() {
    }

    public SrvaSpeciesCountStatistics(@Nonnull final SrvaSpeciesCountStatistics that) {
        Objects.requireNonNull(that);

        this.mooses = that.mooses;
        this.whiteTailedDeers = that.whiteTailedDeers;
        this.roeDeers = that.roeDeers;
        this.wildForestReindeers = that.wildForestReindeers;
        this.fallowDeers = that.fallowDeers;
        this.wildBoars = that.wildBoars;
        this.lynxes = that.lynxes;
        this.bears = that.bears;
        this.wolves = that.wolves;
        this.wolverines = that.wolverines;
    }

    @JsonGetter("all")
    public int countAll() {
        return Stream
                .of(mooses, whiteTailedDeers, roeDeers, wildForestReindeers, fallowDeers, wildBoars, lynxes, bears,
                        wolves, wolverines)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countMooselikes() {
        return Stream
                .of(mooses, whiteTailedDeers, roeDeers, wildForestReindeers, fallowDeers)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    public int countLargeCarnivores() {
        return Stream.of(lynxes, bears, wolves, wolverines).mapToInt(NumberUtils::getIntValueOrZero).sum();
    }

    // Accessors -->

    public Integer getMooses() {
        return mooses;
    }

    public void setMooses(final Integer mooses) {
        this.mooses = mooses;
    }

    public Integer getWhiteTailedDeers() {
        return whiteTailedDeers;
    }

    public void setWhiteTailedDeers(final Integer whiteTailedDeers) {
        this.whiteTailedDeers = whiteTailedDeers;
    }

    public Integer getRoeDeers() {
        return roeDeers;
    }

    public void setRoeDeers(final Integer roeDeers) {
        this.roeDeers = roeDeers;
    }

    public Integer getWildForestReindeers() {
        return wildForestReindeers;
    }

    public void setWildForestReindeers(final Integer wildForestReindeers) {
        this.wildForestReindeers = wildForestReindeers;
    }

    public Integer getFallowDeers() {
        return fallowDeers;
    }

    public void setFallowDeers(final Integer fallowDeers) {
        this.fallowDeers = fallowDeers;
    }

    public Integer getWildBoars() {
        return wildBoars;
    }

    public void setWildBoars(final Integer wildBoars) {
        this.wildBoars = wildBoars;
    }

    public Integer getLynxes() {
        return lynxes;
    }

    public void setLynxes(final Integer lynxes) {
        this.lynxes = lynxes;
    }

    public Integer getBears() {
        return bears;
    }

    public void setBears(final Integer bears) {
        this.bears = bears;
    }

    public Integer getWolves() {
        return wolves;
    }

    public void setWolves(final Integer wolves) {
        this.wolves = wolves;
    }

    public Integer getWolverines() {
        return wolverines;
    }

    public void setWolverines(final Integer wolverines) {
        this.wolverines = wolverines;
    }
}
