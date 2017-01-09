package fi.riista.integration.lupahallinta.club;

public class LHMooselikeHarvestsCSVRow {
    private String permitNumber;
    private String customerNumber;
    private int speciesCode;

    private int adultMales;
    private int adultFemales;
    private int youngMales;
    private int youngFemales;
    private int adultsNonEdible;
    private int youngNonEdible;

    private Integer totalHuntingArea;
    private Integer effectiveHuntingArea;
    private Integer remainingPopulationInTotalArea;
    private Integer remainingPopulationInEffectiveArea;


    public LHMooselikeHarvestsCSVRow(final String permitNumber,
                                     final String customerNumber,
                                     final int speciesCode,
                                     final int adultMales,
                                     final int adultFemales,
                                     final int youngMales,
                                     final int youngFemales,
                                     final int adultsNonEdible,
                                     final int youngNonEdible,
                                     final Integer totalHuntingArea,
                                     final Integer effectiveHuntingArea,
                                     final Integer remainingPopulationInTotalArea,
                                     final Integer remainingPopulationInEffectiveArea) {

        this.permitNumber = permitNumber;
        this.customerNumber = customerNumber;
        this.speciesCode = speciesCode;
        this.adultMales = adultMales;
        this.adultFemales = adultFemales;
        this.youngMales = youngMales;
        this.youngFemales = youngFemales;
        this.adultsNonEdible = adultsNonEdible;
        this.youngNonEdible = youngNonEdible;
        this.totalHuntingArea = totalHuntingArea;
        this.effectiveHuntingArea = effectiveHuntingArea;
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public int getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(int speciesCode) {
        this.speciesCode = speciesCode;
    }

    public int getAdultMales() {
        return adultMales;
    }

    public void setAdultMales(int adultMales) {
        this.adultMales = adultMales;
    }

    public int getAdultFemales() {
        return adultFemales;
    }

    public void setAdultFemales(int adultFemales) {
        this.adultFemales = adultFemales;
    }

    public int getYoungMales() {
        return youngMales;
    }

    public void setYoungMales(int youngMales) {
        this.youngMales = youngMales;
    }

    public int getYoungFemales() {
        return youngFemales;
    }

    public void setYoungFemales(int youngFemales) {
        this.youngFemales = youngFemales;
    }

    public int getAdultsNonEdible() {
        return adultsNonEdible;
    }

    public void setAdultsNonEdible(int adultsNonEdible) {
        this.adultsNonEdible = adultsNonEdible;
    }

    public int getYoungNonEdible() {
        return youngNonEdible;
    }

    public void setYoungNonEdible(int youngNonEdible) {
        this.youngNonEdible = youngNonEdible;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public void setTotalHuntingArea(Integer totalHuntingArea) {
        this.totalHuntingArea = totalHuntingArea;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public void setEffectiveHuntingArea(Integer effectiveHuntingArea) {
        this.effectiveHuntingArea = effectiveHuntingArea;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public void setRemainingPopulationInTotalArea(Integer remainingPopulationInTotalArea) {
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public void setRemainingPopulationInEffectiveArea(Integer remainingPopulationInEffectiveArea) {
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }
}
