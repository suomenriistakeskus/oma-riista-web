package fi.riista.api.moderator;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class HabidesReportRequestDTO {

    @NotNull
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String filename;

    private int speciesCode;

    private int year;

    public String getFilename() { return filename; }

    public void setFilename(final String filename) { this.filename = filename; }

    public int getSpeciesCode() { return speciesCode; }

    public void setSpeciesCode(final int speciesCode) { this.speciesCode = speciesCode; }

    public int getYear() { return year; }

    public void setYear(final int year) { this.year = year; }

    @Override
    public String toString() { return "{ " +
            "filename: " + filename + " ," +
            "speciesCode: " + speciesCode + ", " +
            "year: " + year + " }";
    }
}
