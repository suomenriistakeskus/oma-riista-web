package fi.riista.feature.account.registration;

import com.google.common.collect.ImmutableMap;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.servlet.ServletUtils;
import com.onelogin.saml2.settings.Saml2Settings;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class SamlLoginHelper {
    private Saml2Settings samlSettings;

    @Resource
    private SamlLoginParameters samlLoginParameters;

    @PostConstruct
    public void initSettings() {
        this.samlSettings = samlLoginParameters.buildSettings();
    }

    @Nonnull
    public String buildSsoRedirectUri(final String relayState,
                                      final SamlLoginLanguage lang) throws Exception {
        final CustomAuthnRequest authnRequest = new CustomAuthnRequest(samlSettings, lang);
        final String samlRequest = authnRequest.getEncodedAuthnRequest();

        final Map<String, String> parameters = buildAuthRequestParameters(samlRequest, relayState);

        return ServletUtils.sendRedirect(null,
                samlSettings.getIdpSingleSignOnServiceUrl().toString(),
                parameters, true);
    }

    @Nonnull
    private Map<String, String> buildAuthRequestParameters(final String samlRequest,
                                                           final String relayState) throws Exception {
        return ImmutableMap.<String, String>builder()
                .put("SAMLRequest", samlRequest)
                .put("SigAlg", samlSettings.getSignatureAlgorithm())
                .put("Signature", createRequestSignature(samlRequest, relayState))
                .put("RelayState", relayState)
                .build();
    }

    @Nonnull
    private String createRequestSignature(final String samlRequest, final String relayState) throws Exception {
        return new Auth(samlSettings, null, null)
                .buildRequestSignature(samlRequest, relayState, samlSettings.getSignatureAlgorithm());
    }

    @Nonnull
    public SamlAuthenticationResult processAuthenticationResponse(
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final String relayState = request.getParameter("RelayState");

        final Auth auth = new Auth(samlSettings, request, response);
        auth.processResponse();

        if (auth.isAuthenticated() && auth.getErrors().isEmpty()) {
            final SamlUserAttributes userAttributes = SamlUserAttributesParser.create(auth).parse();

            return new SamlAuthenticationResult(userAttributes, relayState);
        }

        return new SamlAuthenticationResult(auth.getErrors(), auth.getLastErrorReason(), relayState);
    }
}
