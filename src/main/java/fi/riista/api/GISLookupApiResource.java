package fi.riista.api;

import com.google.common.collect.Iterables;
import fi.riista.feature.gis.kiinteisto.CoordinatePropertyLookupFeature;
import fi.riista.feature.gis.rhy.CoordinateRhyLookupFeature;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/gis")
public class GISLookupApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(GISLookupApiResource.class);

    @Resource
    private CoordinatePropertyLookupFeature coordinatePropertyLookupFeature;

    @Resource
    private CoordinateRhyLookupFeature coordinateRhyLookupFeature;

    @RequestMapping(value = "/kt", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getKiinteistoTunnusForCoordinates(
            @RequestParam double latitude, @RequestParam double longitude) {

        final GISPoint gisPoint = GISPoint.create(latitude, longitude);
        final List<MMLRekisteriyksikonTietoja> propertyIdentifiers =
                coordinatePropertyLookupFeature.findByPosition(gisPoint);

        if (propertyIdentifiers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NONE");
        } else if (propertyIdentifiers.size() > 1) {
            final String selectedIdentifier = propertyIdentifiers.get(0).getPropertyIdentifier();

            LOG.warn("Found multiple zones for point {}, extracting property identifier {} from the first one",
                    gisPoint, selectedIdentifier);

            return ResponseEntity.ok(selectedIdentifier);
        } else {
            return ResponseEntity.ok(Iterables.getOnlyElement(propertyIdentifiers).getPropertyIdentifier());
        }
    }

    @RequestMapping(value = "/rhy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getRhyForCoordinates(@RequestParam double latitude, @RequestParam double longitude) {
        final GISPoint gisPoint = GISPoint.create(latitude, longitude);
        final OrganisationNameDTO rhyWithName = coordinateRhyLookupFeature.findByGeoLocation(gisPoint);

        if (rhyWithName != null) {
            return ResponseEntity.ok(rhyWithName);
        }
        return ResponseEntity.notFound().build();
    }

}
