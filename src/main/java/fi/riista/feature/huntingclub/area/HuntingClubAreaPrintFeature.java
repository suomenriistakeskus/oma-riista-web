package fi.riista.feature.huntingclub.area;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.GISBounds;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.integration.mapexport.MapPdfModel;
import fi.riista.integration.mapexport.MapPdfParameters;
import fi.riista.security.EntityPermission;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.locationtech.jts.geom.Geometry;
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
    private GISZoneRepository gisZoneRepository;

    @Transactional(readOnly = true)
    public MapPdfModel getModel(final long id, final MapPdfParameters.Overlay overlayType, final Locale locale) {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(id, EntityPermission.READ);

        final long zoneId = Optional.ofNullable(huntingClubArea)
                .map(HuntingClubArea::getZone).map(F::getId)
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);

        // NOTE: Must retrieve original bounds because inverted geometry bounding box contains all of Finland
        final GISBounds bounds = gisZoneRepository.getBounds(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final GISZoneSizeDTO areaSize = gisZoneRepository.getAreaSize(zoneId);
        final Geometry geometry = gisZoneRepository.getInvertedSimplifiedGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN);
        final Geometry overlayGeometry = overlayType == MapPdfParameters.Overlay.VALTIONMAA ?
                gisZoneRepository.getStateGeometry(zoneId, GISUtils.SRID.ETRS_TM35FIN)
                : null;

        return new MapPdfModel.Builder(locale)
                .withExternalId(huntingClubArea.getExternalId())
                .withAreaName(huntingClubArea.getNameLocalisation())
                .withClubName(huntingClubArea.getClub().getNameLocalisation())
                .withModificationTime(huntingClubArea.getModificationTime())
                .withAreaSize(areaSize)
                .withGeometry(geometry)
                .withOverlayGeometry(overlayGeometry)
                .withBbox(bounds.toBBox())
                .build();
    }

}
