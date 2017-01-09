package fi.riista.util.i18n;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import fi.riista.config.Constants;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class MapToJson {

    public static String toJson(final Map<String, String> map,
                                final boolean prettyPrint) throws IOException {
        final JsonFactory jfactory = new JsonFactory();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final JsonGenerator generator = jfactory.createGenerator(out)) {
            createJsonWriter(prettyPrint).writeValue(generator, convertMap(map));
        }

        return out.toString(Constants.DEFAULT_ENCODING);
    }

    @Nonnull
    private static ObjectWriter createJsonWriter(final boolean prettyPrint) {
        final ObjectMapper mapper = new ObjectMapper();

        if (!prettyPrint) {
            return mapper.writer();
        }

        final DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        final DefaultPrettyPrinter printer = new DefaultPrettyPrinter("");
        printer.withoutSpacesInObjectEntries();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return mapper.writer(printer);
    }

    private static Map<Object, Object> convertMap(final Map<String, String> map) {
        final Map<Object, Object> out = new LinkedHashMap<>();
        map.forEach((key, value) -> addEntries(out, split(key), value));
        return out;
    }

    private static List<String> split(String key) {
        if (key.contains(".")) {
            return Arrays.asList(key.split("\\."));
        }
        return singletonList(key);
    }

    private static void addEntries(final Map<Object, Object> out,
                                   final List<String> keys,
                                   final String value) {
        if (keys.size() == 1) {
            out.put(keys.get(0), value);
            return;
        }

        final String key = keys.get(0);
        if (!out.containsKey(key)) {
            out.put(key, new LinkedHashMap<>());
        }
        @SuppressWarnings("unchecked")
        final Map<Object, Object> o = (Map<Object, Object>) out.get(key);
        addEntries(o, keys.subList(1, keys.size()), value);
    }

    private MapToJson() {
        throw new AssertionError();
    }
}
