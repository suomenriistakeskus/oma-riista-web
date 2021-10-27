package fi.riista.integration.habides.export.derogations;

public class HabidesPermitUsage {
    private final Integer specimenAmount;
    private final Integer eggAmount;

    public HabidesPermitUsage(final Integer specimenAmount, final Integer eggAmount) {
        this.specimenAmount = specimenAmount;
        this.eggAmount = eggAmount;
    }

    public Integer getSpecimenAmount() {
        return specimenAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }
}
