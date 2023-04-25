package fi.riista.feature.huntingclub.area;

import static java.util.Collections.singletonList;

import fi.riista.config.AlertLoggingConstants;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.OnlyStateAreaService;
import fi.riista.feature.gis.zone.CalculateCombinedGeometryJob;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneEditService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.IllegalZoneStateTransitionException;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.geojson.FeatureCollection;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class HuntingClubAreaZoneFeature {

    private static final Logger LOG = LoggerFactory.getLogger(HuntingClubAreaZoneFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneEditService zoneEditService;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private OnlyStateAreaService onlyStateAreaService;

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

    @Transactional(timeout = 300)
    public void updateGeoJSON(final long id, final FeatureCollection featureCollection) {
        final HuntingClubArea area = requireEntityService.requireHuntingClubArea(id, EntityPermission.UPDATE);

        final GISZone zone = area.getZone() != null ? area.getZone() : new GISZone();
        zoneEditService.storeFeaturesAsync(featureCollection, zone);

        area.setZone(zone);
        area.setModificationTimeToCurrentTime();
    }

    @Transactional(readOnly = true, timeout = 60)
    public List<Long> findZonesInStatusPending() {
        return zoneRepository.findInStatusPending();
    }

    // No @Transaction here to avoid long-running transactions during processing
    public void startProcessing(final long zoneId) {

        try {
            zoneEditService.setZoneStatusToProcessing(zoneId);
        } catch (final IllegalZoneStateTransitionException ignored) {
            // Another process has already taken care of this
            LOG.info("Already processed zoneId={}", zoneId);
            return;
        } catch (final Exception e) {
            LOG.error("{} Could not set zone status to processing for zoneId={}.",
                    AlertLoggingConstants.CLUB_AREA_ALERT_PREFIX, zoneId, e);
            return;
        }

        try {
            LOG.info("Start processing hunting club area zoneId={}", zoneId);

            zoneEditService.calculateCombinedGeometry(zoneId);

            LOG.info("starting updateAreaSize zoneId={}", zoneId);
            final boolean onlyStateLand = onlyStateAreaService.shouldContainOnlyStateLand(singletonList(zoneId));
            zoneRepository.calculateAreaSize(zoneId, onlyStateLand);

            LOG.info("setStatusToReady zoneId={}", zoneId);
            zoneEditService.setZoneStatusToReady(zoneId);

        } catch (final Exception e) {
            LOG.error("{} Failed to calculate combined geometry for zone {}.",
                    AlertLoggingConstants.CLUB_AREA_ALERT_PREFIX, zoneId, e);
            zoneEditService.setZoneStatusToProcessFailed(zoneId);
        }
    }

    @Transactional(readOnly = true, timeout = 60)
    public GISZone.StatusCode getZoneStatus(final long id) {
        final HuntingClubArea area = requireEntityService.requireHuntingClubArea(id, EntityPermission.NONE);

        final GISZone zone = area.getZone() != null ? area.getZone() : new GISZone();
        return zone.getStatus();
    }

    @Transactional(readOnly = true, timeout = 60)
    public void checkPendingQueueHealth() {
        final long pendingStatusCount = zoneRepository.findInStatusPending().size();

        if (pendingStatusCount > CalculateCombinedGeometryJob.CONCURRENCY_LIMIT) {
            LOG.warn("{} Hunting club area zone calculation queue length is over the threshold. Current count: {}/{}",
                    AlertLoggingConstants.CLUB_AREA_ALERT_PREFIX,
                    pendingStatusCount,
                    CalculateCombinedGeometryJob.CONCURRENCY_LIMIT);
        }
    }

    @Transactional
    public void setTooLongProcessedZonesStatusFailed() {
        final DateTime processingSince = DateUtil.now().minus(GISZone.CALCULATION_RETRY_PERIOD);
        zoneRepository.updateTooLongProcessedZonesStatus(
                processingSince,
                GISZone.StatusCode.PROCESSING_FAILED
        );
    }

}
