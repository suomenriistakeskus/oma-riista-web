package fi.riista.feature.permit.application.fragment;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gis.zone.TotalLandWaterSizeDTO;
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
import java.util.Objects;

public class HarvestPermitAreaFragmentExcelView extends AbstractXlsxView {
    private static final String COLUMN_HEADER_PREFIX = "HarvestPermitAreaFragmentExcel.";
    private static final String[] COLUMN_HEADERS = new String[]{
            "hash",

            "landAreaSize",
            "waterAreaSize",
            "areaSize",

            "valtionmaaLandAreaSize",
            "valtionmaaWaterAreaSize",
            "valtionmaaAreaSize",

            "yksityismaaLandAreaSize",
            "yksityismaaWaterAreaSize",
            "yksityismaaAreaSize",

            "propertyIdentifier",
            "propertyName",
            "propertyAreaSize",
            "metsahallitus"
    };

    private final EnumLocaliser localiser;

    private final int applicationNumber;
    private final List<HarvestPermitAreaFragmentInfoDTO> rows;

    public HarvestPermitAreaFragmentExcelView(final EnumLocaliser localiser,
                                              final int applicationNumber,
                                              final List<HarvestPermitAreaFragmentInfoDTO> rows) {
        this.localiser = Objects.requireNonNull(localiser);
        this.applicationNumber = applicationNumber;
        this.rows = Objects.requireNonNull(rows);
    }

    private String createFilename() {
        final String prefix = localiser.getTranslation(COLUMN_HEADER_PREFIX + "filename");
        final String timestamp = Constants.FILENAME_TS_PATTERN.print(DateUtil.now());

        return String.format(prefix + "-%d-%s.xlsx", applicationNumber, timestamp);
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

        for (final HarvestPermitAreaFragmentInfoDTO fragment : rows) {
            helper.appendRow();
            helper.appendTextCell(fragment.getHash());

            addSizeColumns(fragment.getBothSize(), helper);
            addSizeColumns(fragment.getStateSize(), helper);
            addSizeColumns(fragment.getPrivateSize(), helper);

            int propertyCounter = 0;

            for (final HarvestPermitAreaFragmentPropertyDTO property : fragment.getPropertyNumbers()) {
                if (propertyCounter++ > 0) {
                    helper.appendRow();
                    helper.appendTextCell(fragment.getHash());
                    helper.appendEmptyCell(9);
                }

                helper.appendTextCell(property.getPropertyNumber());
                helper.appendTextCell(property.getPropertyName());
                helper.appendDoubleCell(property.getPropertyArea() / 10_000, 2);
                helper.appendTextCell(property.isMetsahallitus() ? booleanTrue : booleanFalse);
            }
        }

        helper.autoSizeColumns();
    }

    private static void addSizeColumns(final TotalLandWaterSizeDTO size, final ExcelHelper helper) {
        helper.appendDoubleCell(size.getLand() / 10_000, 2);
        helper.appendDoubleCell(size.getWater() / 10_000, 2);
        helper.appendDoubleCell(size.getTotal() / 10_000, 2);
    }
}
