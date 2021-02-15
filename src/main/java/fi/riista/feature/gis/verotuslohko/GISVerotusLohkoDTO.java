package fi.riista.feature.gis.verotuslohko;

public class GISVerotusLohkoDTO {
    private String officialCode;
    private int huntingYear;
    private String name;

    public GISVerotusLohkoDTO(final String officialCode, final int huntingYear, final String name) {
        this.officialCode = officialCode;
        this.huntingYear = huntingYear;
        this.name = name;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public String getName() {
        return name;
    }
}
