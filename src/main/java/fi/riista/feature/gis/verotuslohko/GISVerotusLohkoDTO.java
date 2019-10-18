package fi.riista.feature.gis.verotuslohko;

public class GISVerotusLohkoDTO {
    private String officialCode;
    private String name;

    public GISVerotusLohkoDTO(final String officialCode, final String name) {
        this.officialCode = officialCode;
        this.name = name;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public String getName() {
        return name;
    }
}
