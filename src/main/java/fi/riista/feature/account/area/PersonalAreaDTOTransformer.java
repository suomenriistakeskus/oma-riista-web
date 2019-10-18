package fi.riista.feature.account.area;

import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.metsahallitus.MetsahallitusMaterialYear;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.idSet;
import static java.util.stream.Collectors.toList;

@Component
public class PersonalAreaDTOTransformer extends ListTransformer<PersonalArea, PersonalAreaDTO> {

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private MetsahallitusMaterialYear metsahallitusMaterialYear;

    @Nonnull
    @Override
    protected List<PersonalAreaDTO> transform(@Nonnull final List<PersonalArea> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final int latestHirviYear = metsahallitusMaterialYear.getLatestHirviYear();

        final Set<Long> zoneIds = F.stream(list).map(PersonalArea::getZone).collect(idSet());
        final Function<PersonalArea, GISZoneWithoutGeometryDTO> areaToZoneMapping = createAreaSizeMapping(zoneIds);
        final Function<PersonalArea, GISBounds> areaToBoundsMapping = createAreaBoundsMapping(zoneIds);

        return list.stream().map(area -> PersonalAreaDTO
                .create(area, areaToZoneMapping.apply(area), areaToBoundsMapping.apply(area), latestHirviYear))
                .collect(toList());
    }

    private Function<PersonalArea, GISZoneWithoutGeometryDTO> createAreaSizeMapping(final Set<Long> zoneIds) {
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = zoneRepository.fetchWithoutGeometry(zoneIds);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }

    private Function<PersonalArea, GISBounds> createAreaBoundsMapping(final Set<Long> zoneIds) {
        final Map<Long, GISBounds> mapping = zoneRepository.getBounds(zoneIds, GISUtils.SRID.WGS84);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }
}
