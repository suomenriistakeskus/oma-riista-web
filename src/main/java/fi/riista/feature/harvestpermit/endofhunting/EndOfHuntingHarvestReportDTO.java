package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.statistics.HarvestPermitUsageDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import org.joda.time.DateTime;

import java.util.List;

public class EndOfHuntingHarvestReportDTO {
    private static class Permissions {
        public boolean create;
        public boolean remove;
        public boolean accept;
    }

    private final Permissions actions = new Permissions();
    private Long permitId;
    private Integer permitRev;
    private String permitNumber;
    private String permitType;
    private List<HarvestPermitUsageDTO> speciesAmounts;
    private List<HarvestDTO> harvests;

    private DateTime harvestReportDate;
    private HarvestReportState harvestReportState;
    private PersonWithHunterNumberDTO harvestReportAuthor;

    public EndOfHuntingHarvestReportDTO(final HarvestPermit harvestPermit,
                                        final SystemUser activeUser, final List<HarvestPermitUsageDTO> speciesAmounts,
                                        final List<HarvestDTO> harvests) {
        this.permitId = harvestPermit.getId();
        this.permitRev = harvestPermit.getConsistencyVersion();
        this.permitNumber = harvestPermit.getPermitNumber();
        this.permitType = harvestPermit.getPermitType();
        this.speciesAmounts = speciesAmounts;
        this.harvests = harvests;

        if (harvestPermit.isHarvestReportDone()) {
            this.harvestReportDate = harvestPermit.getHarvestReportDate();
            this.harvestReportState = harvestPermit.getHarvestReportState();
            this.harvestReportAuthor = PersonWithHunterNumberDTO.create(harvestPermit.getHarvestReportAuthor());
        }

        this.actions.create = harvestPermit.canCreateEndOfHuntingReport(activeUser);
        this.actions.remove = harvestPermit.canRemoveEndOfHuntingReport(activeUser);
        this.actions.accept = harvestPermit.canAcceptOrRejectEndOfHuntingReport(activeUser);
    }

    public Permissions getActions() {
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

    public List<HarvestPermitUsageDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<HarvestDTO> getHarvests() {
        return harvests;
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
}
