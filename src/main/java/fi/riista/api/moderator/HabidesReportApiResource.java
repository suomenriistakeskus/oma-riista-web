package fi.riista.api.moderator;

import fi.riista.integration.habides.export.derogations.HabidesExportFeature;
import fi.riista.util.ContentDispositionUtil;
import org.joda.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/habides", produces = MediaType.APPLICATION_XML_VALUE)
public class HabidesReportApiResource {

    @Resource
    private HabidesExportFeature habidesExportFeature;

    @PostMapping(value = "/birds/export")
    public ResponseEntity<?> exportBirdReport(@RequestBody @Validated final HabidesReportRequestDTO dto) {

        final String report = habidesExportFeature.exportReportForBirdsAsXml(
                new LocalDate(dto.getYear(), 1, 1),
                new LocalDate(dto.getYear(), 12, 31),
                dto.getSpeciesCode());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(report.length())
                .headers(ContentDispositionUtil.header(dto.getFilename()))
                .body(report);

    }

}
