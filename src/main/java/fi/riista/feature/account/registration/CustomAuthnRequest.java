/*
 Copyright (c) 2010-2016 OneLogin, Inc.

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.

 modified from original source com.onelogin.saml2.authn.AuthnRequest to add support for SAML extensions.
*/
package fi.riista.feature.account.registration;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Util;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomAuthnRequest {
    private static final String ID_PREFIX = "oma-riista-";
    private static final String REQUEST_TEMPLATE = "<samlp:AuthnRequest" +
            " xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"" +
            " xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"" +
            " ID=\"${id}\" Version=\"2.0\"" +
            " IssueInstant=\"${issueInstant}\" ${destinationStr}" +
            " ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\"" +
            " AssertionConsumerServiceURL=\"${assertionConsumerServiceURL}\"><saml:Issuer>${spEntityid}</saml:Issuer>" +
            "${samlExtensionStr}${nameIDPolicyStr}${requestedAuthnContextStr}</samlp:AuthnRequest>";

    private final String authnRequestString;
    private final String id;
    private final Long issueInstant;

    public CustomAuthnRequest(final Saml2Settings settings, final SamlLoginLanguage lang) {
        this.id = ID_PREFIX + UUID.randomUUID().toString();
        issueInstant = System.currentTimeMillis();
        authnRequestString = generateSubstitutor(settings, lang).replace(REQUEST_TEMPLATE);
    }

    public String getEncodedAuthnRequest() throws IOException {
        return Util.deflatedBase64encoded(authnRequestString);
    }

    private StrSubstitutor generateSubstitutor(final Saml2Settings settings,
                                               final SamlLoginLanguage lang) {
        final Map<String, String> valueMap = new HashMap<>();
        valueMap.put("id", String.valueOf(id));
        valueMap.put("issueInstant", Util.formatDateTime(issueInstant));
        valueMap.put("nameIDPolicyStr", "<samlp:NameIDPolicy Format=\"" +
                settings.getSpNameIDFormat() + "\" AllowCreate=\"true\" />");
        valueMap.put("assertionConsumerServiceURL", String.valueOf(settings.getSpAssertionConsumerServiceUrl()));
        valueMap.put("spEntityid", settings.getSpEntityId());

        String destinationStr = "";
        URL sso = settings.getIdpSingleSignOnServiceUrl();
        if (sso != null) {
            destinationStr = " Destination=\"" + sso.toString() + "\"";
        }
        valueMap.put("destinationStr", destinationStr);

        final StringBuilder requestedAuthnContextSb = new StringBuilder();
        final List<String> requestedAuthnContexts = settings.getRequestedAuthnContext();

        if (requestedAuthnContexts != null && !requestedAuthnContexts.isEmpty()) {
            requestedAuthnContextSb
                    .append("<samlp:RequestedAuthnContext Comparison=\"")
                    .append(settings.getRequestedAuthnContextComparison())
                    .append("\">");

            for (final String requestedAuthnContext : requestedAuthnContexts) {
                requestedAuthnContextSb
                        .append("<saml:AuthnContextClassRef>")
                        .append(requestedAuthnContext)
                        .append("</saml:AuthnContextClassRef>");
            }

            requestedAuthnContextSb.append("</samlp:RequestedAuthnContext>");
        }

        valueMap.put("requestedAuthnContextStr", requestedAuthnContextSb.toString());

        String samlExtensionStr = "";
        if (lang != null) {
            samlExtensionStr = "<samlp:Extensions><vetuma:vetuma xmlns:vetuma=\"urn:vetuma:SAML:2.0:extensions\">" +
                    "<LG>" + lang.name() + "</LG></vetuma:vetuma></samlp:Extensions>";
        }
        valueMap.put("samlExtensionStr", samlExtensionStr);

        return new StrSubstitutor(valueMap);
    }

    public String getId() {
        return id;
    }
}
