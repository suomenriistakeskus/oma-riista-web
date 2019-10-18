package fi.riista.feature.gis.kiinteisto;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.entity.PropertyIdentifier;
import fi.riista.feature.gis.zone.GISZoneMmlPropertyIntersectionDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GISPropertyExcelListView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "GISPropertyListExcel.";

    private static final String[] COLUMN_HEADERS_SELECTED = new String[]{
            "propertyIdentifier",
            "propertyIdentifierPart1",
            "propertyIdentifierPart2",
            "propertyIdentifierPart3",
            "propertyIdentifierPart4",
            "palstaId",
            "propertyName",
            "propertySize",
            "propertyOriginalSize",
            "propertyIsChanged"
    };

    private static final String[] COLUMN_HEADERS_CALCULATED = new String[]{
            "propertyIdentifier",
            "palstaId",
            "intersectionArea",
            "propertyName"
    };

    private final EnumLocaliser localiser;

    private final LocalisedString clubName;
    private final LocalisedString areaName;
    private final List<GISPropertyExcelRow> rows;
    private List<GISZoneMmlPropertyIntersectionDTO> geometryRows;

    public GISPropertyExcelListView(final EnumLocaliser localiser,
                                    final LocalisedString clubName,
                                    final LocalisedString areaName,
                                    final List<GISPropertyExcelRow> rows,
                                    final List<GISZoneMmlPropertyIntersectionDTO> geometryRows) {

        this.localiser = requireNonNull(localiser);
        this.clubName = requireNonNull(clubName);
        this.areaName = requireNonNull(areaName);
        this.rows = requireNonNull(rows);
        this.geometryRows = geometryRows;
    }

    private String createFilename() {
        return String.format(
                "%s - %s-%s.xlsx",
                localiser.getTranslation(clubName),
                localiser.getTranslation(areaName),
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
        appendDescriptionSheet(workbook);
        appendChosenPalstaSheet(workbook);
        appendPalstaByGeometrySheet(workbook);
    }

    private void appendDescriptionSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(LOCALISATION_PREFIX + "frontPageSheet"));

        helper
                .appendRow()
                .appendTextCellBold(localiser.getTranslation(LOCALISATION_PREFIX + "frontPageTextTitle"))
                .appendRow()
                .appendRow()
                .appendTextCell(localiser.getTranslation(LOCALISATION_PREFIX + "frontPageInfoText"));
        helper.autoSizeColumns();
    }

    private void appendChosenPalstaSheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(LOCALISATION_PREFIX + "selectedPalstaSheet"))
                .appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, COLUMN_HEADERS_SELECTED));

        final String booleanTrue = localiser.getTranslation("Boolean.true");
        final String booleanFalse = localiser.getTranslation("Boolean.false");

        rows.forEach(feature -> {
            helper.appendRow()
                    .appendTextCell(feature.getPropertyIdentifier().getDelimitedValue())
                    .appendTextCell(feature.getPropertyIdentifier().getKuntanumero())
                    .appendTextCell(feature.getPropertyIdentifier().getSijaintialuenumero())
                    .appendTextCell(feature.getPropertyIdentifier().getRyhmanumero())
                    .appendTextCell(feature.getPropertyIdentifier().getYksikkonumero())
                    .appendNumberCell(feature.getPalstaId())
                    .appendTextCell(feature.getPropertyName())
                    .appendDoubleCell(feature.formatActualSize(), 2)
                    .appendDoubleCell(feature.formatOriginalSize(), 2)
                    .appendTextCell(feature.isChanged() ? booleanTrue : booleanFalse);
        });

        helper.autoSizeColumns();
    }

    private void appendPalstaByGeometrySheet(Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation(LOCALISATION_PREFIX + "calculatedPalstaSheet"))
                .appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, COLUMN_HEADERS_CALCULATED));


        geometryRows.forEach(feature -> {
            helper.appendRow()
                    .appendTextCell(PropertyIdentifier.create(feature.getKiinteistoTunnus()).getDelimitedValue())
                    .appendNumberCell(feature.getPalstaId())
                    .appendDoubleCell(feature.getIntersectionArea() / 10_000, 2)
                    .appendTextCell(feature.getName());
        });

        helper.autoSizeColumns();
    }

}
