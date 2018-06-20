package fi.riista.feature.permit.area;

import fi.riista.feature.huntingclub.HuntingClub;

import java.util.List;

public interface HarvestPermitAreaRepositoryCustom {
    List<Integer> listHuntingYears(HuntingClub club);

    List<HarvestPermitArea> listByClub(HuntingClub club, int huntingYear);
}
