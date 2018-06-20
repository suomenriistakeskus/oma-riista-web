package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.gamediary.harvest.mutation.HarvestPreviousState;
import fi.riista.feature.harvestpermit.report.HarvestReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.util.DateUtil;

import java.util.Objects;

public class HarvestUpdateReportMutation implements HarvestMutation {
    private final HarvestPreviousState previousState;
    private final HarvestMutationRole mutationRole;

    public HarvestUpdateReportMutation(final HarvestMutationRole mutationRole,
                                       final HarvestPreviousState previousState) {
        this.mutationRole = Objects.requireNonNull(mutationRole);
        this.previousState = Objects.requireNonNull(previousState);
    }

    @Override
    public void accept(final Harvest harvest) {
        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            throw new HarvestReportExistsException(harvest);
        }

        // Do not update existing report as moderator
        if (mutationRole == HarvestMutationRole.MODERATOR) {
            return;
        }

        // Do not update if reporting type not changed
        if (previousState.shouldInitReportState(harvest)) {
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportDate(DateUtil.now());
        }
    }
}
