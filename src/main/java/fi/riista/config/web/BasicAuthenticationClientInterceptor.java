package fi.riista.config.web;

import fi.riista.config.Constants;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class BasicAuthenticationClientInterceptor implements ClientHttpRequestInterceptor {
    public static final String HEADER_NAME = "Authorization";

    private final String authenticationHeaderValue;

    public BasicAuthenticationClientInterceptor(String username, String password) {
        final String authentication = username + ":" + password;
        final byte[] encodedAuthentication = Base64.encodeBase64(authentication.getBytes(Constants.DEFAULT_CHARSET));

        this.authenticationHeaderValue = "Basic " + new String(encodedAuthentication);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add(HEADER_NAME, this.authenticationHeaderValue);

        return execution.execute(request, body);
    }
}
