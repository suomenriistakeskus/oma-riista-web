package fi.riista.integration.garmin;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.MissingHuntingClubAreaGeometryException;
import fi.riista.security.EntityPermission;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.F;
import fi.riista.util.GISUtils;
import org.geojson.FeatureCollection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class GarminAreaExportFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private CGPSMapperAdapter cgpsMapperAdapter;

    @Resource
    private GISZoneRepository zoneRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> exportClubArea(@PathVariable long id) {
        final HuntingClubArea huntingClubArea = requireEntityService.requireHuntingClubArea(id, EntityPermission.READ);
        final FeatureCollection featureCollection = huntingClubArea.getZoneIdSet()
                .map(zoneIds -> zoneRepository.getCombinedFeatures(zoneIds, GISUtils.SRID.WGS84))
                .orElseThrow(MissingHuntingClubAreaGeometryException::new);
        final String filename = "omariista-" + huntingClubArea.getExternalId() + ".img";

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(ContentDispositionUtil.header(filename))
                .body(cgpsMapperAdapter.exportToFile(featureCollection, id, huntingClubArea.getNameFinnish()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> exportPersonalArea(@PathVariable long id) {
        final PersonalArea personalArea = requireEntityService.requirePersonalArea(id, EntityPermission.READ);
        final Set<Long> zoneIds = F.getUniqueIds(personalArea.getZone());
        final FeatureCollection featureCollection = zoneRepository.getCombinedFeatures(zoneIds, GISUtils.SRID.WGS84);
        final String filename = "omariista-" + personalArea.getExternalId() + ".img";

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(ContentDispositionUtil.header(filename))
                .body(cgpsMapperAdapter.exportToFile(featureCollection, id, personalArea.getName()));
    }

}
