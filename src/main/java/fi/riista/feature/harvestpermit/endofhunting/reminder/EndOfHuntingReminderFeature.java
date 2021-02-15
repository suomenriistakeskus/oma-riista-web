package fi.riista.feature.harvestpermit.endofhunting.reminder;

import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import org.joda.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class EndOfHuntingReminderFeature {

    public enum EndOfHuntingReminderType {
        ALL,
        ONE_YEAR,
        MULTI_YEAR
    }

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<EndOfHuntingReminderDTO> getMissingEndOfHuntingReports(final @Nonnull LocalDate permitEndDate,
                                                                       final EndOfHuntingReminderType endOfHuntingReminderType) {
        requireNonNull(permitEndDate);
        return harvestPermitRepository.findMissingEndOfHuntingReports(permitEndDate, endOfHuntingReminderType);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<EndOfHuntingReminderDTO> getMissingEndOfHuntingReports(final int permitEndYear,
                                                                       final EndOfHuntingReminderType endOfHuntingReminderType) {
        return harvestPermitRepository.findMissingEndOfHuntingReports(permitEndYear, endOfHuntingReminderType);
    }
}
