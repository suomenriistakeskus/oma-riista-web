package fi.riista.feature.organization.rhy;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.RiistakeskuksenAlue;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
@DiscriminatorValue("RHY")
public class Riistanhoitoyhdistys extends Organisation {

    @Size(max = 255)
    @Column
    private String poronhoitoalueId;

    @Size(max = 255)
    @Column
    private String hallialueId;

    @Column(name = "is_at_coast")
    private Boolean atCoast;

    public Riistanhoitoyhdistys() {
        super(OrganisationType.RHY);
    }

    public Riistanhoitoyhdistys(RiistakeskuksenAlue alue, String nimiFI, String nimiSV, String officialRhyId) {
        this();
        setNameFinnish(nimiFI);
        setNameSwedish(nimiSV);
        setOfficialCode(officialRhyId);

        this.parentOrganisation = alue;
    }

    public Organisation getRiistakeskuksenAlue() {
        return getClosestAncestorOfType(OrganisationType.RKA).orElse(null);
    }

    public void setRiistakeskuksenAlue(RiistakeskuksenAlue alue) {
        parentOrganisation = alue;
    }

    public String getPoronhoitoalueId() {
        return poronhoitoalueId;
    }

    public void setPoronhoitoalueId(String poronhoitoalueId) {
        this.poronhoitoalueId = poronhoitoalueId;
    }

    public String getHallialueId() {
        return hallialueId;
    }

    public void setHallialueId(String hallialueId) {
        this.hallialueId = hallialueId;
    }

    public Boolean getAtCoast() {
        return atCoast;
    }

    public void setAtCoast(Boolean atCoast) {
        this.atCoast = atCoast;
    }
}
