package fi.riista.api.external;

import fi.riista.feature.huntingclub.area.transfer.HuntingClubAreaExportFeature;
import fi.riista.integration.gis.ExternalHuntingClubAreaExportRequest;
import fi.riista.util.MediaTypeExtras;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.annotation.Resource;
import javax.validation.Valid;

import static org.hibernate.jpa.internal.QueryImpl.LOG;

@RestController
public class ExternalHuntingClubAreaExportController {
    @Resource
    private HuntingClubAreaExportFeature huntingClubAreaExportFeature;

    @RequestMapping(value = "/api/v1/export/hunting-area-by-id/{externalId}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaTypeExtras.APPLICATION_GEOJSON_VALUE)
    public ResponseEntity<?> getSeuraAlueGeoJson(
            final @Valid @ModelAttribute ExternalHuntingClubAreaExportRequest body,
            final WebRequest request) {

        try {
            return huntingClubAreaExportFeature.exportCombinedGeoJson(body, request);

        } catch (Exception ex) {
            LOG.error("Export error", ex);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("error");
        }
    }
}
