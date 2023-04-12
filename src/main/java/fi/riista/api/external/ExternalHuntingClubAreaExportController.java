package fi.riista.api.external;

import fi.riista.integration.koiratutka.export.ExportRequestDTO;
import fi.riista.integration.koiratutka.export.HuntingClubAreaExportFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
public class ExternalHuntingClubAreaExportController {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalHuntingClubAreaExportController.class);

    @Resource
    private HuntingClubAreaExportFeature huntingClubAreaExportFeature;

    @RequestMapping(value = "/api/v1/export/hunting-area-by-id/{externalId}",
            method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> exportGeoJson(
            final @Valid @ModelAttribute ExportRequestDTO dto,
            final WebRequest request) {

        try {
            return huntingClubAreaExportFeature.export(dto, request, false);

        } catch (Exception ex) {
            LOG.error("Export error", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @RequestMapping(value = "/api/v1/export/hunting-area-with-poi-by-id/{externalId}",
            method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> exportGeoJsonHuntingAreaWithPoi(
            final @Valid @ModelAttribute ExportRequestDTO dto,
            final WebRequest request) {

        try {
            return huntingClubAreaExportFeature.export(dto, request, true);

        } catch (Exception ex) {
            LOG.error("Export with poi error", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
