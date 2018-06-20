package fi.riista.api.external;

import fi.riista.integration.metsahallitus.MhPermitImportDTO;
import fi.riista.integration.metsahallitus.MhPermitImportRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PreAuthorize("hasPrivilege('IMPORT_METSAHALLITUS_PERMITS')")
@RestController
@RequestMapping(value = "/api/v1/import/mh")
public class MhPermitImportApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MhPermitImportApiResource.class);

    @Resource
    private MhPermitImportRunner importRunner;

    @PostMapping(value = "permit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Set<String>>> importMhPermits(
            final @RequestBody @Valid List<MhPermitImportDTO> permits) {

        if (permits.size() > 0) {
            final Map<String, Set<String>> allErrors = importRunner.importMhPermits(permits);
            if (!allErrors.isEmpty()) {
                LOG.info("MH permit import errors:");
                LOG.info(allErrors.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(allErrors);
            }
        }
        return ResponseEntity.ok(Collections.emptyMap());
    }
}
