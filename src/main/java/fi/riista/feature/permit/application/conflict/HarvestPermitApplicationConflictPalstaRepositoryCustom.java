package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;
import java.util.Map;

public interface HarvestPermitApplicationConflictPalstaRepositoryCustom {
    List<HarvestPermitApplicationConflictPalsta> listAll(final long batchId,
                                                         HarvestPermitApplication firstApplication,
                                                         HarvestPermitApplication secondApplication);

    List<HarvestPermitApplicationConflictPalsta> listAll(final long batchId,
                                                         HarvestPermitApplication firstApplication,
                                                         List<HarvestPermitApplication> otherApplicationList);

    Map<Long, ConfictSummaryDTO> countConflictSummaries(final long batchId,
                                                        HarvestPermitApplication application,
                                                        List<HarvestPermitApplication> conflicting);
}
