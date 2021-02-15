package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonGetter;
import fi.riista.feature.organization.rhy.annualstats.export.AnnualStatisticGroup;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
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
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.BigDecimalComparison.nullsafeEq;
import static fi.riista.util.F.nullsafeMax;
import static fi.riista.util.NumberUtils.nullableSum;
import static fi.riista.util.NumberUtils.nullableIntSum;
import static java.util.Objects.requireNonNull;

@Embeddable
@Access(AccessType.FIELD)
public class GameDamageStatistics
        implements AnnualStatisticsFieldsetReadiness,
        AnnualStatisticsManuallyEditableFields<GameDamageStatistics>,
        Serializable {

    public static final GameDamageStatistics reduce(@Nullable final GameDamageStatistics a,
                                                    @Nullable final GameDamageStatistics b) {

        final GameDamageStatistics result = new GameDamageStatistics();
        result.setMooselikeDamageInspectionLocations(nullableIntSum(a, b, s -> s.getMooselikeDamageInspectionLocations()));
        result.setMooselikeDamageInspectionExpenses(nullableSum(a, b, s -> s.getMooselikeDamageInspectionExpenses()));
        result.setLargeCarnivoreDamageInspectionLocations(nullableIntSum(a, b, s -> s.getLargeCarnivoreDamageInspectionLocations()));
        result.setLargeCarnivoreDamageInspectionExpenses(nullableSum(a, b, s -> s.getLargeCarnivoreDamageInspectionExpenses()));
        result.setGameDamageInspectors(nullableIntSum(a, b, s -> s.getGameDamageInspectors()));
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

    @Column(name = "mooselike_damage_inspection_locations_overridden", nullable = false)
    private boolean mooselikeDamageInspectionLocationsOverridden;

    // Hirvieläimet, tarkastuksista aiheutuneet kustannukset euroina
    @Min(0)
    @Column(name = "mooselike_damage_inspection_expenses")
    private BigDecimal mooselikeDamageInspectionExpenses;

    // Suurpedot, maastotarkastuskohteiden määrä
    @Min(0)
    @Column(name = "large_carnivore_damage_inspection_locations")
    private Integer largeCarnivoreDamageInspectionLocations;

    @Column(name = "large_carnivore_damage_inspection_locations_overridden", nullable = false)
    private boolean largeCarnivoreDamageInspectionLocationsOverridden;

    // Suurpedot, tarkastuksista aiheutuneet kustannukset euroina
    @Min(0)
    @Column(name = "large_carnivore_damage_inspection_expenses")
    private BigDecimal largeCarnivoreDamageInspectionExpenses;

    // Nimitetyt katselmoijat, kpl
    @Min(0)
    @Column(name = "game_damage_inspectors")
    private Integer gameDamageInspectors;

    // Updated when any of the manually updateable fields is changed.
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "game_damage_last_modified")
    private DateTime lastModified;

    public GameDamageStatistics() {
    }

    public GameDamageStatistics(@Nonnull final GameDamageStatistics that) {
        requireNonNull(that);

        this.mooselikeDamageInspectionLocations = that.mooselikeDamageInspectionLocations;
        this.mooselikeDamageInspectionLocationsOverridden = that.mooselikeDamageInspectionLocationsOverridden;
        this.mooselikeDamageInspectionExpenses = that.mooselikeDamageInspectionExpenses;
        this.largeCarnivoreDamageInspectionLocations = that.largeCarnivoreDamageInspectionLocations;
        this.largeCarnivoreDamageInspectionLocationsOverridden = that.largeCarnivoreDamageInspectionLocationsOverridden;
        this.largeCarnivoreDamageInspectionExpenses = that.largeCarnivoreDamageInspectionExpenses;
        this.gameDamageInspectors = that.gameDamageInspectors;
        this.lastModified = that.lastModified;
    }

    @Override
    public AnnualStatisticGroup getGroup() {
        return AnnualStatisticGroup.GAME_DAMAGE;
    }

    @Override
    public boolean isEqualTo(@Nonnull final GameDamageStatistics that) {
        // Includes manually updateable fields only.

        return Objects.equals(mooselikeDamageInspectionLocations, that.mooselikeDamageInspectionLocations) &&
                nullsafeEq(mooselikeDamageInspectionExpenses, that.mooselikeDamageInspectionExpenses) &&
                Objects.equals(largeCarnivoreDamageInspectionLocations, that.largeCarnivoreDamageInspectionLocations) &&
                nullsafeEq(largeCarnivoreDamageInspectionExpenses, that.largeCarnivoreDamageInspectionExpenses);
    }

    @Override
    public void assignFrom(@Nonnull final GameDamageStatistics that) {
        // Includes manually updateable fields only.

        if (!Objects.equals(this.mooselikeDamageInspectionLocations, that.mooselikeDamageInspectionLocations)) {
            this.mooselikeDamageInspectionLocationsOverridden = true;
        }
        this.mooselikeDamageInspectionLocations = that.mooselikeDamageInspectionLocations;
        this.mooselikeDamageInspectionExpenses = that.mooselikeDamageInspectionExpenses;

        if (!Objects.equals(this.largeCarnivoreDamageInspectionLocations, that.largeCarnivoreDamageInspectionLocations)) {
            this.largeCarnivoreDamageInspectionLocationsOverridden = true;
        }
        this.largeCarnivoreDamageInspectionLocations = that.largeCarnivoreDamageInspectionLocations;
        this.largeCarnivoreDamageInspectionExpenses = that.largeCarnivoreDamageInspectionExpenses;
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

    public boolean isMooselikeDamageInspectionLocationsOverridden() {
        return mooselikeDamageInspectionLocationsOverridden;
    }

    public void setMooselikeDamageInspectionLocationsOverridden(final boolean mooselikeDamageInspectionLocationsOverridden) {
        this.mooselikeDamageInspectionLocationsOverridden = mooselikeDamageInspectionLocationsOverridden;
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

    public boolean isLargeCarnivoreDamageInspectionLocationsOverridden() {
        return largeCarnivoreDamageInspectionLocationsOverridden;
    }

    public void setLargeCarnivoreDamageInspectionLocationsOverridden(final boolean largeCarnivoreDamageInspectionLocationsOverridden) {
        this.largeCarnivoreDamageInspectionLocationsOverridden = largeCarnivoreDamageInspectionLocationsOverridden;
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

    @Override
    public DateTime getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(final DateTime lastModified) {
        this.lastModified = lastModified;
    }
}
