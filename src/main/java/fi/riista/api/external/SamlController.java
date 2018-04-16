package fi.riista.api.external;

import fi.riista.feature.account.registration.RegisterAccountFeature;
import fi.riista.feature.account.registration.SamlAuthenticationResult;
import fi.riista.feature.account.registration.SamlLoginHelper;
import fi.riista.feature.account.registration.SamlLoginLanguage;
import fi.riista.feature.account.registration.VetumaTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@RestController
@RequestMapping("/saml")
public class SamlController {
    private static final String FRAGMENT_SUCCESS = "/register/from-sso/success";
    private static final String FRAGMENT_ERROR = "/register/from-sso/error";

    private static final Logger LOG = LoggerFactory.getLogger(SamlController.class);

    private static ResponseEntity<Void> redirectFragment(final String fragment) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(ServletUriComponentsBuilder.fromCurrentContextPath().fragment(fragment).build().toUri())
                .build();
    }

    private static ResponseEntity<Void> redirectToError() {
        return redirectFragment(FRAGMENT_ERROR);
    }

    private static ResponseEntity<Void> redirectToSuccess(final String trid) {
        return redirectFragment(FRAGMENT_SUCCESS + "/" + trid);
    }

    @Resource
    private RegisterAccountFeature registerAccountFeature;

    @Resource
    private SamlLoginHelper samlLoginHelper;

    @PostMapping(value = "login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> login(@RequestParam String trid,
                                      @RequestParam SamlLoginLanguage lang) {
        if (!VetumaTransactionService.isValidTransactionId(trid)) {
            LOG.error("Got invalid TRID={}", trid);
            return redirectToError();
        }

        LOG.info("Attempting login with relayState={}", trid);

        try {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(samlLoginHelper.buildSsoRedirectUri(trid, lang)))
                    .build();

        } catch (Exception e) {
            LOG.error("SAML login redirect failed", e);
        }

        return redirectToError();
    }

    @PostMapping(value = "acs", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> acs(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final SamlAuthenticationResult authResult =
                    samlLoginHelper.processAuthenticationResponse(request, response);

            registerAccountFeature.fromSso(authResult);

            if (authResult.isSuccessful()) {
                return redirectToSuccess(authResult.getRelayState());
            }

        } catch (Exception e) {
            LOG.error("Unknown exception during SAML authentication", e);
        }

        return redirectToError();
    }
}
