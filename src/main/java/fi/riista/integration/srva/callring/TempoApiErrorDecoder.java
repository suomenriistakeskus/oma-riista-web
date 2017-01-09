package fi.riista.integration.srva.callring;

import com.nsftele.tempo.model.Errors;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;

import java.io.IOException;

class TempoApiErrorDecoder implements ErrorDecoder {
    final Decoder decoder;
    final ErrorDecoder defaultDecoder = new Default();

    TempoApiErrorDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public Exception decode(final String methodKey, final Response response) {
        try {
            final Errors errors = (Errors) decoder.decode(response, Errors.class);

            if (errors != null) {
                return new TempoApiException(response.status(), errors);
            }
        } catch (IOException ignore) {
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
