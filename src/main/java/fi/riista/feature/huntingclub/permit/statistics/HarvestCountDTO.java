package fi.riista.feature.huntingclub.permit.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsDTO;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.Collection;

import static fi.riista.util.NumberUtils.percentRatio;
import static fi.riista.util.NumberUtils.sum;

public class HarvestCountDTO implements HasHarvestCountsForPermit {

    public static final HarvestCountDTO ZEROS = new HarvestCountDTO(0, 0, 0, 0, 0, 0, 0, 0);

    @Nonnull
    public static HarvestCountDTO createStatisticTotal(final Collection<MoosePermitStatisticsDTO> stats) {
        return createTotal(F.mapNonNullsToList(stats, MoosePermitStatisticsDTO::getHarvestCount));
    }

    public static HarvestCountDTO createTotal(final @Nonnull Collection<HarvestCountDTO> harvestCounts) {
        return new HarvestCountDTO(
                sum(harvestCounts, HarvestCountDTO::getNumberOfAdultMales),
                sum(harvestCounts, HarvestCountDTO::getNumberOfAdultFemales),
                sum(harvestCounts, HarvestCountDTO::getNumberOfYoungMales),
                sum(harvestCounts, HarvestCountDTO::getNumberOfYoungFemales),
                sum(harvestCounts, HarvestCountDTO::getNumberOfNonEdibleAdults),
                sum(harvestCounts, HarvestCountDTO::getNumberOfNonEdibleYoungs),
                sum(harvestCounts, HarvestCountDTO::getNumberOfNonEdibleAdultMales));
    }

    @JsonProperty(value = "adultMales")
    private final int numberOfAdultMales;

    @JsonProperty(value = "adultFemales")
    private final int numberOfAdultFemales;

    @JsonProperty(value = "youngMales")
    private final int numberOfYoungMales;

    @JsonProperty(value = "youngFemales")
    private final int numberOfYoungFemales;

    @JsonProperty(value = "adultsNotEdible")
    private final int numberOfNonEdibleAdults;

    @JsonProperty(value = "youngsNotEdible")
    private final int numberOfNonEdibleYoungs;

    @JsonIgnore
    private final int numberOfNonEdibleAdultMales;

    public HarvestCountDTO(final HasHarvestCountsForPermit harvestCounts) {
        this.numberOfAdultMales = harvestCounts.getNumberOfAdultMales();
        this.numberOfAdultFemales = harvestCounts.getNumberOfAdultFemales();
        this.numberOfYoungMales = harvestCounts.getNumberOfYoungMales();
        this.numberOfYoungFemales = harvestCounts.getNumberOfYoungFemales();
        this.numberOfNonEdibleAdults = harvestCounts.getNumberOfNonEdibleAdults();
        this.numberOfNonEdibleYoungs = harvestCounts.getNumberOfNonEdibleYoungs();

        // XXX: Permit restriction calculation is not done correctly
        this.numberOfNonEdibleAdultMales = harvestCounts.getNumberOfNonEdibleAdults();
    }

    public HarvestCountDTO(final int numberOfAdultMales,
                           final int numberOfAdultFemales,
                           final int numberOfYoungMales,
                           final int numberOfYoungFemales,
                           final int numberOfNonEdibleAdults,
                           final int numberOfNonEdibleYoungs,
                           final int numberOfNonEdibleAdultMales) {
        this.numberOfAdultMales = numberOfAdultMales;
        this.numberOfAdultFemales = numberOfAdultFemales;
        this.numberOfYoungMales = numberOfYoungMales;
        this.numberOfYoungFemales = numberOfYoungFemales;
        this.numberOfNonEdibleAdults = numberOfNonEdibleAdults;
        this.numberOfNonEdibleYoungs = numberOfNonEdibleYoungs;
        this.numberOfNonEdibleAdultMales = numberOfNonEdibleAdultMales;
    }

    public HarvestCountDTO(
            final int numberOfAdultMales,
            final int numberOfAdultFemales,
            final int numberOfYoungMales,
            final int numberOfYoungFemales,
            final int numberOfNonEdibleAdultMales,
            final int numberOfNonEdibleAdultFemales,
            final int numberOfNonEdibleYoungMales,
            final int numberOfNonEdibleYoungFemales) {
        this.numberOfAdultMales = numberOfAdultMales;
        this.numberOfAdultFemales = numberOfAdultFemales;
        this.numberOfYoungMales = numberOfYoungMales;
        this.numberOfYoungFemales = numberOfYoungFemales;
        this.numberOfNonEdibleAdults = numberOfNonEdibleAdultMales + numberOfNonEdibleAdultFemales;
        this.numberOfNonEdibleYoungs = numberOfNonEdibleYoungMales + numberOfNonEdibleYoungFemales;
        this.numberOfNonEdibleAdultMales = numberOfNonEdibleAdultMales;
    }

    @JsonGetter("adults")
    public int getNumberOfAdults() {
        return numberOfAdultMales + numberOfAdultFemales;
    }

    @JsonGetter("young")
    public int getNumberOfYoung() {
        return numberOfYoungMales + numberOfYoungFemales;
    }

    @JsonGetter("total")
    public int getTotal() {
        return getNumberOfAdults() + getNumberOfYoung();
    }

    @JsonGetter
    public double getRequiredPermitAmount() {
        return getNumberOfAdults() + getNumberOfYoung() / 2.0;
    }

    @JsonGetter
    public double getRequiredAmendmentPermits() {
        return getNumberOfNonEdibleAdults() + getNumberOfNonEdibleYoungs() / 2.0;
    }

    @JsonGetter
    public double getYoungPercentage() {
        return percentRatio(
                getNumberOfYoung(),
                getTotal());
    }

    @JsonGetter
    public double getYoungMalePercentage() {
        return percentRatio(
                getNumberOfYoungMales(),
                getNumberOfYoung());
    }

    @JsonGetter
    public double getAdultMalePercentage() {
        return percentRatio(
                getNumberOfAdultMales(),
                getNumberOfAdults());
    }

    // Accessors -->

    @Override
    public int getNumberOfAdultMales() {
        return numberOfAdultMales;
    }

    @Override
    public int getNumberOfAdultFemales() {
        return numberOfAdultFemales;
    }

    @Override
    public int getNumberOfYoungMales() {
        return numberOfYoungMales;
    }

    @Override
    public int getNumberOfYoungFemales() {
        return numberOfYoungFemales;
    }

    @Override
    public int getNumberOfNonEdibleAdults() {
        return numberOfNonEdibleAdults;
    }

    @Override
    public int getNumberOfNonEdibleYoungs() {
        return numberOfNonEdibleYoungs;
    }

    @JsonIgnore
    public int getNumberOfNonEdibleAdultMales() {
        return numberOfNonEdibleAdultMales;
    }
}
