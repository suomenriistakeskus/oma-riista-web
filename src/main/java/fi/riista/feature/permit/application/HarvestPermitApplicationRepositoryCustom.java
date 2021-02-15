package fi.riista.feature.permit.application;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.conflict.HarvestPermitApplicationConflictPalsta;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface HarvestPermitApplicationRepositoryCustom {
    Slice<HarvestPermitApplication> search(HarvestPermitApplicationSearchDTO dto, Pageable pageRequest);

    List<HarvestPermitApplication> search(HarvestPermitApplicationSearchDTO dto);

    List<HarvestPermitApplication> searchForRhy(String officialCode, int year, @Nullable Integer gameSpeciesCode);

    List<Integer> searchYears(HarvestPermitApplicationSearchDTO dto);

    List<SystemUser> listHandlers();

    List<HarvestPermitApplicationConflictPalsta> findIntersectingPalsta(HarvestPermitApplication firstApplication,
                                                                        HarvestPermitApplication secondApplication);

    List<HarvestPermitApplication> listPostalQueue();

    List<HarvestPermitApplication> listByAnnualPermitsToRenew(final Long handlerId);

    Map<Long, Integer> getAnnualPermitsToRenewByHandlerId();

    List<Harvest> findNonEdibleHarvestsByPermit(HarvestPermit original);

    List<HarvestPermitApplication> findByOriginalPermit(final HarvestPermit originalPermit);
}
