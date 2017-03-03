package fi.riista.util.i18n;

import com.google.common.collect.Sets;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalisationTest {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Set<String> NON_LOCALISED_KEYS = Sets.newHashSet(
            "global.language.fi", "global.language.en", "global.language.sv");

    @Test
    public void testFinnish() throws Exception {
        final LinkedHashMap<String, String> map = JsonToMap.readFileToMap(JsonToMap.FI);
        assertTrue(!map.isEmpty());
    }

    @Test
    public void testSwedish() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.SV));
    }

    @Test
    public void testEnglish() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.EN));
    }

    private static void compare(final LinkedHashMap<String, String> mapA, final LinkedHashMap<String, String> mapB) {
        assertEquals(mapA.size(), mapB.size());

        mapA.forEach((key, value) -> {
            // key in A should be present in B
            assertThat(mapB, hasKey(key));

            // if value in A is not empty, then it shouldn't be empty in B either
            final String bValue = mapB.get(key);
            assertEquals(value.isEmpty(), bValue.isEmpty());
        });
    }

    // Export Excel file containing localisations needed to be done.
    public static void main(final String[] args) {
        try {
            final HSSFWorkbook wb = new HSSFWorkbook();

            final LinkedHashMap<String, String> fiMap = JsonToMap.readFileToMap(JsonToMap.FI);

            processTranslations(wb,
                    fiMap,
                    JsonToMap.readFileToMap(JsonToMap.SV),
                    "Ruotsinnokset",
                    new String[] { "KÄÄNNÖSAVAIN", "SUOMEKSI", "PUUTTUVA RUOTSINNOS" });

            processTranslations(wb,
                    fiMap,
                    JsonToMap.readFileToMap(JsonToMap.EN),
                    "Englanninnokset",
                    new String[] { "KÄÄNNÖSAVAIN", "SUOMEKSI", "PUUTTUVA ENGLANNINNOS" });

            final String filename = String.format("%s-%s.xls", "Lokalisoinnit", DATETIME_PATTERN.print(DateUtil.now()));

            try (final FileOutputStream fos = new FileOutputStream(filename);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                wb.write(bos);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void processTranslations(final HSSFWorkbook wb,
                                            final LinkedHashMap<String, String> finnishTranslations,
                                            final LinkedHashMap<String, String> foreignLangTranslations,
                                            final String tabName,
                                            final String[] headers) {

        final ExcelHelper excelHelper = new ExcelHelper(wb, tabName).appendHeaderRow(headers);

        finnishTranslations.forEach((key, finnishText) -> {
            // Key in Finnish version should also be present in foreign language translations.
            assertThat(foreignLangTranslations, hasKey(key));

            if (!NON_LOCALISED_KEYS.contains(key)) {
                final String foreignLangTranslation = foreignLangTranslations.get(key);

                if (finnishText.equals(foreignLangTranslation)) {
                    excelHelper.appendRow()
                            .appendTextCell(key)
                            .appendTextCell(finnishText)
                            .appendTextCell("");
                }
            }
        });

        excelHelper.autoSizeColumns();
    }

}
