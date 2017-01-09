package fi.riista.util;

import fi.riista.config.Constants;

import org.springframework.oxm.Marshaller;

import javax.annotation.Nonnull;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class JaxbUtils {

    public static String marshalToString(@Nonnull final Object data, @Nonnull final Marshaller marshaller) {
        Objects.requireNonNull(data, "data must not be null");
        Objects.requireNonNull(marshaller, "marshaller must not be null");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            marshaller.marshal(data, new StreamResult(baos));
            return new String(baos.toByteArray(), Constants.DEFAULT_ENCODING);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JaxbUtils() {
        throw new AssertionError();
    }

}
