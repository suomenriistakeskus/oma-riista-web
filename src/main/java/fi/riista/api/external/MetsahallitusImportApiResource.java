package fi.riista.api.external;

import com.google.common.collect.Lists;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitErrorCollector;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitImportDTO;
import fi.riista.integration.metsahallitus.permit.MetsahallitusPermitImportFeature;
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

@RestController
@RequestMapping(value = "/api/v1/import/mh")
public class MetsahallitusImportApiResource {

    private static final Logger LOG = LoggerFactory.getLogger(MetsahallitusImportApiResource.class);

    @Resource
    private MetsahallitusPermitImportFeature metsahallitusPermitImportFeature;

    @PreAuthorize("hasPrivilege('IMPORT_METSAHALLITUS_PERMITS')")
    @PostMapping(value = "/permit",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Map<String, Set<String>>> importMhPermits(
            final @RequestBody @Valid List<MetsahallitusPermitImportDTO> permits) {

        final MetsahallitusPermitErrorCollector errorCollector = new MetsahallitusPermitErrorCollector();

        for (final List<MetsahallitusPermitImportDTO> batch : Lists.partition(permits, 1024)) {
            metsahallitusPermitImportFeature.importPermits(batch, errorCollector);
        }

        final Map<String, Set<String>> allErrors = errorCollector.getAllErrors();

        if (allErrors.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyMap());
        }

        LOG.error("Errors occurred while importing Metsahallitus permits");

        if (allErrors.size() < 100) {
            for (final Map.Entry<String, Set<String>> entry : allErrors.entrySet()) {
                LOG.info(entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(allErrors);
    }
}
