package fi.riista.feature.metrics;

import fi.riista.feature.metrics.AdminRhyEditMetricsDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelRowValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class AdminRhyEditMetricsExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String[] HEADERS_FI = {
            "Alue koodi",
            "RHY koodi",
            "RHY nimi",
            "Tehtävät toiminnanohjaajat",
            "Tehtävät moderaattorit",
            "Tapahtumat toiminnanohjaajat",
            "Tapahtumat moderaattorit"
    };

    private final List<AdminRhyEditMetricsDTO> results;

    public AdminRhyEditMetricsExcelView(List<AdminRhyEditMetricsDTO> results) {
        this.results = results;
    }

    private static ExcelRowValue<?>[] createRows(AdminRhyEditMetricsDTO result) {
        return new ExcelRowValue[] {
                ExcelRowValue.from(result.areaCode),
                ExcelRowValue.from(result.rhyCode),
                ExcelRowValue.from(result.rhyName),
                ExcelRowValue.from(result.occupations.coordinator.sum()),
                ExcelRowValue.from(result.occupations.moderator.sum()),
                ExcelRowValue.from(result.events.coordinator.sum()),
                ExcelRowValue.from(result.events.moderator.sum())
        };
    }

    private static String createFilename() {
        final String timestamp = DATETIME_PATTERN.print(DateUtil.now());
        return "rhy-tietojen-muokkaukset-" + timestamp + ".xls";
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
        for (AdminRhyEditMetricsDTO result : results) {
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
