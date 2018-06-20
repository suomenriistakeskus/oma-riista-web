package fi.riista.feature.permit.application.metsahallitus;

public class MetsahallitusAreaPermitNumbersDTO {
    private int mhApplicationNumber;
    private int mhPermitNumber;

    public int getMhApplicationNumber() {
        return mhApplicationNumber;
    }

    public void setMhApplicationNumber(final int mhApplicationNumber) {
        this.mhApplicationNumber = mhApplicationNumber;
    }

    public int getMhPermitNumber() {
        return mhPermitNumber;
    }

    public void setMhPermitNumber(final int mhPermitNumber) {
        this.mhPermitNumber = mhPermitNumber;
    }
}
