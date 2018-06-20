package fi.riista.feature.gamediary.summary;

import fi.riista.feature.organization.OrganisationType;
import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AdminGameDiarySummaryRequestDTO {
    private Integer speciesCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate beginDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NotNull
    private OrganisationType organisationType;

    @NotNull
    @Pattern(regexp = "\\d+")
    private String officialCode;

    private boolean harvestReportOnly;

    public Integer getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(final Integer speciesCode) {
        this.speciesCode = speciesCode;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public OrganisationType getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(final OrganisationType organisationType) {
        this.organisationType = organisationType;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public boolean isHarvestReportOnly() {
        return harvestReportOnly;
    }

    public void setHarvestReportOnly(final boolean harvestReportOnly) {
        this.harvestReportOnly = harvestReportOnly;
    }
}
