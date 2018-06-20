package fi.riista.feature.gamediary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import org.joda.time.DateTime;

public class HarvestChangeHistoryDTO {

    public HarvestChangeHistoryDTO(final DateTime pointOfTime,
                                   final String reasonForChange,
                                   final HarvestReportState harvestReportState,
                                   final SystemUser user) {
        this.pointOfTime = pointOfTime;
        this.reasonForChange = reasonForChange;
        this.harvestReportState = harvestReportState;

        if (user != null) {
            this.userId = user.getId();
            this.moderator = user.isModeratorOrAdmin();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
        }
    }

    private DateTime pointOfTime;
    private String reasonForChange;
    private HarvestReportState harvestReportState;

    private Long userId;
    private boolean moderator;
    private String firstName;
    private String lastName;

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isModerator() {
        return moderator;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
