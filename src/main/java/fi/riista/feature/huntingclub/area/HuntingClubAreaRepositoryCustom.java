package fi.riista.feature.huntingclub.area;

import fi.riista.feature.huntingclub.HuntingClub;

import java.util.List;

public interface HuntingClubAreaRepositoryCustom {
    List<HuntingClubArea> findByClubAndYear(HuntingClub club, Integer year, boolean activeOnly, boolean includeEmpty);

    List<Long> listPois(long areaId);

    void addPois(long areaId, List<Long> pois);

    void updatePois(long areaId, List<Long> pois);

    void removeConnectionsToPoi(long poiGroupId);
}
