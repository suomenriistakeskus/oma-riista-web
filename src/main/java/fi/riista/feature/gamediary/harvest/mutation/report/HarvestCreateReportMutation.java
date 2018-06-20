package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutationRole;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportExistsException;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestCreateReportMutation implements HarvestMutation {
    private final HarvestMutationRole mutationRole;
    private final Person activePerson;

    public HarvestCreateReportMutation(@Nonnull final HarvestMutationRole mutationRole,
                                       final Person activePerson) {
        this.mutationRole = Objects.requireNonNull(mutationRole);
        this.activePerson = activePerson;
    }

    @Override
    public void accept(final Harvest harvest) {
        if (harvest.isHarvestReportDone()) {
            throw new HarvestReportExistsException(harvest);
        }

        harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        harvest.setHarvestReportDate(DateUtil.now());
        harvest.setHarvestReportAuthor(getHarvestReportAuthor(harvest.getAuthor(), harvest.getHarvestPermit()));
    }

    private Person getHarvestReportAuthor(final Person harvestAuthor, final HarvestPermit harvestPermit) {
        switch (mutationRole) {
            case AUTHOR_OR_ACTOR:
            case PERMIT_CONTACT_PERSON:
                return Objects.requireNonNull(activePerson, "activePerson is missing");
            case MODERATOR:
                if (harvestPermit != null) {
                    return harvestPermit.getOriginalContactPerson();
                }

                return Objects.requireNonNull(harvestAuthor, "harvest author not set");
            default:
                // Should not happen, implementation is faulty
                throw new RuntimeException("Can not resolve report author for mutationRole " + mutationRole);
        }
    }
}
