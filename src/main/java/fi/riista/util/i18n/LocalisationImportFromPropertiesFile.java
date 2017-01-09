package fi.riista.util.i18n;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LocalisationImportFromPropertiesFile {

    public static void main(String... args) throws Exception {
        //importFile("src/main/resources/i18n/frontend_fi.properties", JsonToMap.FI);
        importFile("src/main/resources/i18n/frontend_sv.properties", JsonToMap.SV);
        importFile("src/main/resources/i18n/frontend_en.properties", JsonToMap.EN);
    }

    private static void importFile(final String input, final String output) throws Exception {
        final Map<String, String> inputMap = readProperties(new File(input));
        final String outputJson = MapToJson.toJson(inputMap, true);

        FileUtils.writeStringToFile(new File(output), outputJson, StandardCharsets.UTF_8);
    }

    private static Map<String, String> readProperties(final File file) throws IOException {
        final LinkedProperties properties = new LinkedProperties();
        try (final InputStream is = new FileInputStream(file)) {
            properties.load(is);
        }

        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (final Object key : properties.orderedKeys()) {
            final Object value = properties.get(key);
            result.put(key.toString(), value != null ? value.toString() : null);
        }
        return result;
    }

    private LocalisationImportFromPropertiesFile() {
        throw new AssertionError();
    }
}
