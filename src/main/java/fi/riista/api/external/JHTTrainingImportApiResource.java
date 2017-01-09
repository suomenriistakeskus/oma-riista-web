package fi.riista.api.external;

import com.google.common.collect.Iterables;
import fi.riista.config.BatchConfig;
import fi.riista.integration.koulutusportaali.JHTTrainingImportService;
import fi.riista.integration.koulutusportaali.jht.JHT_Suoritukset;
import fi.riista.integration.koulutusportaali.jht.JHT_Suoritus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@RestController
public class JHTTrainingImportApiResource {
    private static final Logger LOG = LoggerFactory.getLogger(JHTTrainingImportApiResource.class);

    @Resource
    private JHTTrainingImportService jhtTrainingImportService;

    @PreAuthorize("hasPrivilege('IMPORT_JHT')")
    @RequestMapping(value = "/api/v1/import/koulutusportaali/jht",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String receive(@RequestBody JHT_Suoritukset suoritukset) {
        final Iterable<List<JHT_Suoritus>> batchList =
                Iterables.partition(suoritukset.getSuoritus(), BatchConfig.BATCH_SIZE);

        int okCount = 0;
        int failureCount = 0;

        for (final List<JHT_Suoritus> batch : batchList) {
            try {
                jhtTrainingImportService.importData(batch);
                okCount += batch.size();

            } catch (Exception ex) {
                for (final JHT_Suoritus suoritus : batch) {
                    try {
                        jhtTrainingImportService.importData(Collections.singletonList(suoritus));
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
        LOG.error("JHT import error", ex);

        return ex.getMessage() != null ? ex.getMessage() : "error";
    }
}
