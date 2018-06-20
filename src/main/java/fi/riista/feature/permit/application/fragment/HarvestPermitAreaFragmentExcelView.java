package fi.riista.feature.permit.application.fragment;

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
            "propertyAreaSize",
            "metsahallitus"
    };

    private final EnumLocaliser localiser;

    private final String permitNumber;
    private final List<HarvestPermitAreaFragmentInfoDTO> rows;

    public HarvestPermitAreaFragmentExcelView(final EnumLocaliser localiser,
                                              final String permitNumber,
                                              final List<HarvestPermitAreaFragmentInfoDTO> rows) {
        this.localiser = Objects.requireNonNull(localiser);
        this.permitNumber = Objects.requireNonNull(permitNumber);
        this.rows = Objects.requireNonNull(rows);
    }

    private String createFilename() {
        return String.format(
                "Sirpaleet-%s-Pvm-%se.xlsx",
                permitNumber,
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

        String previousHash = null;
        for (final HarvestPermitAreaFragmentInfoDTO f : rows) {
            helper.appendRow();
            helper.appendTextCell(f.getHash());

            if (Objects.equals(previousHash, f.getHash())) {
                helper.appendEmptyCell(9);
            } else {
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getAreaSize() - f.getWaterAreaSize()));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getWaterAreaSize()));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getAreaSize()));

                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getValtionmaaAreaSize() - f.getValtionmaaWaterAreaSize()));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getValtionmaaWaterAreaSize()));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getValtionmaaAreaSize()));

                final double yksityismaaAreaSize = f.getAreaSize() - f.getValtionmaaAreaSize();
                final double yksityismaaWaterAreaSize = f.getWaterAreaSize() - f.getValtionmaaWaterAreaSize();
                final double yksityismaaLandAreaSize = yksityismaaAreaSize - yksityismaaWaterAreaSize;

                helper.appendNumberCell(NumberUtils.squareMetersToHectares(yksityismaaLandAreaSize));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(yksityismaaWaterAreaSize));
                helper.appendNumberCell(NumberUtils.squareMetersToHectares(yksityismaaAreaSize));
            }

            helper.appendTextCell(f.getPropertyNumber());
            helper.appendNumberCell(NumberUtils.squareMetersToHectares(f.getPropertyArea()));
            helper.appendTextCell(f.isMetsahallitus() ? booleanTrue : booleanFalse);

            previousHash = f.getHash();
        }

        helper.autoSizeColumns();
    }
}
