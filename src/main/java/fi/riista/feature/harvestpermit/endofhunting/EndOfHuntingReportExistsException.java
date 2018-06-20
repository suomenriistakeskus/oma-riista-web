package fi.riista.feature.harvestpermit.endofhunting;

import fi.riista.feature.harvestpermit.HarvestPermit;

import javax.annotation.Nonnull;

public class EndOfHuntingReportExistsException extends IllegalStateException {
    public EndOfHuntingReportExistsException(@Nonnull final HarvestPermit permit) {

    }
}
