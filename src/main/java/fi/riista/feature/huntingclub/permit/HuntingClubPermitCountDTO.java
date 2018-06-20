package fi.riista.feature.huntingclub.permit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuntingClubPermitCountDTO implements HasHarvestCountsForPermit {

    private final long huntingClubId;

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

    private final int numberOfNonEdibleAdultMales;
    private final int numberOfNonEdibleAdultFemales;
    private final int numberOfNonEdibleYoungMales;
    private final int numberOfNonEdibleYoungFemales;

    public HuntingClubPermitCountDTO(final long huntingClubId, final HasHarvestCountsForPermit harvestCounts) {
        this(huntingClubId, harvestCounts.getNumberOfAdultMales(), harvestCounts.getNumberOfAdultFemales(),
                harvestCounts.getNumberOfYoungMales(), harvestCounts.getNumberOfYoungFemales(),
                harvestCounts.getNumberOfNonEdibleAdults(), harvestCounts.getNumberOfNonEdibleYoungs(),
                0, 0, 0, 0);
    }

    public HuntingClubPermitCountDTO(
            final long huntingClubId,
            final int numberOfAdultMales,
            final int numberOfAdultFemales,
            final int numberOfYoungMales,
            final int numberOfYoungFemales,
            final int numberOfNonEdibleAdults,
            final int numberOfNonEdibleYoung,
            final int numberOfNonEdibleAdultMales,
            final int numberOfNonEdibleAdultFemales,
            final int numberOfNonEdibleYoungMales,
            final int numberOfNonEdibleYoungFemales) {

        this.huntingClubId = huntingClubId;
        this.numberOfAdultMales = numberOfAdultMales;
        this.numberOfAdultFemales = numberOfAdultFemales;
        this.numberOfYoungMales = numberOfYoungMales;
        this.numberOfYoungFemales = numberOfYoungFemales;
        this.numberOfNonEdibleAdults = numberOfNonEdibleAdults;
        this.numberOfNonEdibleYoungs = numberOfNonEdibleYoung;
        this.numberOfNonEdibleAdultMales = numberOfNonEdibleAdultMales;
        this.numberOfNonEdibleAdultFemales = numberOfNonEdibleAdultFemales;
        this.numberOfNonEdibleYoungMales = numberOfNonEdibleYoungMales;
        this.numberOfNonEdibleYoungFemales = numberOfNonEdibleYoungFemales;
    }

    public int countAdults() {
        return numberOfAdultMales + numberOfAdultFemales;
    }

    public int countYoung() {
        return numberOfYoungMales + numberOfYoungFemales;
    }

    // Accessors -->

    public long getHuntingClubId() {
        return huntingClubId;
    }

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

    public int getNumberOfNonEdibleAdultMales() {
        return numberOfNonEdibleAdultMales;
    }

    public int getNumberOfNonEdibleAdultFemales() {
        return numberOfNonEdibleAdultFemales;
    }

    public int getNumberOfNonEdibleYoungMales() {
        return numberOfNonEdibleYoungMales;
    }

    public int getNumberOfNonEdibleYoungFemales() {
        return numberOfNonEdibleYoungFemales;
    }
}
