package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.harvestpermit.report.HarvestReportState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class EndOfHuntingHarvestReportStateChangeDTO {
    @NotNull
    private Long id;

    @NotNull
    private Integer rev;

    @NotNull
    private HarvestReportState to;

    @Valid
    private EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public HarvestReportState getTo() {
        return to;
    }

    public void setTo(final HarvestReportState to) {
        this.to = to;
    }

    public EndOfHuntingReportModeratorCommentsDTO getEndOfHuntingReportComments() {
        return endOfHuntingReportComments;
    }

    public void setEndOfHuntingReportComments(final EndOfHuntingReportModeratorCommentsDTO endOfHuntingReportComments) {
        this.endOfHuntingReportComments = endOfHuntingReportComments;
    }
}
