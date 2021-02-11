package fi.riista.feature.natura;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotBlank;

@JsonTypeName("info")
public class NaturaAreaInfoDTO {

    @NotBlank
    private String naturaId;

    private String conservationId;

    @NotBlank
    private String nameFI;

    private String nameSV;

    @NotBlank
    private String conservationType;

    private String areaHectares;

    private String decreeLink;

    public String getNaturaId() {
        return naturaId;
    }

    public void setNaturaId(final String naturaId) {
        this.naturaId = naturaId;
    }

    public String getConservationId() {
        return conservationId;
    }

    public void setConservationId(final String conservationId) {
        this.conservationId = conservationId;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameFI(final String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setNameSV(final String nameSV) {
        this.nameSV = nameSV;
    }

    public String getConservationType() {
        return conservationType;
    }

    public void setConservationType(final String conservationType) {
        this.conservationType = conservationType;
    }

    public String getAreaHectares() {
        return areaHectares;
    }

    public void setAreaHectares(final String areaHectares) {
        this.areaHectares = areaHectares;
    }

    public String getDecreeLink() {
        return decreeLink;
    }

    public void setDecreeLink(final String decreeLink) {
        this.decreeLink = decreeLink;
    }
}
