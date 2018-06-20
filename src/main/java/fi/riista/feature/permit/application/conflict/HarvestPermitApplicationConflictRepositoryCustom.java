package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.permit.application.HarvestPermitApplication;

import java.util.List;

public interface HarvestPermitApplicationConflictRepositoryCustom {
    List<HarvestPermitApplication> listAllConflicting(HarvestPermitApplication application);
}
