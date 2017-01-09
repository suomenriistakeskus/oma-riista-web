package fi.riista.util.i18n;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public final class LocalisationExportToCSV {

    private static final char CSV_STR_ESCAPE = '"';
    private static final String CSV_NEWLINE = "\r\n";
    private static final char CSV_FIELD_SEPARATOR = ';';

    public static void main(String... args) throws Exception {
        export(JsonToMap.FI, "fi.csv");
        export(JsonToMap.SV, "sv.csv");
        export(JsonToMap.EN, "en.csv");
    }

    private static void export(String json, String outFileName) throws Exception {
        LinkedHashMap<String, String> map = JsonToMap.readFileToMap(json);
        String result = export(map);
        File file = new File(outFileName);
        FileUtils.writeStringToFile(file, result, StandardCharsets.UTF_8);
    }

    public static String export(LinkedHashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> output(sb, key, value));
        return sb.toString();
    }

    private static void output(StringBuilder sb, String key, String value) {
        sb.append(CSV_STR_ESCAPE);
        sb.append(key);
        sb.append(CSV_STR_ESCAPE);

        sb.append(CSV_FIELD_SEPARATOR);

        sb.append(CSV_STR_ESCAPE);
        sb.append(value);
        sb.append(CSV_STR_ESCAPE);

        sb.append(CSV_NEWLINE);
    }

    private LocalisationExportToCSV() {
        throw new AssertionError();
    }
}
