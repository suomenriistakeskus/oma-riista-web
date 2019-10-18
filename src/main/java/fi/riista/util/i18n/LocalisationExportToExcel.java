package fi.riista.util.i18n;

import com.google.common.collect.Sets;
import fi.riista.config.Constants;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Set;

public class LocalisationExportToExcel {

    private static final Set<String> NON_LOCALISED_KEYS = Sets.newHashSet(
            "global.language.fi", "global.language.en", "global.language.sv",
            "global.geoLocation.plainCoordinatesText", "global.hectares",
            "club.group.mooseDataCardImport.fileDownload",
            "club.hunting.specimenAbbrv.UNKNOWN",
            "club.hunting.specimenAbbrv.UNKNOWN_UNKNOWN",
            "payment.bic",
            "payment.iban");

    public static void main(final String[] args) {
        try {
            export();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void export() throws Exception {
        final HSSFWorkbook wb = new HSSFWorkbook();

        final LinkedHashMap<String, String> fiMap = JsonToMap.readFileToMap(JsonToMap.FI);

        processTranslations(wb,
                fiMap,
                JsonToMap.readFileToMap(JsonToMap.SV),
                "Ruotsinnokset",
                new String[]{"KÄÄNNÖSAVAIN", "SUOMEKSI", "PUUTTUVA RUOTSINNOS"});

        processTranslations(wb,
                fiMap,
                JsonToMap.readFileToMap(JsonToMap.EN),
                "Englanninnokset",
                new String[]{"KÄÄNNÖSAVAIN", "SUOMEKSI", "PUUTTUVA ENGLANNINNOS"});

        final String filename = String.format("%s-%s.xls", "Lokalisoinnit", Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));

        try (final FileOutputStream fos = new FileOutputStream(filename);
             final BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            wb.write(bos);
        }
    }

    private static void processTranslations(final HSSFWorkbook wb,
                                            final LinkedHashMap<String, String> finnishTranslations,
                                            final LinkedHashMap<String, String> foreignLangTranslations,
                                            final String tabName,
                                            final String[] headers) {

        final ExcelHelper excelHelper = new ExcelHelper(wb, tabName).appendHeaderRow(headers);

        finnishTranslations.forEach((key, finnishText) -> {
            if (!NON_LOCALISED_KEYS.contains(key)) {
                final String foreignLangTranslation = foreignLangTranslations.get(key);

                if (finnishText.equals(foreignLangTranslation)) {
                    excelHelper.appendRow()
                            .appendTextCell(key)
                            .appendTextCell(htmlEscapeToUnicode(finnishText))
                            .appendTextCell("");
                }
            }
        });
    }

    private static String htmlEscapeToUnicode(final String text) {
        return text.replaceAll("&#8209;", "\u2011");
    }
}
