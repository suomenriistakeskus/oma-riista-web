package fi.riista.integration.paytrail.rest.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class PaytrailApiVersionHeaderInterceptor implements ClientHttpRequestInterceptor {
    private static final String HEADER_NAME = "X-Verkkomaksut-Api-Version";
    private static final String HEADER_VALUE = "1";

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
                                        final ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add(HEADER_NAME, HEADER_VALUE);

        return execution.execute(request, body);
    }
}
