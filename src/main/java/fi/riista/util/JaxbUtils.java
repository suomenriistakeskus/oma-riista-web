package fi.riista.util;

import com.google.common.collect.ImmutableMap;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import fi.riista.config.Constants;
import org.springframework.oxm.Marshaller;

import javax.annotation.Nonnull;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static javax.xml.bind.Marshaller.JAXB_ENCODING;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.bind.Marshaller.JAXB_FRAGMENT;
import static javax.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION;

public final class JaxbUtils {

    private static final NamespacePrefixMapper NAMESPACE_PREFIX_MAPPER = new NamespacePrefixMapper() {
        @Override
        public String getPreferredPrefix(final String namespaceUri,
                                         final String suggestion,
                                         final boolean requirePrefix) {

            return suggestion;
        }

        @Override
        public String[] getPreDeclaredNamespaceUris2() {
            return new String[] {
                    "xsd", "http://www.w3.org/2001/XMLSchema",
                    "xsi", "http://www.w3.org/2001/XMLSchema-instance"
            };
        }
    };

    public static String marshalToString(@Nonnull final Object object, @Nonnull final Marshaller marshaller) {
        Objects.requireNonNull(object, "object is null");
        Objects.requireNonNull(marshaller, "marshaller is null");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            marshaller.marshal(object, new StreamResult(baos));
            return new String(baos.toByteArray(), Constants.DEFAULT_ENCODING);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> getDefaultMarshallerProperties(final boolean includeNamespacePrefixMapper) {
        final String encoding = Constants.DEFAULT_ENCODING;
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object> builder()
                .put(JAXB_ENCODING, encoding)
                .put(JAXB_FRAGMENT, true)
                .put(JAXB_FORMATTED_OUTPUT, true)
                .put("com.sun.xml.bind.xmlHeaders", String.format("<?xml version=\"1.0\" encoding=\"%s\"?>\n", encoding));

        if (includeNamespacePrefixMapper) {
            builder.put("com.sun.xml.bind.namespacePrefixMapper", NAMESPACE_PREFIX_MAPPER);
        }

        return builder.build();
    }

    public static Map<String, Object> getHabidesMarshallerProperties() {
        final String encoding = Constants.DEFAULT_ENCODING;
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object> builder()
                .put(JAXB_ENCODING, encoding)
                .put(JAXB_FRAGMENT, true)
                .put(JAXB_FORMATTED_OUTPUT, true)
                .put(JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://dd.eionet.europa.eu/schemas/habides-2.0/derogations.xsd")
                .put("com.sun.xml.bind.xmlHeaders", String.format("<?xml version=\"1.0\" encoding=\"%s\"?>\n", encoding));

        return builder.build();
    }

    private JaxbUtils() {
        throw new AssertionError();
    }
}
