package fi.riista.feature.permit.application.pdf;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Optional;

@Component
public class HarvestPermitApplicationMapPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private MapPdfRemoteService mapPdfService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    public byte[] renderPdf(final MapPdfParameters parameters, final MapPdfModel model) {
        return mapPdfService.renderPdf(parameters, model.toFeatureCollection());
    }

    @Transactional(readOnly = true)
    public MapPdfModel getModel(final long applicationId, final Locale locale) {
        final HarvestPermitApplication application = requireEntityService
                .requireHarvestPermitApplication(applicationId, EntityPermission.READ);
        application.assertHasPermitArea();

        final long zoneId = Optional.ofNullable(application.getArea())
                .map(HarvestPermitArea::getZone).map(F::getId)
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);

        final HarvestPermitArea harvestPermitArea = application.getArea();

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final Geometry geometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);

        return new MapPdfModel.Builder(messageSource, locale)
                .withAreaName(getAreaName(application.getApplicationName()))
                .withClubName(getClubName(application))
                .withModificationTime(harvestPermitArea.getModificationTime())
                .withSize(harvestPermitArea.getZone().getSize())
                .withGeometry(geometry)
                .withBbox(bounds.toBBox())
                .build();
    }

    private static LocalisedString getAreaName(final String applicationName) {
        return LocalisedString.of(applicationName);
    }

    private static LocalisedString getClubName(final HarvestPermitApplication application) {
        return application.getPermitHolder() != null
                ? application.getPermitHolder().getNameLocalisation()
                : LocalisedString.of(application.getContactPerson().getFullName());
    }
}
