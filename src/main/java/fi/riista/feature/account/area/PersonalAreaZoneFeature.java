package fi.riista.feature.account.area;

import com.newrelic.api.agent.Trace;
import fi.riista.feature.RequireEntityService;
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

@Component
public class PersonalAreaZoneFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneEditService zoneEditService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection geoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requirePersonalArea(areaId, EntityPermission.READ))
                .map(PersonalArea::getZone)
                .map(zoneEditService::getFeatures)
                .orElseGet(FeatureCollection::new);
    }

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection combinedGeoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requirePersonalArea(areaId, EntityPermission.READ))
                .map(PersonalArea::getZone)
                .map(zoneId -> zoneRepository.getCombinedPolygonFeatures(zoneId.getId(), GISUtils.SRID.WGS84))
                .orElseGet(FeatureCollection::new);
    }

    @Trace
    @Transactional(timeout = 300)
    public long updateGeoJSON(final long id, final FeatureCollection featureCollection) {
        final PersonalArea area = requireEntityService.requirePersonalArea(id, EntityPermission.UPDATE);

        zoneEditService.storeFeatures(featureCollection, area.getZone());

        area.setZone(area.getZone());
        area.setModificationTimeToCurrentTime();

        return area.getZone().getId();
    }

    @Trace
    @Async
    @Transactional(timeout = 900)
    public void updateAreaSize(final long zoneId) {
        zoneEditService.updateAreaSize(zoneId);
    }
}
