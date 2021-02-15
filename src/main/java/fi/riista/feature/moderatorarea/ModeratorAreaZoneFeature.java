package fi.riista.feature.moderatorarea;

import com.newrelic.api.agent.Trace;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneEditService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class ModeratorAreaZoneFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneEditService zoneEditService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection geoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requireModeratorArea(areaId, EntityPermission.READ))
                .map(ModeratorArea::getZone)
                .map(zoneEditService::getFeatures)
                .orElseGet(FeatureCollection::new);
    }

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection combinedGeoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requireModeratorArea(areaId, EntityPermission.READ))
                .map(ModeratorArea::getZone)
                .map(zoneId -> zoneRepository.getCombinedPolygonFeatures(zoneId.getId(), GISUtils.SRID.WGS84))
                .orElseGet(FeatureCollection::new);
    }

    @Trace
    @Transactional(timeout = 300)
    public void updateGeoJSON(final long id, final FeatureCollection featureCollection) {
        final ModeratorArea area = requireEntityService.requireModeratorArea(id, EntityPermission.UPDATE);

        final GISZone zone = area.getZone() != null ? area.getZone() : new GISZone();
        zoneEditService.storeFeatures(featureCollection, zone);

        area.setZone(zone);
        area.setModificationTimeToCurrentTime();
    }

    @Trace
    @Async
    @Transactional
    public void updateAreaSize(final long areaId) {
        final ModeratorArea area = requireEntityService.requireModeratorArea(areaId, EntityPermission.UPDATE);
        final long zoneId = requireNonNull(area.getZone()).getId();
        try {
            zoneEditService.updateAreaSize(zoneId);
        } catch (Exception e) {
            zoneEditService.markCalculationFailed(zoneId);
        }
    }
}
