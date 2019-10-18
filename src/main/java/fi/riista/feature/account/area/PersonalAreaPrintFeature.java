package fi.riista.feature.account.area;

import com.vividsolutions.jts.geom.Geometry;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.security.EntityPermission;
import fi.riista.util.GISUtils;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

@Component
public class PersonalAreaPrintFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Transactional(readOnly = true)
    public MapPdfModel getModel(final long id, final MapPdfParameters.Overlay overlayType, final Locale locale) {
        final PersonalArea personalArea = requireEntityService.requirePersonalArea(id, EntityPermission.READ);
        final long zoneId = personalArea.getZone().getId();

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(zoneId);
        final Geometry geometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final Geometry overlayGeometry = overlayType == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(personalArea.getExternalId())
                .withAreaName(LocalisedString.of(personalArea.getName()))
                .withClubName(LocalisedString.of(personalArea.getName()))
                .withModificationTime(personalArea.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(geometry)
                .withOverlayGeometry(overlayGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }

}
