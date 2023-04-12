package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.OrganisationType;
import java.util.Set;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

public class HuntingControlEventSearchParametersDTO {
    private LocalDate beginDate;
    private LocalDate endDate;
    private Set<HuntingControlEventType> types;
    private Set<HuntingControlEventStatus> statuses;
    private Set<HuntingControlCooperationType> cooperationTypes;
    private OrganisationType orgType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String orgCode;

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

    public Set<HuntingControlEventType> getTypes() {
        return types;
    }

    public void setTypes(final Set<HuntingControlEventType> types) {
        this.types = types;
    }

    public Set<HuntingControlEventStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<HuntingControlEventStatus> statuses) {
        this.statuses = statuses;
    }

    public Set<HuntingControlCooperationType> getCooperationTypes() {
        return cooperationTypes;
    }

    public void setCooperationTypes(final Set<HuntingControlCooperationType> cooperationTypes) {
        this.cooperationTypes = cooperationTypes;
    }

    public OrganisationType getOrgType() {
        return orgType;
    }

    public void setOrgType(final OrganisationType orgType) {
        this.orgType = orgType;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(final String orgCode) {
        this.orgCode = orgCode;
    }
}
