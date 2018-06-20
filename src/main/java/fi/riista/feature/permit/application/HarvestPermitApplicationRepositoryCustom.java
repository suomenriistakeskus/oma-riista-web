package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalsta;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;

import java.util.List;

public interface HarvestPermitApplicationRepositoryCustom {
    List<HarvestPermitApplication> search(HarvestPermitApplicationSearchDTO dto);

    List<Integer> searchYears(HarvestPermitApplicationSearchDTO dto);

    List<SystemUser> listHandlers();

    List<HarvestPermitApplicationConflictPalsta> findIntersectingPalsta(HarvestPermitApplication firstApplication,
                                                                        HarvestPermitApplication secondApplication);

    List<HarvestPermitApplication> listByRevisionCreator(Long userId);

    List<HarvestPermitApplication> listPostalQueue();
}
