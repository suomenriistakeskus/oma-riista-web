package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;
import java.util.Map;

public interface HarvestPermitApplicationConflictPalstaRepositoryCustom {
    List<HarvestPermitApplicationConflictPalsta> listAll(HarvestPermitApplication firstApplication,
                                                         HarvestPermitApplication secondApplication);

    List<HarvestPermitApplicationConflictPalsta> listAll(HarvestPermitApplication firstApplication,
                                                         List<HarvestPermitApplication> otherApplicationList);

    Map<Long, ConfictSummaryDTO> countConflictSummaries(HarvestPermitApplication application,
                                                        List<HarvestPermitApplication> conflicting);
}
