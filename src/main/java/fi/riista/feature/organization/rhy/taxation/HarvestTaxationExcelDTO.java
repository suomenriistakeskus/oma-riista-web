package fi.riista.feature.organization.rhy.taxation;

public class HarvestTaxationExcelDTO {

    private int huntingYear;

    private int gameSpeciesCode;

    private Long rhyId;

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final int gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final Long rhyId) {
        this.rhyId = rhyId;
    }
}
