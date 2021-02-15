package fi.riista.api.moderator;

import fi.riista.integration.habides.export.derogations.DerogationNotFoundException;
import fi.riista.integration.habides.export.derogations.DraftDecisionsExistException;
import fi.riista.integration.habides.export.derogations.HabidesErrorDTO;
import fi.riista.integration.habides.export.derogations.HabidesExportFeature;
import fi.riista.util.ContentDispositionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.integration.habides.export.derogations.HabidesErrorDTO.HabidesErrorCategory.DRAFT_DECISIONS;
import static fi.riista.integration.habides.export.derogations.HabidesErrorDTO.HabidesErrorCategory.NOT_FOUND;

@RestController
@RequestMapping(value = "/api/v1/habides")
public class HabidesReportApiResource {

    @Resource
    private HabidesExportFeature habidesExportFeature;

    @PostMapping(value = "/export")
    public ResponseEntity<?> exportReport(@RequestBody @Validated final HabidesReportRequestDTO dto) {
        String report;
        try {
            report = habidesExportFeature.exportReportAsXml(
                    dto.getYear(),
                    dto.getSpeciesCode());
        } catch (DraftDecisionsExistException e) {
            final List<String> draftDecisions = e.getDraftDecisions();
            final HabidesErrorDTO errorDTO = new HabidesErrorDTO(DRAFT_DECISIONS, draftDecisions);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorDTO);
        } catch (DerogationNotFoundException e) {
            final HabidesErrorDTO errorDTO = new HabidesErrorDTO(NOT_FOUND, "No derogations found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorDTO);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .headers(ContentDispositionUtil.header(dto.getFilename()))
                .body(report);
    }
}
