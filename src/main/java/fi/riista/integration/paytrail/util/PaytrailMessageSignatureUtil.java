package fi.riista.integration.paytrail.util;

import org.springframework.http.HttpHeaders;

import java.util.Map;

public interface PaytrailMessageSignatureUtil {

    // For calculating signature for own request
    String calculateSignature(HttpHeaders httpHeaders, byte[] body);

    // Validate response
    boolean validateResponse(HttpHeaders httpHeaders, byte[] body);

    // Validate redirect or callback
    boolean validateCallback(Map<String, String> parameterMap);
}
