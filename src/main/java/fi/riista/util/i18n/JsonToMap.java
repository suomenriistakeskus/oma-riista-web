package fi.riista.util.i18n;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Create map of JSON localisation file, where JSON property name will be the key,
 * and nested object names will be joined with a dot '.'.
 * </p>
 * Example:
 * If localisation JSON file contains object:
 * <pre>
 *     {"key1":"val1", "key2":{"key3":"val2"}}
 * </pre>
 * Then returned map will be:
 * <pre>
 *     {"key1"=>"val1", "key2.key3"=>"val2"}
 * </pre>
 */
public final class JsonToMap {

    public static final String FI = "frontend/app/assets/i18n/fi.json";
    public static final String SV = "frontend/app/assets/i18n/sv.json";
    public static final String EN = "frontend/app/assets/i18n/en.json";

    private final JsonParser parser;

    public static LinkedHashMap<String, String> readFileToMap(final String jsonFilePath) throws Exception {
        final JsonFactory jfactory = new JsonFactory();

        try (final JsonParser p = jfactory.createParser(new File(jsonFilePath))) {
            return new JsonToMap(p).toMap();
        }
    }

    public JsonToMap(JsonParser p) {
        this.parser = p;
    }

    private LinkedHashMap<String, String> toMap() throws Exception {
        final LinkedHashMap<String, String> map = new LinkedHashMap<>();
        doExport(null, map);
        return map;
    }

    private void doExport(final String path, final Map<String, String> map) throws Exception {
        while (parser.nextToken() != JsonToken.END_OBJECT && parser.hasCurrentToken()) {
            final String currentName = parser.getCurrentName();
            assertJsonKey(currentName);
            if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                doExport(createPath(path, currentName), map);
            } else if (parser.getCurrentToken() != JsonToken.FIELD_NAME) {
                map.put(createPath(path, currentName), parser.getValueAsString());
            }
        }
    }

    private static void assertJsonKey(final String key) {
        if (key != null && key.contains(".")) {
            throw new IllegalArgumentException(String.format("JSON key contains dot, key:'%s'", key));
        }
    }

    private static String createPath(final String path, final String currentName) {
        if (currentName == null) {
            return "";
        }
        return StringUtils.isEmpty(path) ? currentName : path + "." + currentName;
    }
}
