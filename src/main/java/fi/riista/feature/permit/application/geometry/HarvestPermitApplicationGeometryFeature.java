package fi.riista.feature.permit.application.geometry;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.HarvestPermitApplicationLockedCondition;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaRepository;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class HarvestPermitApplicationGeometryFeature {

    @Resource
    private HarvestPermitApplicationGeoJsonService harvestPermitApplicationGeoJsonService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private HarvestPermitAreaRhyRepository harvestPermitAreaRhyRepository;

    @Resource
    private HarvestPermitAreaHtaRepository harvestPermitAreaHtaRepository;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    private HarvestPermitArea getApplicationArea(final long applicationId, final EntityPermission permission) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(applicationId, permission);
        application.assertHasPermitArea();

        return application.getArea();
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationAreaDTO getPermitArea(final long applicationId) {
        final HarvestPermitArea permitArea = getApplicationArea(applicationId, EntityPermission.READ);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(permitArea.getZone().getId());

        return new HarvestPermitApplicationAreaDTO(permitArea.getStatus(), areaSize,
                permitArea.isFreeHunting(),
                F.mapNonNullsToList(permitArea.getRhy(), HarvestPermitAreaRhyDTO::create),
                F.mapNonNullsToList(permitArea.getHta(), HarvestPermitAreaHtaDTO::create));
    }

    @Transactional(readOnly = true)
    public HarvestPermitArea.StatusCode getStatus(final long applicationId) {
        return getApplicationArea(applicationId, EntityPermission.NONE).getStatus();
    }

    @Transactional(readOnly = true)
    public GISBounds getBounds(final long applicationId) {
        return Optional.ofNullable(getApplicationArea(applicationId, EntityPermission.READ))
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> gisZoneRepository.getBounds(zoneId, GISUtils.SRID.WGS84))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public FeatureCollection getGeometry(final long applicationId) {
        return harvestPermitApplicationGeoJsonService.getGeometry(getApplicationArea(applicationId, EntityPermission.READ));
    }

    @Transactional(readOnly = true)
    public FeatureCollection getGeometry(final long applicationId, final String outputStyle) {
        return getFeatures(outputStyle, getApplicationArea(applicationId, EntityPermission.READ));
    }

    private FeatureCollection getFeatures(final String outputStyle, final HarvestPermitArea permitArea) {
        return "partner".equals(outputStyle)
                ? harvestPermitApplicationGeoJsonService.getGeometry(permitArea)
                : gisZoneRepository.getCombinedPolygonFeatures(permitArea.getZone().getId(), GISUtils.SRID.WGS84);
    }

    @Transactional
    public void setReadyForProcessing(final long applicationId) {
        final HarvestPermitArea harvestPermitArea = getApplicationArea(applicationId, EntityPermission.UPDATE);
        harvestPermitArea.setStatusPending().ifPresent(harvestPermitAreaEventRepository::save);
    }

    @Transactional
    public void setIncomplete(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        harvestPermitApplicationLockedCondition.assertCanUpdate(application);

        application.assertHasPermitArea();

        final HarvestPermitArea harvestPermitArea = application.getArea();

        harvestPermitArea.setStatusIncomplete().ifPresent(harvestPermitAreaEventRepository::save);

        final GISZone zone = harvestPermitArea.getZone();
        zone.setComputedAreaSize(0);
        zone.setWaterAreaSize(0);
        zone.setStateLandAreaSize(null);
        zone.setPrivateLandAreaSize(null);
        zone.setGeom(null);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);
    }
}
