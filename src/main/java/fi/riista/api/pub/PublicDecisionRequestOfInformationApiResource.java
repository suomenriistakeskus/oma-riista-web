package fi.riista.api.pub;

import fi.riista.feature.permit.decision.informationrequest.PermitDecisionInformationRequestFeature;
import fi.riista.util.MediaTypeExtras;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping(value = PublicDecisionRequestOfInformationApiResource.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicDecisionRequestOfInformationApiResource {

    public static final String API_PREFIX = "/api/v1/anon/decision_inforequest";

    @Resource
    private PermitDecisionInformationRequestFeature permitDecisionInformationRequestFeature;


    @CacheControl(policy = CachePolicy.NO_CACHE)
    @GetMapping(value = "/{linkKey}/{documentNumber}", produces = MediaTypeExtras.APPLICATION_ZIP_VALUE)
    public ResponseEntity<byte[]> getRequestOfInformationZip(@PathVariable final String linkKey, @PathVariable final String documentNumber, final HttpServletResponse response,
                                                         final Locale locale) throws IOException {

        return permitDecisionInformationRequestFeature.downloadPublicCarnivoreDecisionThroughInformationRequestNoAuthentication(response,linkKey, documentNumber,locale);
    }

}

