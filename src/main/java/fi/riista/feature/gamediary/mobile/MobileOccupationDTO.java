package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.occupation.OccupationType;
import org.joda.time.LocalDate;

import java.util.Map;

public class MobileOccupationDTO implements HasBeginAndEndDate {

    private MobileOrganisationDTO organisation;

    private LocalDate beginDate;
    private LocalDate endDate;

    private OccupationType occupationType;
    private Map<String, String> name;

    public MobileOrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(MobileOrganisationDTO organisation) {
        this.organisation = organisation;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }
}
