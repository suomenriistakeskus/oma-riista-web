package fi.riista.feature.organization.occupation.search;

import fi.riista.config.Constants;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RhyContactSearchExcelView extends AbstractXlsxView {

    private static final String[] HEADERS_FI = {
            "RHY",
            "Sähköpostiosoite",
            "Puhelinnumero",
            "Katuosoite",
            "Postinumero",
            "Kaupunki",
            "Maa"
    };

    private static final String[] HEADERS_SV = {
            "JVF",
            "E-Postadress",
            "Telefonnummer",
            "Gatuadress",
            "Postnummer",
            "Stad",
            "Land"
    };

    private final List<RhyContactSearchResultDTO> results;

    public RhyContactSearchExcelView(List<RhyContactSearchResultDTO> results) {
        this.results = results;
    }

    private static String[] createRow(RhyContactSearchResultDTO result) {
        return new String[] {
                result.getRhyName(),
                result.getEmail(),
                result.getPhoneNumber(),
                result.getStreetAddress(),
                result.getPostalCode(),
                result.getCity(),
                result.getCountry()
        };
    }

    private static String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "rhy-yhteystiedot-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename=" + createFilename());

        Sheet sheet = workbook.createSheet();
        sheet.setDefaultColumnWidth(20);

        createHeaderRow(sheet, LocaleContextHolder.getLocale());

        int row = 2;
        for (RhyContactSearchResultDTO result : results) {
            Row sheetRow = sheet.createRow(row++);

            int col = 0;
            for (String cellValue : createRow(result)) {
                sheetRow.createCell(col++).setCellValue(cellValue);
            }
        }
    }

    private static void createHeaderRow(Sheet sheet, Locale locale) {
        String[] headersNames = Locales.isSwedish(locale) ? HEADERS_SV : HEADERS_FI;

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headersNames.length; i++) {
            headerRow.createCell(i).setCellValue(headersNames[i]);
        }
    }
}
