package fi.riista.feature.harvestpermit.report;

import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotNull;

public class HarvestReportStateChangeDTO {
    @NotNull
    private Long harvestId;

    @NotNull
    private Integer rev;

    @NotNull
    private HarvestReportState to;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String reason;

    public Long getHarvestId() {
        return harvestId;
    }

    public void setHarvestId(final Long harvestId) {
        this.harvestId = harvestId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
