package fi.riista.feature.organization.rka;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;
import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

public class AreaMeetingRepresentativeExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "AreaMeetingRepresentativeExcelView.";

    private static final int TABLE_CONTENT_START_ROW = 15;

    private static final int LOGO_COLUMN = 5;
    private static final int LOGO_START_ROW = 0;
    private static final int LOGO_END_ROW = 2;

    private final EnumLocaliser localiser;
    private final OrganisationNameDTO rka;
    private final List<AreaMeetingRhyRepresentativesDTO> representativeList;
    private CellStyle wrappedTextStyle;

    public AreaMeetingRepresentativeExcelView(@Nonnull final EnumLocaliser localiser,
                                              @Nonnull final OrganisationNameDTO rka,
                                              @Nonnull final List<AreaMeetingRhyRepresentativesDTO> representativeList) {
        this.localiser = requireNonNull(localiser);
        this.rka = requireNonNull(rka);
        this.representativeList = requireNonNull(representativeList);
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper excelHelper = new ExcelHelper(workbook);
        wrappedTextStyle = workbook.createCellStyle();
        wrappedTextStyle.setWrapText(true);

        appendPreamble(excelHelper);

        appendTableHeader(excelHelper);
        appendTableContent(excelHelper);

        appendOtherRows(excelHelper);

        insertLogo(workbook);
        excelHelper.autoSizeColumns();
    }

    private void appendOtherRows(final ExcelHelper excelHelper) {
        excelHelper
                .appendRow()
                .appendRow()
                .appendRow()
                .appendRow()
                .appendTextCellBold(i18n("participantCount"))
                .appendEmptyCell(5)
                .appendRow()
                .appendRow()
                .appendTextCellBold(i18n("acceptedVoteCount"))
                .appendEmptyCell(4)
                .appendFormula(String.format("SUM(F%d:F%d)",
                        TABLE_CONTENT_START_ROW,
                        calculateVoteCountEndRow()));

        excelHelper
                .appendRow()
                .appendRow()
                .appendRow()
                .appendRow()
                .appendRow()
                .appendRow()
                .appendEmptyCell(2)
                .appendTextCell(i18n("wildlifeAgencyRepresentative")).withBorders(THIN, NONE, NONE, NONE)
                .appendTextCell("").withBorders(THIN, NONE, NONE, NONE)
                .appendTextCell("").withBorders(THIN, NONE, NONE, NONE);
    }

    private int calculateVoteCountEndRow() {
        return TABLE_CONTENT_START_ROW + Math.max(representativeList.size() - 1, 0);
    }

    private void appendPreamble(final ExcelHelper excelHelper) {
        excelHelper.appendRow()
                .appendTextCellBold(localiser.getTranslation(rka.getNameLocalisation()))
                .appendEmptyCell(4)
                .appendRow()
                .appendRow()
                .appendRow()
                .appendTextCell(i18n("representativeList"))
                .appendRow()
                .appendRow()
                .appendTextCell(i18n("regionalMeeting"))
                .appendRow()
                .appendRow()
                .appendTextCell(i18n("location"))
                .appendRow()
                .appendTextCell(i18n("date"))
                .appendRow()
                .appendRow();
    }

    private void insertLogo(final Workbook workbook) throws IOException {
        final InputStream is = getClass().getResourceAsStream("/logo.png");
        final byte[] bytes = IOUtils.toByteArray(is);
        final int pictureIndex = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        is.close();

        final CreationHelper helper = workbook.getCreationHelper();
        final Drawing drawingPatriarch = workbook.getSheetAt(0).createDrawingPatriarch();

        final ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(LOGO_COLUMN);
        anchor.setCol2(LOGO_COLUMN);
        anchor.setRow1(LOGO_START_ROW);
        anchor.setRow2(LOGO_END_ROW);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

        final Picture pict = drawingPatriarch.createPicture(anchor, pictureIndex);
        pict.resize(0.7f, 1.8f);
    }

    private void appendTableContent(final ExcelHelper excelHelper) {
        representativeList.stream()
                .forEach(r -> excelHelper.appendRow()

                        .appendTextCell(mapNullable(
                                r.getRepresentatives(),
                                this::formatRepresentativeList), wrappedTextStyle).withBorders(THIN, THIN, THIN, THIN)

                        .appendTextCell("", HorizontalAlignment.CENTER).withBorders(THIN, THIN, THIN, THIN)

                        .appendTextCell(mapNullable(
                                r.getSubstituteRepresentatives(),
                                this::formatRepresentativeList), wrappedTextStyle).withBorders(THIN, THIN, THIN, THIN)

                        .appendTextCell("", HorizontalAlignment.CENTER).withBorders(THIN, THIN, THIN, THIN)

                        .appendTextCell(localiser.getTranslation(r.getRhy().getNameLocalisation())).withBorders(THIN, THIN, THIN, THIN)

                        .appendNumberCell(1).withBorders(THIN, THIN, THIN, THIN));
    }

    private String formatRepresentativeList(List<RepresentativePersonDTO> representatives) {
        return representatives.stream()
                .map(this::formatRepresentative)
                .collect(Collectors.joining("\n"));
    }

    private String formatRepresentative(final RepresentativePersonDTO representative) {
        return String.format("%s %s", representative.getByName(), representative.getLastName());
    }

    private void appendTableHeader(final ExcelHelper excelHelper) {
        excelHelper
                .appendRow()
                .appendEmptyCell(1).withBorders(THIN, NONE, THIN, NONE)
                .appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE)
                .appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE)
                .appendEmptyCell(1).withBorders(THIN, NONE, NONE, THIN)
                .appendEmptyCell(1).withBorders(THIN, NONE, THIN, THIN)
                .appendEmptyCell(1).withBorders(THIN, NONE, THIN, THIN)
                .appendRow()
                .appendTextCell(i18n("representativeName")).withBorders(NONE, NONE, THIN, NONE)
                .appendTextCell(i18n("participating"))
                .appendTextCell(i18n("viceRepresentativeName"))
                .appendTextCell(i18n("participating")).withBorders(NONE, NONE, NONE, THIN)
                .appendTextCell(i18n("rhy")).withBorders(NONE, NONE, THIN, THIN)
                .appendTextCell(i18n("voteCount")).withBorders(NONE, NONE, THIN, THIN)
                .appendRow()
                .appendEmptyCell(1).withBorders(NONE, THIN, THIN, NONE)
                .appendEmptyCell(1).withBorders(NONE, THIN, NONE, NONE)
                .appendEmptyCell(1).withBorders(NONE, THIN, NONE, NONE)
                .appendEmptyCell(1).withBorders(NONE, THIN, NONE, THIN)
                .appendEmptyCell(1).withBorders(NONE, THIN, THIN, THIN)
                .appendEmptyCell(1).withBorders(NONE, THIN, THIN, THIN);
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n("fileName"), Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }

    private String i18n(final String key) {
        return localiser.getTranslation(LOCALISATION_PREFIX + key);
    }
}
