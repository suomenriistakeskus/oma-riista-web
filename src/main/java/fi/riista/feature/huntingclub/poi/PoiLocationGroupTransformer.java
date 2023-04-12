package fi.riista.feature.huntingclub.poi;

import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.util.Collect;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class PoiLocationGroupTransformer extends ListTransformer<PoiLocationGroup, PoiLocationGroupDTO> {

    @Resource
    private LastModifierService lastModifierService;

    @Resource
    private PoiLocationRepository poiLocationRepository;

    @Nonnull
    @Override
    protected List<PoiLocationGroupDTO> transform(@Nonnull final List<PoiLocationGroup> list) {
        final Map<PoiLocationGroup, LastModifierDTO> lastModifierMapping = lastModifierService.getLastModifiers(list);

        final Map<Long, List<PoiLocation>> locationsByPoiId =
                poiLocationRepository.findAllByPoiIn(list).stream()
                        .collect(Collect.groupingByIdOf(PoiLocation::getPoi));

        return list.stream()
                .map(poi -> {
                    final PoiLocationGroupDTO dto = PoiLocationGroupDTO.create(poi);

                    final LastModifierDTO modifier = lastModifierMapping.get(poi);

                    dto.setLastModifiedDate(modifier.getTimestampAsLocalDateTime());
                    dto.setLastModifierName(modifier.getFullName());
                    dto.setLastModifierRiistakeskus(modifier.isAdminOrModerator());

                    final List<PoiLocation> poiLocations =
                            locationsByPoiId.getOrDefault(poi.getId(), Collections.emptyList());
                    dto.setLocations(F.mapNonNullsToList(poiLocations, PoiLocationDTO::new));

                    return dto;
                }).collect(toList());
    }
}
