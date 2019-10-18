package fi.riista.feature.permit.area;

import fi.riista.feature.huntingclub.HuntingClub;

import java.util.List;


public interface HarvestPermitAreaRepositoryCustom {
    List<HarvestPermitArea> listActiveApplicationAreas(HuntingClub club, int huntingYear);

    List<Long> findPartnerZoneIds(HarvestPermitArea permitArea);
}
