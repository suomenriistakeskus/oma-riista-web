package fi.riista.feature.huntingclub.area;

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

@Component
public class HuntingClubAreaZoneFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneEditService zoneEditService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection geoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requireHuntingClubArea(areaId, EntityPermission.READ))
                .map(HuntingClubArea::getZone)
                .map(zoneEditService::getFeatures)
                .orElseGet(FeatureCollection::new);
    }

    @Transactional(readOnly = true, timeout = 60)
    public FeatureCollection combinedGeoJSON(final long areaId) {
        return Optional
                .of(requireEntityService.requireHuntingClubArea(areaId, EntityPermission.READ))
                .map(HuntingClubArea::getZone)
                .map(zoneId -> zoneRepository.getCombinedPolygonFeatures(zoneId.getId(), GISUtils.SRID.WGS84))
                .orElseGet(FeatureCollection::new);
    }

    @Trace
    @Transactional(timeout = 300)
    public long updateGeoJSON(final long id, final FeatureCollection featureCollection) {
        final HuntingClubArea area = requireEntityService.requireHuntingClubArea(id, EntityPermission.UPDATE);

        final GISZone zone = area.getZone() != null ? area.getZone() : new GISZone();
        zoneEditService.storeFeatures(featureCollection, zone);

        area.setZone(zone);
        area.setModificationTimeToCurrentTime();

        return zone.getId();
    }

    @Trace
    @Async
    @Transactional(timeout = 900)
    public void updateAreaSize(final long zoneId) {
        zoneEditService.updateAreaSize(zoneId);
    }
}
