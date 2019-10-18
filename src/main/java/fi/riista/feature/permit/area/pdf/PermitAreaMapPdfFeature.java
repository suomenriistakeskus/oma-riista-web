package fi.riista.feature.permit.area.pdf;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Optional;

@Component
public class PermitAreaMapPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(readOnly = true)
    public MapPdfModel getModelForHarvestPermit(final long permitId, final MapPdfParameters.Overlay overlayType, final Locale locale) {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        final HarvestPermitApplication application = Optional.ofNullable(harvestPermit.getPermitDecision())
                .map(PermitDecision::getApplication)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Application for HarvestPermit id=%d is not available", permitId)));

        return getModel(application, overlayType, locale);
    }

    @Transactional(readOnly = true)
    public MapPdfModel getModelForApplication(final long applicationId, final MapPdfParameters.Overlay overlayType, final Locale locale) {
        return getModel(requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ), overlayType, locale);
    }

    private MapPdfModel getModel(final HarvestPermitApplication application, final MapPdfParameters.Overlay overlayType,
                                 final Locale locale) {
        application.assertHasPermitArea();

        final long zoneId = Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone).map(F::getId)
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);

        final HarvestPermitArea harvestPermitArea = application.getArea();

        final Geometry invertedGeometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(zoneId);
        final Geometry overlayGeometry = overlayType == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(harvestPermitArea.getExternalId())
                .withAreaName(getAreaName(application))
                .withClubName(getClubName(application))
                .withModificationTime(harvestPermitArea.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(invertedGeometry)
                .withOverlayGeometry(overlayGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }

    private static LocalisedString getAreaName(final HarvestPermitApplication application) {
        return LocalisedString.of(
                "Hakemusalue " + application.getApplicationNumber(),
                "Ansökningsområde " + application.getApplicationNumber(),
                "Application area " + application.getApplicationNumber());
    }

    private static LocalisedString getClubName(final HarvestPermitApplication application) {
        return LocalisedString.of(application.getPermitHolder().getName());
    }
}
