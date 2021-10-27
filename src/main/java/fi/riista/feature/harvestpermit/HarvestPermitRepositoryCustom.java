package fi.riista.feature.harvestpermit;

import fi.riista.api.pub.PublicCarnivorePermitDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderDTO;
import fi.riista.feature.harvestpermit.endofhunting.reminder.EndOfHuntingReminderFeature.EndOfHuntingReminderType;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HarvestPermitRepositoryCustom {

    Slice<PublicCarnivorePermitDTO> findCarnivorePermits(
            final String permitNumber, final Integer speciesCode, final Integer calendarYear,
            final String rkaCode, final Pageable pageRequest);

    Optional<Long> isCarnivorePermitAvailable(String permitNumber);

    List<EndOfHuntingReminderDTO> findMissingEndOfHuntingReports(final LocalDate permitEndDate,
                                                                 final EndOfHuntingReminderType endOfHuntingReminderType);
    List<EndOfHuntingReminderDTO> findMissingEndOfHuntingReports(final int permitEndYear,
                                                                 final EndOfHuntingReminderType endOfHuntingReminderType);

    Map<HuntingClubGroup, HarvestPermit> getByHuntingGroup(Collection<HuntingClubGroup> groups);

    List<HarvestPermit> findByHuntingYearAndSpeciesAndCategory(int huntingYear, GameSpecies species, HarvestPermitCategory category);
}
