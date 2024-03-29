package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.usage.PermitUsageDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import org.joda.time.DateTime;

import java.util.List;

public class EndOfPermitPeriodReportDTO {
    private static class Permissions {
        public boolean create;
        public boolean remove;
        public boolean accept;
    }

    private final EndOfPermitPeriodReportDTO.Permissions actions = new EndOfPermitPeriodReportDTO.Permissions();
    private Long permitId;
    private Integer permitRev;
    private String permitNumber;
    private String permitType;
    private List<PermitUsageDTO> usages;

    private DateTime harvestReportDate;
    private HarvestReportState harvestReportState;
    private PersonWithHunterNumberDTO harvestReportAuthor;

    private String endOfHuntingReportComments;

    public EndOfPermitPeriodReportDTO(final HarvestPermit harvestPermit,
                                      final SystemUser activeUser,
                                      final List<PermitUsageDTO> usages) {
        this.permitId = harvestPermit.getId();
        this.permitRev = harvestPermit.getConsistencyVersion();
        this.permitNumber = harvestPermit.getPermitNumber();
        this.permitType = harvestPermit.getPermitType();
        this.usages = usages;

        if (harvestPermit.isHarvestReportDone()) {
            this.harvestReportDate = harvestPermit.getHarvestReportDate();
            this.harvestReportState = harvestPermit.getHarvestReportState();
            this.harvestReportAuthor = PersonWithHunterNumberDTO.create(harvestPermit.getHarvestReportAuthor());
        }

        this.actions.create = harvestPermit.canCreateEndOfHuntingReport(activeUser);
        this.actions.remove = harvestPermit.canRemoveEndOfHuntingReport(activeUser);
        this.actions.accept = harvestPermit.canAcceptOrRejectEndOfHuntingReport(activeUser);

        this.endOfHuntingReportComments = harvestPermit.getEndOfHuntingReportComments();
    }

    public EndOfPermitPeriodReportDTO.Permissions getActions() {
        return actions;
    }

    public Long getPermitId() {
        return permitId;
    }

    public Integer getPermitRev() {
        return permitRev;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public List<PermitUsageDTO> getUsages() {
        return usages;
    }

    public DateTime getHarvestReportDate() {
        return harvestReportDate;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public PersonWithHunterNumberDTO getHarvestReportAuthor() {
        return harvestReportAuthor;
    }

    public String getEndOfHuntingReportComments() {
        return endOfHuntingReportComments;
    }
}
