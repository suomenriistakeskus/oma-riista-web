package fi.riista.feature.harvestpermit.endofhunting;

import org.hibernate.validator.constraints.SafeHtml;

public class EndOfHuntingReportModeratorCommentsDTO {
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.BASIC)
    private String endOfHuntingReportComments;

    public void setEndOfHuntingReportComments(final String endOfHuntingReportComments) {
        this.endOfHuntingReportComments = endOfHuntingReportComments;
    }

    public String getEndOfHuntingReportComments() {
        return endOfHuntingReportComments;
    }
}
