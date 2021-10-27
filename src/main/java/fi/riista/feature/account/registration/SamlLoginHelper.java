package fi.riista.feature.account.registration;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.servlet.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Component
public class SamlLoginHelper {
    private static Logger LOG = LoggerFactory.getLogger(SamlLoginHelper.class);

    // List of supported settings. Outbound requests are always sent using the primary settings.
    // When processing received requests, fall back to secondary settings is tried before failing the request.
    private List<Saml2Settings> samlSettings;

    @Resource
    private SamlLoginParameters samlLoginParameters;

    @PostConstruct
    public void initSettings() {
        this.samlSettings = samlLoginParameters.buildSettings();
        Preconditions.checkState(!samlSettings.isEmpty());
        LOG.info("Initialized saml login parameters with {} certificates", samlSettings.size());
    }

    @Nonnull
    public String buildSsoRedirectUri(final String relayState,
                                      final SamlLoginLanguage lang) throws Exception {
        final Saml2Settings settings = getPrimarySettings();
        final CustomAuthnRequest authnRequest = new CustomAuthnRequest(settings, lang);
        final String samlRequest = authnRequest.getEncodedAuthnRequest();

        final Map<String, String> parameters = buildAuthRequestParameters(settings, samlRequest, relayState);

        return ServletUtils.sendRedirect(null,
                settings.getIdpSingleSignOnServiceUrl().toString(),
                parameters, true);
    }

    @Nonnull
    private Map<String, String> buildAuthRequestParameters(final Saml2Settings settings,
                                                           final String samlRequest,
                                                           final String relayState) throws Exception {
        return ImmutableMap.<String, String>builder()
                .put("SAMLRequest", samlRequest)
                .put("SigAlg", settings.getSignatureAlgorithm())
                .put("Signature", createRequestSignature(samlRequest, relayState))
                .put("RelayState", relayState)
                .build();
    }

    @Nonnull
    private String createRequestSignature(final String samlRequest, final String relayState) throws Exception {
        final Saml2Settings settings = getPrimarySettings();
        return new Auth(settings, null, null)
                .buildRequestSignature(samlRequest, relayState, settings.getSignatureAlgorithm());
    }

    private Saml2Settings getPrimarySettings() {
        Preconditions.checkState(!samlSettings.isEmpty(), "Saml settings not configured.");
        return samlSettings.get(0);
    }

    @Nonnull
    public SamlAuthenticationResult processAuthenticationResponse(
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        try {
            final Saml2Settings settings = getPrimarySettings();
            return doProcessAuthenticationResponse(settings, request, response);
        } catch (Exception e) {
            if (samlSettings.size() > 1) {
                LOG.info("Trying secondary certificate for authentication response");
                return doProcessAuthenticationResponse(samlSettings.get(1), request, response);
            } else {
                LOG.warn("Processing authentication response failed. Secondary certificate not configured.");
                throw e;
            }
        }

    }

    private SamlAuthenticationResult doProcessAuthenticationResponse(final Saml2Settings settings,
                                                                     final HttpServletRequest request,
                                                                     final HttpServletResponse response) throws Exception {
        final String relayState = request.getParameter("RelayState");

        final Auth auth = new Auth(settings, request, response);
        auth.processResponse();

        if (auth.isAuthenticated() && auth.getErrors().isEmpty()) {
            final SamlUserAttributes userAttributes = SamlUserAttributesParser.create(auth).parse();

            return new SamlAuthenticationResult(userAttributes, relayState);
        }

        return new SamlAuthenticationResult(auth.getErrors(), auth.getLastErrorReason(), relayState);
    }

    public List<String> processLogoutRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            final Saml2Settings settings = getPrimarySettings();
            return doProcessLogoutRequest(settings, request, response);
        } catch (Exception e) {
            if (samlSettings.size() > 1) {
                LOG.info("Trying secondary certificate for logout request");
                return doProcessLogoutRequest(samlSettings.get(1), request, response);
            } else {
                LOG.warn("Processing logout request failed. Secondary certificate not configured.");
                throw e;
            }
        }
    }

    private List<String> doProcessLogoutRequest(final Saml2Settings settings,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response) throws Exception {
        final Auth auth = new Auth(settings, request, response);
        auth.processSLO();
        return auth.getErrors();
    }
}
