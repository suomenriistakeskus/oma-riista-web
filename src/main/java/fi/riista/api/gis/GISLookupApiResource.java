package fi.riista.api.gis;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.CoordinateMunicipalityLookupFeature;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.kiinteisto.CoordinatePropertyLookupFeature;
import fi.riista.feature.gis.rhy.CoordinateRhyLookupFeature;
import fi.riista.feature.organization.OrganisationNameDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/gis")
public class GISLookupApiResource {

    @Resource
    private CoordinatePropertyLookupFeature coordinatePropertyLookupFeature;

    @Resource
    private CoordinateRhyLookupFeature coordinateRhyLookupFeature;

    @Resource
    private CoordinateMunicipalityLookupFeature coordinateMunicipalityLookupFeature;

    @RequestMapping(value = "/kt", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getKiinteistoTunnusForCoordinates(
            @RequestParam double latitude, @RequestParam double longitude) {
        return coordinatePropertyLookupFeature.findByPosition(GISPoint.create(latitude, longitude))
                .map(property -> ResponseEntity.ok(property.getPropertyIdentifier()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("NONE"));
    }

    @RequestMapping(value = "/rhy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRhyForCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        final GISPoint gisPoint = GISPoint.create(latitude, longitude);
        final OrganisationNameDTO rhyWithName = coordinateRhyLookupFeature.findByGeoLocation(gisPoint);

        if (rhyWithName != null) {
            return ResponseEntity.ok(rhyWithName);
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/municipality", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMunicipalityForCoordinates(@RequestParam int latitude, @RequestParam int longitude) {
        final OrganisationNameDTO municipality = coordinateMunicipalityLookupFeature.findByGeoLocation(new GeoLocation(latitude, longitude));

        if (municipality != null) {
            return ResponseEntity.ok(municipality);
        }
        return ResponseEntity.notFound().build();
    }

}
