package fi.riista.feature.permit.application.geometry;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationLockedCondition;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationData;
import fi.riista.feature.permit.application.amendment.AmendmentApplicationDataRepository;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationAreaDTO;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaEventRepository;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaDTO;
import fi.riista.feature.permit.area.hta.HarvestPermitAreaHtaRepository;
import fi.riista.feature.permit.area.mml.HarvestPermitAreaMmlRepository;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyDTO;
import fi.riista.feature.permit.area.rhy.HarvestPermitAreaRhyRepository;
import fi.riista.feature.permit.area.verotuslohko.HarvestPermitAreaVerotusLohkoRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptySet;

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
    private HarvestPermitAreaVerotusLohkoRepository harvestPermitAreaVerotusLohkoRepository;

    @Resource
    private HarvestPermitAreaMmlRepository harvestPermitAreaMmlRepository;

    @Resource
    private HarvestPermitAreaEventRepository harvestPermitAreaEventRepository;

    @Resource
    private AmendmentApplicationDataRepository amendmentApplicationDataRepository;

    @Transactional(readOnly = true)
    public MooselikePermitApplicationAreaDTO getPermitArea(final long applicationId) {
        final HarvestPermitArea permitArea = requirePermitArea(applicationId, EntityPermission.READ);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(permitArea.getZone().getId());

        final List<HarvestPermitAreaRhyDTO> rhy = F.mapNonNullsToList(permitArea.getRhy(), HarvestPermitAreaRhyDTO::create);
        final List<HarvestPermitAreaHtaDTO> hta = F.mapNonNullsToList(permitArea.getHta(), HarvestPermitAreaHtaDTO::create);

        return new MooselikePermitApplicationAreaDTO(permitArea.getStatus(), areaSize, permitArea.isFreeHunting(), rhy, hta);
    }

    private HarvestPermitArea requirePermitArea(final long applicationId, final EntityPermission permission) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(applicationId, permission);

        if (application.getHarvestPermitCategory().isAmendment()) {
            final HarvestPermitApplication originalApplication = findOriginalApplication(application);
            originalApplication.assertHasPermitArea();
            return originalApplication.getArea();
        }

        application.assertHasPermitArea();

        return application.getArea();
    }

    private HarvestPermitApplication findOriginalApplication(final HarvestPermitApplication application) {
        final AmendmentApplicationData data = amendmentApplicationDataRepository.getByApplication(application);
        final HarvestPermit originalPermit = data.getOriginalPermit();
        final PermitDecision originalDecision = originalPermit.getPermitDecision();

        return originalDecision.getApplication();
    }

    // PROCESSING STATUS

    @Transactional(readOnly = true)
    public HarvestPermitArea.StatusCode getStatus(final long applicationId) {
        return requirePermitArea(applicationId, EntityPermission.NONE).getStatus();
    }

    @Transactional
    public void setReadyForProcessing(final long applicationId) {
        final HarvestPermitArea harvestPermitArea = requirePermitArea(applicationId, EntityPermission.UPDATE);
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
        zone.setGeom(null);
        zone.setExcludedGeom(null);
        zone.setMetsahallitusHirvi(emptySet());
        zone.setComputedAreaSize(0);
        zone.setWaterAreaSize(0);
        zone.setStateLandAreaSize(null);
        zone.setPrivateLandAreaSize(null);

        harvestPermitAreaRhyRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaHtaRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaMmlRepository.deleteByHarvestPermitArea(harvestPermitArea);
        harvestPermitAreaVerotusLohkoRepository.deleteByHarvestPermitArea(harvestPermitArea);
    }

    // GEOJSON

    @Transactional(readOnly = true)
    public GISBounds getBounds(final long applicationId) {
        return Optional.ofNullable(requirePermitArea(applicationId, EntityPermission.READ))
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .map(zoneId -> gisZoneRepository.getBounds(zoneId, GISUtils.SRID.WGS84))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public FeatureCollection getPermitAreaForEachPartner(final long applicationId) {
        final HarvestPermitArea permitArea = requirePermitArea(applicationId, EntityPermission.READ);

        return harvestPermitApplicationGeoJsonService.getPermitAreaForEachPartner(permitArea);
    }

    @Transactional(readOnly = true)
    public FeatureCollection getPermitAreaCombined(final long applicationId) {
        final HarvestPermitArea permitArea = requirePermitArea(applicationId, EntityPermission.READ);

        return harvestPermitApplicationGeoJsonService.getPermitAreaCombined(permitArea);
    }
}
