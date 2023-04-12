package fi.riista.feature.huntingclub.poi.mobile;

import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationGroup;
import fi.riista.feature.huntingclub.poi.PoiLocationGroupRepository;
import fi.riista.feature.huntingclub.poi.PoiLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class MobilePoiListFeature {

    @Resource
    private HuntingClubAreaRepository areaRepository;

    @Resource
    private PoiLocationRepository locationRepository;

    @Resource
    private PoiLocationGroupRepository poiLocationGroupRepository;


    @Transactional(readOnly = true)
    public List<MobilePoiLocationGroupDTO> list(final String externalId) {
        // No authorization here to allow visiting hunters to list POIs.
        return areaRepository.findByExternalId(externalId)
                .map(HuntingClubArea::getId)
                .map(areaRepository::listPois)
                .map(poiLocationGroupRepository::findAllById)
                .map(pois -> getPoisWithLocations(pois))
                .orElse(emptyList());

    }

    private List<MobilePoiLocationGroupDTO> getPoisWithLocations(final List<PoiLocationGroup> pois) {
        final Map<Long, List<MobilePoiLocationDTO>> locationsByGroup =
                locationRepository.findAllByPoiIn(pois).stream()
                        .map(MobilePoiLocationDTO::new)
                        .collect(groupingBy(MobilePoiLocationDTO::getPoiId));

        return pois.stream()
                .map(poiLocationGroup ->
                        toDto(poiLocationGroup, locationsByGroup.getOrDefault(poiLocationGroup.getId(), emptyList())))
                .collect(toList());
    }

    private MobilePoiLocationGroupDTO toDto(final PoiLocationGroup poiLocationGroup, final List<MobilePoiLocationDTO> locations) {
        final MobilePoiLocationGroupDTO dto = MobilePoiLocationGroupDTO.create(poiLocationGroup);
        dto.setLocations(locations);
        return dto;
    }


}
