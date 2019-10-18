package fi.riista.feature.huntingclub.permit.endofhunting.basicsummary;

import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class ModeratedHarvestCounts implements HasHarvestCountsForPermit, Serializable {

    @Min(0)
    @Column(nullable = false)
    private int numberOfAdultMales;

    @Min(0)
    @Column(nullable = false)
    private int numberOfAdultFemales;

    @Min(0)
    @Column(nullable = false)
    private int numberOfYoungMales;

    @Min(0)
    @Column(nullable = false)
    private int numberOfYoungFemales;

    @Min(0)
    @Column(nullable = false)
    private int numberOfNonEdibleAdults;

    @Min(0)
    @Column(nullable = false)
    private int numberOfNonEdibleYoungs;

    public ModeratedHarvestCounts() {
    }

    public ModeratedHarvestCounts(final HasHarvestCountsForPermit harvestCounts) {
        this.numberOfAdultMales = harvestCounts.getNumberOfAdultMales();
        this.numberOfAdultFemales = harvestCounts.getNumberOfAdultFemales();
        this.numberOfYoungMales = harvestCounts.getNumberOfYoungMales();
        this.numberOfYoungFemales = harvestCounts.getNumberOfYoungFemales();
        this.numberOfNonEdibleAdults = harvestCounts.getNumberOfNonEdibleAdults();
        this.numberOfNonEdibleYoungs = harvestCounts.getNumberOfNonEdibleYoungs();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModeratedHarvestCounts)) return false;
        final ModeratedHarvestCounts that = (ModeratedHarvestCounts) o;
        return numberOfAdultMales == that.numberOfAdultMales &&
                numberOfAdultFemales == that.numberOfAdultFemales &&
                numberOfYoungMales == that.numberOfYoungMales &&
                numberOfYoungFemales == that.numberOfYoungFemales &&
                numberOfNonEdibleAdults == that.numberOfNonEdibleAdults &&
                numberOfNonEdibleYoungs == that.numberOfNonEdibleYoungs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numberOfAdultMales, numberOfAdultFemales, numberOfYoungMales, numberOfYoungFemales, numberOfNonEdibleAdults, numberOfNonEdibleYoungs);
    }

    @Override
    public int getNumberOfAdultMales() {
        return numberOfAdultMales;
    }

    public void setNumberOfAdultMales(final int numberOfAdultMales) {
        this.numberOfAdultMales = numberOfAdultMales;
    }

    @Override
    public int getNumberOfAdultFemales() {
        return numberOfAdultFemales;
    }

    public void setNumberOfAdultFemales(final int numberOfAdultFemales) {
        this.numberOfAdultFemales = numberOfAdultFemales;
    }

    @Override
    public int getNumberOfYoungMales() {
        return numberOfYoungMales;
    }

    public void setNumberOfYoungMales(final int numberOfYoungMales) {
        this.numberOfYoungMales = numberOfYoungMales;
    }

    @Override
    public int getNumberOfYoungFemales() {
        return numberOfYoungFemales;
    }

    public void setNumberOfYoungFemales(final int numberOfYoungFemales) {
        this.numberOfYoungFemales = numberOfYoungFemales;
    }

    @Override
    public int getNumberOfNonEdibleAdults() {
        return numberOfNonEdibleAdults;
    }

    public void setNumberOfNonEdibleAdults(final int numberOfNonEdibleAdults) {
        this.numberOfNonEdibleAdults = numberOfNonEdibleAdults;
    }

    @Override
    public int getNumberOfNonEdibleYoungs() {
        return numberOfNonEdibleYoungs;
    }

    public void setNumberOfNonEdibleYoungs(final int numberOfNonEdibleYoungs) {
        this.numberOfNonEdibleYoungs = numberOfNonEdibleYoungs;
    }
}
