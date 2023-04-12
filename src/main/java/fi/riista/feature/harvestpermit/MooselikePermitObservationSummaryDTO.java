package fi.riista.feature.harvestpermit;

public class MooselikePermitObservationSummaryDTO {

    private int adultMale;

    private int adultFemaleNoCalfs;

    private int adultFemaleOneCalf;
    private int adultFemaleTwoCalfs;
    private int adultFemaleThreeCalfs;
    private int adultFemaleFourCalfs;
    private int solitaryCalf;
    private int unknown;


    public int getAdultMale() {
        return adultMale;
    }

    public void setAdultMale(final int adultMale) {
        this.adultMale = adultMale;
    }

    public MooselikePermitObservationSummaryDTO addAdultMale(final int adultMale) {
        this.adultMale += adultMale;
        return this;
    }

    public int getAdultFemaleNoCalfs() {
        return adultFemaleNoCalfs;
    }

    public void setAdultFemaleNoCalfs(final int adultFemaleNoCalfs) {
        this.adultFemaleNoCalfs = adultFemaleNoCalfs;
    }

    public MooselikePermitObservationSummaryDTO addAdultFemaleNoCalfs(final int adultFemaleNoCalfs) {
        this.adultFemaleNoCalfs += adultFemaleNoCalfs;
        return this;
    }

    public int getAdultFemaleOneCalf() {
        return adultFemaleOneCalf;
    }

    public void setAdultFemaleOneCalf(final int adultFemaleOneCalf) {
        this.adultFemaleOneCalf = adultFemaleOneCalf;
    }

    public MooselikePermitObservationSummaryDTO addAdultFemaleOneCalf(final int adultFemaleOneCalf) {
        this.adultFemaleOneCalf += adultFemaleOneCalf;
        return this;
    }

    public int getAdultFemaleTwoCalfs() {
        return adultFemaleTwoCalfs;
    }

    public void setAdultFemaleTwoCalfs(final int adultFemaleTwoCalfs) {
        this.adultFemaleTwoCalfs = adultFemaleTwoCalfs;
    }

    public MooselikePermitObservationSummaryDTO addAdultFemaleTwoCalfs(final int adultFemaleTwoCalfs) {
        this.adultFemaleTwoCalfs += adultFemaleTwoCalfs;
        return this;
    }

    public int getAdultFemaleThreeCalfs() {
        return adultFemaleThreeCalfs;
    }

    public void setAdultFemaleThreeCalfs(final int adultFemaleThreeCalfs) {
        this.adultFemaleThreeCalfs = adultFemaleThreeCalfs;
    }

    public MooselikePermitObservationSummaryDTO addAdultFemaleThreeCalfs(final int adultFemaleThreeCalfs) {
        this.adultFemaleThreeCalfs += adultFemaleThreeCalfs;
        return this;
    }

    public int getAdultFemaleFourCalfs() {
        return adultFemaleFourCalfs;
    }

    public void setAdultFemaleFourCalfs(final int adultFemaleFourCalfs) {
        this.adultFemaleFourCalfs = adultFemaleFourCalfs;
    }

    public MooselikePermitObservationSummaryDTO addAdultFemaleFourCalfs(final int adultFemaleFourCalfs) {
        this.adultFemaleFourCalfs += adultFemaleFourCalfs;
        return this;
    }

    public int getSolitaryCalf() {
        return solitaryCalf;
    }

    public void setSolitaryCalf(final int solitaryCalf) {
        this.solitaryCalf = solitaryCalf;
    }

    public MooselikePermitObservationSummaryDTO addSolitaryCalf(final int solitaryCalf) {
        this.solitaryCalf += solitaryCalf;
        return this;
    }

    public int getUnknown() {
        return unknown;
    }

    public void setUnknown(final int unknown) {
        this.unknown = unknown;
    }

    public MooselikePermitObservationSummaryDTO addUnknown(final int unknown) {
        this.unknown += unknown;
        return this;
    }
}
