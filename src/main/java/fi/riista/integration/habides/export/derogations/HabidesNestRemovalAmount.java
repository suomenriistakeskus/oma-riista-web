package fi.riista.integration.habides.export.derogations;

public class HabidesNestRemovalAmount {

    private Integer nestAmount;
    private Integer eggAmount;
    private Integer constructionAmount;

    public HabidesNestRemovalAmount(final Integer nestAmount,
                                    final Integer eggAmount,
                                    final Integer constructionAmount) {
        this.nestAmount = nestAmount;
        this.eggAmount = eggAmount;
        this.constructionAmount = constructionAmount;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }
}
