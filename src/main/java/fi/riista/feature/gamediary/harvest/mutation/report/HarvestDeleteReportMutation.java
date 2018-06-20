package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.harvestpermit.report.HarvestReportExistsException;

import java.util.Objects;

public class HarvestDeleteReportMutation implements HarvestMutation {
    private final HarvestMutationRole mutationRole;

    public HarvestDeleteReportMutation(final HarvestMutationRole mutationRole) {
        this.mutationRole = Objects.requireNonNull(mutationRole);
    }

    @Override
    public void accept(final Harvest harvest) {
        if (!harvest.isHarvestReportDone()) {
            // Nothing to do
            return;
        }

        if (mutationRole == HarvestMutationRole.MODERATOR) {
            throw new HarvestReportExistsException(harvest);
        }

        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            throw new HarvestReportExistsException(harvest);
        }

        // Delete existing
        harvest.setHarvestReportState(null);
        harvest.setHarvestReportAuthor(null);
        harvest.setHarvestReportDate(null);
    }
}
