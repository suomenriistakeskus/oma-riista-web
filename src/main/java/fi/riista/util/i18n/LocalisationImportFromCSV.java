package fi.riista.util.i18n;

import liquibase.util.csv.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LocalisationImportFromCSV {

    public static void main(String... args) throws Exception {
        //importFile("fi.csv", JsonToMap.FI);
        //importFile("sv.csv", JsonToMap.SV);
        importFile("en.csv", JsonToMap.EN);
    }

    private static void importFile(final String input, final String output) throws Exception {
        final Map<String, String> inputMap = readCsv(new File(input));
        final String outputJson = MapToJson.toJson(inputMap, true);

        FileUtils.writeStringToFile(new File(output), outputJson, StandardCharsets.UTF_8);
    }

    private static Map<String, String> readCsv(File file) throws IOException {
        final Map<String, String> map = new LinkedHashMap<>();

        try (final Reader bf = new FileReader(file);
                final CSVReader reader = new CSVReader(bf, ';')) {

            final List<String[]> lines = reader.readAll();

            for (final String[] line : lines) {
                if (line.length != 2) {
                    throw new IllegalArgumentException();
                }
                map.put(line[0], line[1]);
            }
        }
        return map;
    }

    private LocalisationImportFromCSV() {
        throw new AssertionError();
    }
}
