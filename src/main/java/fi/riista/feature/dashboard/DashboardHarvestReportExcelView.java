package fi.riista.feature.dashboard;

import fi.riista.config.Constants;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelRowValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class DashboardHarvestReportExcelView extends AbstractXlsxView {

    private static final String[] HEADERS_FI = {
            "Alue",
            "Eläin",
            "Lupatyyppi",
            "Lupatyypin tunniste",
            "Kausi",
            "Lupa",
            "Käyttäjien lisäämiä",
            "Ylläpidon lisäämiä",
            "Ilmoituksia yhteensä"
    };

    private final List<DashboardHarvestReportDTO> results;

    public DashboardHarvestReportExcelView(List<DashboardHarvestReportDTO> results) {
        this.results = results;
    }

    private static ExcelRowValue<?>[] createRows(DashboardHarvestReportDTO result) {
        return new ExcelRowValue[] {
                ExcelRowValue.from(result.getRka()),
                ExcelRowValue.from(result.getSpecies()),
                ExcelRowValue.from(result.getPermitType()),
                ExcelRowValue.from(result.getPermitTypeCode()),
                ExcelRowValue.from(result.isSeason()),
                ExcelRowValue.from(result.isPermit()),
                ExcelRowValue.from(result.getUserCount()),
                ExcelRowValue.from(result.getModeratorCount()),
                ExcelRowValue.from(result.getReportsTotal())
        };
    }

    private static String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "saalisilmoitukset-kooste" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename=" + createFilename());

        Sheet sheet = workbook.createSheet();
        sheet.setDefaultColumnWidth(20);

        createHeaderRow(sheet);

        int row = 1;
        for (DashboardHarvestReportDTO result : results) {
            Row sheetRow = sheet.createRow(row++);

            int col = 0;
            for (ExcelRowValue<?> rowValue : createRows(result)) {
                rowValue.setCellValue(sheetRow.createCell(col++));
            }
        }
    }

    private static void createHeaderRow(Sheet sheet) {
        String[] headersNames = HEADERS_FI;

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headersNames.length; i++) {
            headerRow.createCell(i).setCellValue(headersNames[i]);
        }
    }

}
