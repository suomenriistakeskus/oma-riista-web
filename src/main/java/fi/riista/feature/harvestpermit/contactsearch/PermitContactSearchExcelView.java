package fi.riista.feature.harvestpermit.contactsearch;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class PermitContactSearchExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "PermitContactSearchExcel.";

    private final List<PermitContactSearchResultDTO> results;
    private final EnumLocaliser localiser;

    public PermitContactSearchExcelView(final List<PermitContactSearchResultDTO> results,
                                        final EnumLocaliser localiser) {
        this.results = results;
        this.localiser = localiser;
    }

    private static String createFilename() {
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());
        return "lupayhteystiedot-" + timestamp + ".xlsx";
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook);

        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, new String[] {
                "permitType",
                "huntingYear",
                "rka",
                "rhy",
                "name",
                "email"
        }));

        results.forEach(result -> {
            helper.appendRow()
                    .appendTextCell(localiser.getTranslation("HarvestPermitApplicationStatisticsExcel." + result.getHarvestPermitCategory()))
                    .appendNumberCell(result.getHuntingYear())
                    .appendTextCell(localiser.getTranslation(result.getRka()))
                    .appendTextCell(localiser.getTranslation(result.getRhy()))
                    .appendTextCell(result.getName())
                    .appendTextCell(result.getEmail());
        });

        helper.autoSizeColumns();
    }
}
