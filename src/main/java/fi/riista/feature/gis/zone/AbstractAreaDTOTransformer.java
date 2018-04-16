package fi.riista.feature.gis.zone;

import fi.riista.feature.common.entity.HasID;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractAreaDTOTransformer<E extends AreaEntity<Long>, D> extends ListTransformer<E, D> {

    protected static final Function<AreaEntity<?>, GISZone> ZONE_FN = AreaEntity::getZone;

    @Resource
    protected GISZoneRepository gisZoneRepository;

    protected static <T extends AreaEntity<?>> Set<Long> getUniqueZoneIds(final Iterable<T> iterable) {
        return F.getUniqueIdsAfterTransform(iterable, ZONE_FN);
    }

    protected <T extends AreaEntity<?>> Function<T, GISZoneWithoutGeometryDTO> createZoneDTOFunction(
            final Iterable<T> iterable) {

        final Set<Long> zoneIds = getUniqueZoneIds(iterable);
        final List<GISZoneWithoutGeometryDTO> zoneDtos = gisZoneRepository.fetchWithoutGeometry(zoneIds);
        final Map<Long, GISZoneWithoutGeometryDTO> zoneIndex = F.indexById(zoneDtos);

        return t -> Optional.ofNullable(t).map(ZONE_FN).map(HasID::getId).map(zoneIndex::get).orElse(null);
    }

}
