package fi.riista.feature.gamediary.harvest.mutation.report;

import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HarvestForHuntingDayMutation implements HarvestMutationForReportType {
    private final Long groupHuntingDayId;
    private final Person activePerson;
    private final GroupHuntingDayService groupHuntingDayService;

    public HarvestForHuntingDayMutation(final long groupHuntingDayId,
                                        final Person activePerson,
                                        @Nonnull final GroupHuntingDayService groupHuntingDayService) {
        this.groupHuntingDayId = groupHuntingDayId;
        this.groupHuntingDayService = Objects.requireNonNull(groupHuntingDayService);
        this.activePerson = activePerson;
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setHarvestReportRequired(false);
        clearSeasonFields(harvest);
        clearPermitFields(harvest);

        groupHuntingDayService.linkDiaryEntryToHuntingDay(harvest, groupHuntingDayId, activePerson);
    }

    @Override
    public HarvestReportingType getReportingType() {
        return HarvestReportingType.HUNTING_DAY;
    }
}
