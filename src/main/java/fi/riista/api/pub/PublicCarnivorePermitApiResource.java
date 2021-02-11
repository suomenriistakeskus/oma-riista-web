package fi.riista.api.pub;

import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionDownloadFeature;
import fi.riista.feature.pub.permit.PublicCarnivorePermitFeature;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(value = PublicCarnivorePermitApiResource.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicCarnivorePermitApiResource {

    public static final String API_PREFIX = "/api/v1/anon/carnivore";

    @Resource
    private PublicCarnivorePermitFeature publicCarnivorePermitFeature;

    @Resource
    private PermitDecisionRevisionDownloadFeature downloadFeature;

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping
    public Slice<PublicCarnivorePermitDTO> getPage(
            @RequestParam(required = false) final String permitNumber,
            @RequestParam(required = false) final Integer speciesCode,
            @RequestParam(required = false) final Integer calendarYear,
            @RequestParam(required = false) final String rkaCode,
            final Pageable pageRequest) {

        return publicCarnivorePermitFeature.getPageNoAuthorization(
                permitNumber, speciesCode, calendarYear, rkaCode, pageRequest);
    }

    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/decision/{decisionNumber}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public void getDecision(@PathVariable final String decisionNumber, final HttpServletResponse response,
                            final Locale locale) {
        ofNullable(downloadFeature.downloadPublicCarnivoreDecisionNoAuthentication(response, decisionNumber, locale))
                .ifPresent(downloadFeature::decisionDownloaded); // Async insert for download counters
    }

}

