package fi.riista.api.external;

import com.google.common.collect.Iterables;
import fi.riista.config.BatchConfig;
import fi.riista.integration.koulutusportaali.OtherTrainingImportService;
import fi.riista.integration.koulutusportaali.other.OTH_Suoritukset;
import fi.riista.integration.koulutusportaali.other.OTH_Suoritus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@RestController
public class OtherTrainingImportApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(OtherTrainingImportApiResource.class);

    @Resource
    private OtherTrainingImportService importService;

    @PreAuthorize("hasPrivilege('IMPORT_OTHER_TRAINING')")
    @PostMapping(value = "/api/v1/import/koulutusportaali/other",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String receive(@RequestBody OTH_Suoritukset suoritukset) {
        final Iterable<List<OTH_Suoritus>> batchList =
                Iterables.partition(suoritukset.getSuoritus(), BatchConfig.BATCH_SIZE);

        int okCount = 0;
        int failureCount = 0;

        for (final List<OTH_Suoritus> batch : batchList) {

            try {
                importService.importData(batch);
                okCount += batch.size();
            } catch (Exception ex) {
                for (final OTH_Suoritus suoritus : batch) {
                    try {
                        importService.importData(Collections.singletonList(suoritus));
                        okCount++;

                    } catch (Exception e) {
                        LOG.error("Could not process element with externalId=" + suoritus.getId(), e);
                        failureCount++;
                    }
                }
            }
        }

        return "ok=" + okCount + " failed=" + failureCount;
    }

    @ExceptionHandler(Exception.class)
    public String handleAllErrors(Exception ex) {
        LOG.error("Occupation training import error", ex);

        return ex.getMessage() != null ? ex.getMessage() : "error";
    }
}
