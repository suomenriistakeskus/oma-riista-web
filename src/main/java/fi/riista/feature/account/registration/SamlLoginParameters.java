package fi.riista.feature.account.registration;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:configuration/saml.properties")
public class SamlLoginParameters {
    @Value("${saml.debug}")
    private String samlDebug;

    @Value("${saml.sp.entityid}")
    private String spEntityId;

    @Value("${saml.sp.x509cert}")
    private String spCert;

    @Value("${saml.sp.privatekey}")
    private String spPrivateKey;

    @Value("${saml.sp.single_logout_service.url}")
    private String spSlsUrl;

    @Value("${saml.idp.entityid}")
    private String idpEntityId;

    @Value("${saml.idp.x509cert}")
    private String idpCertOld;

    @Value("${saml.idp.x509cert.1}")
    private String idpCertPrimary;

    @Value("${saml.idp.x509cert.2}")
    private String idpCertSecondary;

    @Value("${saml.idp.single_sign_on_service.url}")
    private String idpSsoUrl;

    @Value("${saml.sp.assertion_consumer_service.url}")
    private String spAcsUrl;

    @Value("${saml.idp.single_logout_service.url}")
    private String idpSlsUrl;

    @Value("${saml.security.requested_authncontext}")
    private String requestedAuthContext;

    public Saml2Settings buildSettings() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(SettingsBuilder.DEBUG_PROPERTY_KEY, samlDebug);
        properties.put(SettingsBuilder.STRICT_PROPERTY_KEY, true);
        properties.put(SettingsBuilder.SECURITY_REQUESTED_AUTHNCONTEXT, requestedAuthContext);
        properties.put(SettingsBuilder.SECURITY_REQUESTED_AUTHNCONTEXTCOMPARISON, "exact");
        properties.put(SettingsBuilder.SECURITY_SIGNATURE_ALGORITHM, Constants.RSA_SHA256);
        properties.put(SettingsBuilder.SECURITY_NAMEID_ENCRYPTED, false);
        properties.put(SettingsBuilder.SECURITY_AUTHREQUEST_SIGNED, true);
        properties.put(SettingsBuilder.SECURITY_LOGOUTREQUEST_SIGNED, true);
        properties.put(SettingsBuilder.SECURITY_LOGOUTRESPONSE_SIGNED, true);
        properties.put(SettingsBuilder.SECURITY_WANT_XML_VALIDATION, true);
        properties.put(SettingsBuilder.SECURITY_WANT_MESSAGES_SIGNED, true);
        properties.put(SettingsBuilder.SECURITY_WANT_ASSERTIONS_SIGNED, false);
        properties.put(SettingsBuilder.SECURITY_WANT_ASSERTIONS_ENCRYPTED, true);
        properties.put(SettingsBuilder.SECURITY_WANT_NAMEID_ENCRYPTED, false);
        properties.put(SettingsBuilder.SECURITY_SIGN_METADATA, false);

        properties.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, spEntityId);
        properties.put(SettingsBuilder.SP_X509CERT_PROPERTY_KEY, spCert);
        properties.put(SettingsBuilder.SP_PRIVATEKEY_PROPERTY_KEY, spPrivateKey);
        properties.put(SettingsBuilder.SP_NAMEIDFORMAT_PROPERTY_KEY, Constants.NAMEID_TRANSIENT);

        properties.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, spAcsUrl);
        properties.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_BINDING_PROPERTY_KEY, Constants.BINDING_HTTP_POST);

        properties.put(SettingsBuilder.SP_SINGLE_LOGOUT_SERVICE_URL_PROPERTY_KEY, spSlsUrl);
        properties.put(SettingsBuilder.SP_SINGLE_LOGOUT_SERVICE_BINDING_PROPERTY_KEY, Constants.BINDING_HTTP_REDIRECT);

        if (StringUtils.hasText(idpCertPrimary) && StringUtils.hasText(idpCertSecondary)) {
            properties.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, idpCertPrimary);

            properties.put(SettingsBuilder.IDP_X509CERTMULTI_PROPERTY_KEY + ".0", idpCertPrimary);
            properties.put(SettingsBuilder.IDP_X509CERTMULTI_PROPERTY_KEY + ".1", idpCertSecondary);

        } else if (StringUtils.hasText(idpCertPrimary)) {
            properties.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, idpCertPrimary);

        } else if (StringUtils.hasText(idpCertSecondary)) {
            properties.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, idpCertSecondary);

        } else {
            properties.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, idpCertOld);
        }

        properties.put(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, idpEntityId);
        properties.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, idpSsoUrl);
        properties.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_BINDING_PROPERTY_KEY, Constants.BINDING_HTTP_REDIRECT);
        properties.put(SettingsBuilder.IDP_SINGLE_LOGOUT_SERVICE_URL_PROPERTY_KEY, idpSlsUrl);
        properties.put(SettingsBuilder.IDP_SINGLE_LOGOUT_SERVICE_BINDING_PROPERTY_KEY, Constants.BINDING_HTTP_REDIRECT);

        return new SettingsBuilder().fromValues(properties).build();
    }

}
