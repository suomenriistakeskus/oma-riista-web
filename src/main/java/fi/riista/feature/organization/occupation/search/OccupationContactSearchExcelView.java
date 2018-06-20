package fi.riista.feature.organization.occupation.search;

import fi.riista.config.Constants;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelRowValue;
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

public class OccupationContactSearchExcelView extends AbstractXlsxView {

    private static final String[] HEADERS_FI = {
            "Yläorganisaatio",
            "Organisaatio",
            "Tehtävä",
            "Rekisteröitynyt",
            "Sukunimi",
            "Etunimi",
            "Sähköpostiosoite",
            "Puhelinnumero",
            "Katuosoite",
            "Postinumero",
            "Kaupunki",
            "Maa"
    };

    private static final String[] HEADERS_SV = {
            "Huvudorganisation",
            "Organisation",
            "Uppdrag",
            "Registrerad",
            "Efternamn",
            "Förnamn",
            "E-Postadress",
            "Telefonnummer",
            "Gatuadress",
            "Postnummer",
            "Stad",
            "Land"
    };

    private final List<OccupationContactSearchResultDTO> results;

    public OccupationContactSearchExcelView(List<OccupationContactSearchResultDTO> results) {
        this.results = results;
    }

    private static ExcelRowValue<?>[] createRows(OccupationContactSearchResultDTO result) {
        return new ExcelRowValue[]{
                ExcelRowValue.from(result.getParentOrganisationName()),
                ExcelRowValue.from(result.getOrganisationName()),
                ExcelRowValue.from(result.getOccupationName()),
                ExcelRowValue.from(result.isRegistered()),
                ExcelRowValue.from(result.getLastName()),
                ExcelRowValue.from(result.getFirstName()),
                ExcelRowValue.from(result.getEmail()),
                ExcelRowValue.from(result.getPhoneNumber()),
                ExcelRowValue.from(result.getStreetAddress()),
                ExcelRowValue.from(result.getPostalCode()),
                ExcelRowValue.from(result.getCity()),
                ExcelRowValue.from(result.getCountry())
        };
    }

    private static String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "yhteystiedot-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename=" + createFilename());

        final Sheet sheet = workbook.createSheet();
        sheet.setDefaultColumnWidth(20);

        createHeaderRow(sheet, LocaleContextHolder.getLocale());

        int row = 2;
        for (OccupationContactSearchResultDTO result : results) {
            final Row sheetRow = sheet.createRow(row++);

            int col = 0;
            for (ExcelRowValue<?> rowValue : createRows(result)) {
                rowValue.setCellValue(sheetRow.createCell(col++));
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
