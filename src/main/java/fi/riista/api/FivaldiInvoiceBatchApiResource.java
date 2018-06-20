package fi.riista.api;

import fi.riista.feature.permit.invoice.batch.FivaldiBatchDTO;
import fi.riista.feature.permit.invoice.batch.FivaldiInvoiceBatchFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.joda.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping(value = "/api/v1/fivaldi")
public class FivaldiInvoiceBatchApiResource {

    @Resource
    private FivaldiInvoiceBatchFeature feature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/availableyearmonths", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<YearMonth> listAvailableFivaldiBatchMonths() {
        return feature.getAvailableFivaldiBatchMonths();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/batch/new", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<FivaldiBatchDTO> getNewFivaldiBatches() {
        return feature.getNewFivaldiBatches();
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/batch/{year:\\d+}/{month:\\d+}", produces = APPLICATION_JSON_UTF8_VALUE)
    public List<FivaldiBatchDTO> getPreviousFivaldiBatches(final @PathVariable int year,
                                                           final @PathVariable int month) {

        return feature.getPreviousFivaldiBatches(year, month);
    }

    @PostMapping(value = "/batch/{batchId:\\d+}/file", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> getFivaldiBatchFile(final @PathVariable long batchId) throws IOException {
        return feature.getAndMarkFivaldiBatchFileDownloaded(batchId);
    }
}
