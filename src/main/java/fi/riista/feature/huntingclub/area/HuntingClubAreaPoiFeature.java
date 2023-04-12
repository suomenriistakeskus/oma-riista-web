package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.huntingclub.poi.PoiCollectionDTO;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Service
public class HuntingClubAreaPoiFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Transactional(readOnly = true)
    public PoiCollectionDTO listPois(final long clubAreaId) {
        // Authorize update
        requireEntityService.requireHuntingClubArea(clubAreaId, EntityPermission.READ);

        final List<Long> poiIds = ofNullable(huntingClubAreaRepository.listPois(clubAreaId)).orElse(emptyList());

        return new PoiCollectionDTO(poiIds);
    }

    @Transactional
    public void updatePois(final long clubAreaId, final PoiCollectionDTO pois) {
        // Authorize update
        requireEntityService.requireHuntingClubArea(clubAreaId, EntityPermission.UPDATE);

        huntingClubAreaRepository.updatePois(clubAreaId, pois.getPoiIds());
    }
}
