package fi.riista.feature.permit.application.search;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Set;

public class HarvestPermitApplicationSearchDTO {
    public enum StatusSearch {
        // Hakemus jätetty
        ACTIVE,
        // Hakemus käsittelyssä
        DRAFT,
        // Hakemusta täydennetään
        AMENDING,
        // Lukittu
        LOCKED,
        // Julkaistu
        PUBLISHED
    }

    @Pattern(regexp = "^\\d{0,3}$")
    private String rhyOfficialCode;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rkaOfficialCode;

    @Min(2017)
    private Integer huntingYear;

    private Integer gameSpeciesCode;

    private Long handlerId;

    private Set<StatusSearch> status;

    private Integer applicationNumber;

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    public String getRkaOfficialCode() {
        return rkaOfficialCode;
    }

    public void setRkaOfficialCode(final String rkaOfficialCode) {
        this.rkaOfficialCode = rkaOfficialCode;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(final Long handlerId) {
        this.handlerId = handlerId;
    }

    public Set<StatusSearch> getStatus() {
        return status;
    }

    public void setStatus(final Set<StatusSearch> status) {
        this.status = status;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }
}
