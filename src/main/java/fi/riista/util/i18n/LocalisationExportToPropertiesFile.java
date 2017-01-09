package fi.riista.util.i18n;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Properties;

public final class LocalisationExportToPropertiesFile {

    public static void main(String... args) throws Exception {
        export(JsonToMap.FI, "src/main/resources/i18n/frontend_fi.properties");
        export(JsonToMap.SV, "src/main/resources/i18n/frontend_sv.properties");
        export(JsonToMap.EN, "src/main/resources/i18n/frontend_en.properties");
    }

    private static void export(final String json, final String outFileName) throws Exception {
        final LinkedHashMap<String, String> inputMap = JsonToMap.readFileToMap(json);
        final Properties properties = new LinkedProperties();
        properties.putAll(inputMap);

        try (final OutputStream fos = new FileOutputStream(new File(outFileName))) {
            properties.store(fos, null);
        }
    }

    private LocalisationExportToPropertiesFile() {
        throw new AssertionError();
    }
}
