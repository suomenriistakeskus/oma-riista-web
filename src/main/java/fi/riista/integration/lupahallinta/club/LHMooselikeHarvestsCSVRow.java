package fi.riista.integration.lupahallinta.club;

import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.util.F;

public class LHMooselikeHarvestsCSVRow {
    private final String permitNumber;
    private final String customerNumber;
    private final int speciesCode;

    private final int adultMales;
    private final int adultFemales;
    private final int youngMales;
    private final int youngFemales;
    private final int adultsNonEdible;
    private final int youngNonEdible;

    private final Integer totalHuntingArea;
    private final Integer effectiveHuntingArea;
    private final Integer remainingPopulationInTotalArea;
    private final Integer remainingPopulationInEffectiveArea;

    public LHMooselikeHarvestsCSVRow(final String permitNumber,
                                     final String customerNumber,
                                     final int speciesCode,
                                     final HasHarvestCountsForPermit harvestCount,
                                     final ClubHuntingSummaryBasicInfoDTO huntingSummary,
                                     final int permitAreaSize) {
        final int totalAreaSize = F.coalesceAsInt(huntingSummary.getTotalHuntingArea(), permitAreaSize);
        final int effectiveAreaSize = F.coalesceAsInt(huntingSummary.getEffectiveHuntingArea(), totalAreaSize);

        this.permitNumber = permitNumber;
        this.customerNumber = customerNumber;
        this.speciesCode = speciesCode;
        this.adultMales = harvestCount.getNumberOfAdultMales();
        this.adultFemales = harvestCount.getNumberOfAdultFemales();
        this.youngMales = harvestCount.getNumberOfYoungMales();
        this.youngFemales = harvestCount.getNumberOfYoungFemales();
        this.adultsNonEdible = harvestCount.getNumberOfNonEdibleAdults();
        this.youngNonEdible = harvestCount.getNumberOfNonEdibleYoungs();
        this.totalHuntingArea = totalAreaSize;
        this.effectiveHuntingArea = Math.min(effectiveAreaSize, totalAreaSize);
        this.remainingPopulationInTotalArea = F.coalesceAsInt(huntingSummary.getRemainingPopulationInTotalArea(), 0);
        this.remainingPopulationInEffectiveArea = F.coalesceAsInt(huntingSummary.getRemainingPopulationInEffectiveArea(), 0);
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public int getSpeciesCode() {
        return speciesCode;
    }

    public int getAdultMales() {
        return adultMales;
    }

    public int getAdultFemales() {
        return adultFemales;
    }

    public int getYoungMales() {
        return youngMales;
    }

    public int getYoungFemales() {
        return youngFemales;
    }

    public int getAdultsNonEdible() {
        return adultsNonEdible;
    }

    public int getYoungNonEdible() {
        return youngNonEdible;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }
}
