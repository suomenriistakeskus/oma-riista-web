package fi.riista.integration.habides.export.derogations;

public class HabidesDerogationEntityDTO {
    private Float individuals;
    private Integer nests;
    private Integer eggs;
    private Integer breeding;

    public HabidesDerogationEntityDTO() {
    }

    public HabidesDerogationEntityDTO(final Float individuals, final Integer nests, final Integer eggs, final Integer breeding) {
        this.individuals = individuals;
        this.nests = nests;
        this.eggs = eggs;
        this.breeding = breeding;
    }

    public Float getIndividuals() {
        return individuals;
    }

    public void setIndividuals(final Float individuals) {
        this.individuals = individuals;
    }

    public Integer getNests() {
        return nests;
    }

    public void setNests(final Integer nests) {
        this.nests = nests;
    }

    public Integer getEggs() {
        return eggs;
    }

    public void setEggs(final Integer eggs) {
        this.eggs = eggs;
    }

    public Integer getBreeding() {
        return breeding;
    }

    public void setBreeding(final Integer breeding) {
        this.breeding = breeding;
    }
}
