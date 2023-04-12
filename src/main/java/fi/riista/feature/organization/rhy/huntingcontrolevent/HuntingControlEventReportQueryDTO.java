package fi.riista.feature.organization.rhy.huntingcontrolevent;

public class HuntingControlEventReportQueryDTO {
    public enum HuntingControlSubsidyFilter {
        ACCEPTED_SUBSIDIZED,
        ACCEPTED
    }

    private int year;
    private HuntingControlEventType eventType;
    private HuntingControlCooperationType cooperationType;
    private HuntingControlEventStatus status;
    private HuntingControlSubsidyFilter subsidized;

    public HuntingControlEventReportQueryDTO() {
    }

    public HuntingControlEventReportQueryDTO(final int year,
                                             final HuntingControlEventType eventType,
                                             final HuntingControlCooperationType cooperationType,
                                             final HuntingControlEventStatus status,
                                             final HuntingControlSubsidyFilter subsidized) {
        this.year = year;
        this.eventType = eventType;
        this.cooperationType = cooperationType;
        this.status = status;
        this.subsidized = subsidized;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public HuntingControlEventType getEventType() {
        return eventType;
    }

    public void setEventType(final HuntingControlEventType eventType) {
        this.eventType = eventType;
    }

    public HuntingControlCooperationType getCooperationType() {
        return cooperationType;
    }

    public void setCooperationType(final HuntingControlCooperationType cooperationType) {
        this.cooperationType = cooperationType;
    }

    public HuntingControlEventStatus getStatus() {
        return status;
    }

    public void setStatus(final HuntingControlEventStatus status) {
        this.status = status;
    }

    public HuntingControlSubsidyFilter getSubsidized() {
        return subsidized;
    }

    public void setSubsidized(final HuntingControlSubsidyFilter subsidized) {
        this.subsidized = subsidized;
    }
}
