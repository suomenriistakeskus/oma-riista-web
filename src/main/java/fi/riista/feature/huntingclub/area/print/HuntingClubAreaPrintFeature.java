package fi.riista.feature.huntingclub.area.print;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.integration.mapexport.MapPdfRemoteService;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Optional;

@Component
public class HuntingClubAreaPrintFeature {

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
    public MapPdfModel getModel(final long id, final Locale locale) {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(id, EntityPermission.READ);

        final long zoneId = Optional.ofNullable(huntingClubArea)
                .map(HuntingClubArea::getZone).map(F::getId)
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final Geometry geometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);

        return new MapPdfModel.Builder(messageSource, locale)
                .withAreaName(huntingClubArea.getNameLocalisation())
                .withClubName(huntingClubArea.getClub().getNameLocalisation())
                .withModificationTime(huntingClubArea.getModificationTime())
                .withSize(huntingClubArea.getZone().getSize())
                .withGeometry(geometry)
                .withBbox(bounds.toBBox())
                .build();
    }

}
