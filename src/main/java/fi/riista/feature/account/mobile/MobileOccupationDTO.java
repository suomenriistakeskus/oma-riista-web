package fi.riista.feature.account.mobile;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.occupation.OccupationType;
import org.joda.time.LocalDate;

import java.util.Map;

public class MobileOccupationDTO implements HasBeginAndEndDate {

    private long id;

    private OccupationType occupationType;
    private Map<String, String> name;

    private LocalDate beginDate;
    private LocalDate endDate;

    private MobileOrganisationDTO organisation;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(final Map<String, String> name) {
        this.name = name;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public MobileOrganisationDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final MobileOrganisationDTO organisation) {
        this.organisation = organisation;
    }
}
