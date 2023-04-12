package fi.riista.feature.huntingclub.poi;

import com.google.common.collect.Sets;
import fi.riista.util.F;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PoiLocationService {
    @Resource
    private PoiLocationRepository poiLocationRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<PoiLocationDTO> getLocations(final PoiLocationGroup poi) {
        return F.mapNonNullsToList(poiLocationRepository.findAllByPoi(poi), PoiLocationDTO::new);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateLocations(final PoiLocationGroup poi,
                                final List<PoiLocationDTO> dtos) {

        final Map<Long, PoiLocation> existingById = F.indexById(poiLocationRepository.findAllByPoi(poi));
        final List<PoiLocation> toAdd = new ArrayList();

        dtos.stream().forEach(dto -> {
            PoiLocation poiLocation = existingById.get(dto.getId());
            if (poiLocation == null) {
                poiLocation = new PoiLocation();
                toAdd.add(poiLocation);
            }
            updateFrom(poi, poiLocation, dto);
        });

        final Set<Long> toDelete = Sets.difference(existingById.keySet(), F.getUniqueIds(dtos));

        poiLocationRepository.saveAll(toAdd);
        poiLocationRepository.deleteAllByIdIn(toDelete);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteByPoi(final PoiLocationGroup poi) {
        poiLocationRepository.deleteAllByPoi(poi);
    }

    private void updateFrom(final PoiLocationGroup poi,
                            final PoiLocation location,
                            final PoiLocationDTO dto) {
        location.setPoi(poi);
        location.setVisibleId(dto.getVisibleId());
        location.setDescription(dto.getDescription());
        location.setGeoLocation(dto.getGeoLocation());
    }
}
