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
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.BigDecimalComparison.nullsafeEq;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullsafeSum;
import static fi.riista.util.NumberUtils.nullsafeSumAsInt;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class GameDamageStatistics
        implements AnnualStatisticsFieldsetStatus, HasLastModificationStatus<GameDamageStatistics>, Serializable {

    public static final GameDamageStatistics reduce(@Nullable final GameDamageStatistics a,
                                                    @Nullable final GameDamageStatistics b) {

        final GameDamageStatistics result = new GameDamageStatistics();
        result.setMooselikeDamageInspectionLocations(nullsafeSumAsInt(a, b, s -> s.getMooselikeDamageInspectionLocations()));
        result.setMooselikeDamageInspectionExpenses(nullsafeSum(a, b, s -> s.getMooselikeDamageInspectionExpenses()));
        result.setLargeCarnivoreDamageInspectionLocations(nullsafeSumAsInt(a, b, s -> s.getLargeCarnivoreDamageInspectionLocations()));
        result.setLargeCarnivoreDamageInspectionExpenses(nullsafeSum(a, b, s -> s.getLargeCarnivoreDamageInspectionExpenses()));
        result.setGameDamageInspectors(nullsafeSumAsInt(a, b, s -> s.getGameDamageInspectors()));
        result.setLastModified(nullsafeMax(a, b, s -> s.getLastModified()));
        return result;
    }

    public static GameDamageStatistics reduce(@Nonnull final Stream<GameDamageStatistics> items) {
        requireNonNull(items);
        return items.reduce(new GameDamageStatistics(), GameDamageStatistics::reduce);
    }

    public static <T> GameDamageStatistics reduce(@Nonnull final Iterable<? extends T> items,
                                                  @Nonnull final Function<? super T, GameDamageStatistics> extractor) {

        requireNonNull(extractor, "extractor is null");
        return reduce(F.stream(items).map(extractor));
    }

    // Hirvieläimet, maastotarkastuskohteiden määrä
    @Min(0)
    @Column(name = "mooselike_damage_inspection_locations")
    private Integer mooselikeDamageInspectionLocations;

    // Hirvieläimet, tarkastuksista aiheutuneet kustannukset euroina
    @Min(0)
    @Column(name = "mooselike_damage_inspection_expenses")
    private BigDecimal mooselikeDamageInspectionExpenses;

    // Suurpedot, maastotarkastuskohteiden määrä
    @Min(0)
    @Column(name = "large_carnivore_damage_inspection_locations")
    private Integer largeCarnivoreDamageInspectionLocations;

    // Suurpedot, tarkastuksista aiheutuneet kustannukset euroina
    @Min(0)
    @Column(name = "large_carnivore_damage_inspection_expenses")
    private BigDecimal largeCarnivoreDamageInspectionExpenses;

    // Nimitetyt katselmoijat, kpl
    @Min(0)
    @Column(name = "game_damage_inspectors")
    private Integer gameDamageInspectors;

    // Updated when any of the manually updateable fields is changed.
    @Column(name = "game_damage_last_modified")
    private DateTime lastModified;

    public GameDamageStatistics() {
    }

    public GameDamageStatistics(@Nonnull final GameDamageStatistics that) {
        requireNonNull(that);

        this.mooselikeDamageInspectionLocations = that.mooselikeDamageInspectionLocations;
        this.mooselikeDamageInspectionExpenses = that.mooselikeDamageInspectionExpenses;
        this.largeCarnivoreDamageInspectionLocations = that.largeCarnivoreDamageInspectionLocations;
        this.largeCarnivoreDamageInspectionExpenses = that.largeCarnivoreDamageInspectionExpenses;
        this.gameDamageInspectors = that.gameDamageInspectors;
        this.lastModified = that.lastModified;
    }

    @Override
    public boolean isEqualTo(final GameDamageStatistics other) {
        // Includes manually updateable fields only.

        return Objects.equals(mooselikeDamageInspectionLocations, other.mooselikeDamageInspectionLocations) &&
                nullsafeEq(mooselikeDamageInspectionExpenses, other.mooselikeDamageInspectionExpenses) &&
                Objects.equals(largeCarnivoreDamageInspectionLocations, other.largeCarnivoreDamageInspectionLocations) &&
                nullsafeEq(largeCarnivoreDamageInspectionExpenses, other.largeCarnivoreDamageInspectionExpenses);
    }

    @Override
    public void updateModificationStatus() {
        lastModified = DateUtil.now();
    }

    @Override
    public boolean isReadyForInspection() {
        return F.allNotNull(
                mooselikeDamageInspectionLocations, largeCarnivoreDamageInspectionLocations, gameDamageInspectors);
    }

    @Override
    public boolean isCompleteForApproval() {
        return isReadyForInspection();
    }

    @JsonGetter(value = "totalDamageInspectionLocations")
    public int getTotalDamageInspectionLocations() {
        return Stream
                .of(mooselikeDamageInspectionLocations, largeCarnivoreDamageInspectionLocations)
                .mapToInt(NumberUtils::getIntValueOrZero)
                .sum();
    }

    @JsonGetter(value = "totalDamageInspectionExpenses")
    public BigDecimal getTotalDamageInspectionExpenses() {
        return Stream
                .of(mooselikeDamageInspectionExpenses, largeCarnivoreDamageInspectionExpenses)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Accessors -->

    public Integer getMooselikeDamageInspectionLocations() {
        return mooselikeDamageInspectionLocations;
    }

    public void setMooselikeDamageInspectionLocations(final Integer mooselikeDamageInspectionLocations) {
        this.mooselikeDamageInspectionLocations = mooselikeDamageInspectionLocations;
    }

    public BigDecimal getMooselikeDamageInspectionExpenses() {
        return mooselikeDamageInspectionExpenses;
    }

    public void setMooselikeDamageInspectionExpenses(final BigDecimal mooselikeDamageInspectionExpenses) {
        this.mooselikeDamageInspectionExpenses = mooselikeDamageInspectionExpenses;
    }

    public Integer getLargeCarnivoreDamageInspectionLocations() {
        return largeCarnivoreDamageInspectionLocations;
    }

    public void setLargeCarnivoreDamageInspectionLocations(final Integer largeCarnivoreDamageInspectionLocations) {
        this.largeCarnivoreDamageInspectionLocations = largeCarnivoreDamageInspectionLocations;
    }

    public BigDecimal getLargeCarnivoreDamageInspectionExpenses() {
        return largeCarnivoreDamageInspectionExpenses;
    }

    public void setLargeCarnivoreDamageInspectionExpenses(final BigDecimal largeCarnivoreDamageInspectionExpenses) {
        this.largeCarnivoreDamageInspectionExpenses = largeCarnivoreDamageInspectionExpenses;
    }

    public Integer getGameDamageInspectors() {
        return gameDamageInspectors;
    }

    public void setGameDamageInspectors(final Integer gameDamageInspectors) {
        this.gameDamageInspectors = gameDamageInspectors;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
