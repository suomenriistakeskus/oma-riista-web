package fi.riista.feature.permit.application.conflict;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.NumberUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HarvestPermitApplicationConflictExcelView extends AbstractXlsxView {
    private static final String COLUMN_HEADER_PREFIX = "HarvestPermitApplicationConflictExcel.";

    private static final String[] COLUMN_HEADERS = new String[]{
            "firstApplicationNumber",
            "firstContactPersonFullName",
            "firstContactPersonPhoneNumber",
            "firstContactPersonPhoneEmail",

            "secondApplicationNumber",
            "secondContactPersonFullName",
            "secondContactPersonPhoneNumber",
            "secondContactPersonPhoneEmail",

            "propertyNumber",
            "propertyName",
            "areaSize",
            "metsahallitus"
    };

    private final EnumLocaliser localiser;

    private final int applicationNumber;
    private final List<HarvestPermitApplicationConflictExcelDTO> rows;

    public HarvestPermitApplicationConflictExcelView(final EnumLocaliser localiser,
                                                     final int applicationNumber,
                                                     final List<HarvestPermitApplicationConflictExcelDTO> rows) {
        this.localiser = Objects.requireNonNull(localiser);
        this.applicationNumber = applicationNumber;
        this.rows = Objects.requireNonNull(rows);
    }

    private String createFilename() {
        return String.format(
                "Konfliktit-%d-Pvm-%se.xlsx",
                applicationNumber,
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final String booleanTrue = localiser.getTranslation("Boolean.true");
        final String booleanFalse = localiser.getTranslation("Boolean.false");

        final ExcelHelper helper = new ExcelHelper(workbook);
        helper.appendHeaderRow(localiser.translate(COLUMN_HEADER_PREFIX, COLUMN_HEADERS));

        for (final HarvestPermitApplicationConflictExcelDTO f : rows) {
            helper.appendRow();

            helper.appendNumberCell(f.getFirstApplicationNumber());
            helper.appendTextCell(f.getFirstApplicationContactPerson().getFullName());
            helper.appendTextCell(f.getFirstApplicationContactPerson().getPhoneNumber());
            helper.appendTextCell(f.getFirstApplicationContactPerson().getEmail());

            helper.appendNumberCell(f.getSecondApplicationNumber());
            helper.appendTextCell(f.getSecondApplicationContactPerson().getFullName());
            helper.appendTextCell(f.getSecondApplicationContactPerson().getPhoneNumber());
            helper.appendTextCell(f.getSecondApplicationContactPerson().getEmail());

            helper.appendTextCell(f.getPropertyNumber());
            helper.appendTextCell(f.getPropertyName());
            helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getConflictAreaSize()));
            helper.appendTextCell(f.isMetsahallitus() ? booleanTrue : booleanFalse);
        }

        helper.autoSizeColumns();
    }

}
