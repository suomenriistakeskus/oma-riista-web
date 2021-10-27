package fi.riista.feature.largecarnivorereport;

import com.google.common.collect.Streams;
import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.srva.SrvaEventDTO;
import fi.riista.feature.gamediary.srva.specimen.SrvaSpecimenDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.feature.otherwisedeceased.OtherwiseDeceasedDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_LYNX;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_BEAR;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_LYNX_PORONHOITO;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF;
import static fi.riista.feature.harvestpermit.HarvestPermitCategory.LARGE_CARNIVORE_WOLF_PORONHOITO;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaDetailedType.PORONHOITOALUE_ITAINEN;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaDetailedType.PORONHOITOALUE_LANTINEN;
import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.DateUtil.now;
import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

public class LargeCarnivoreReportExcelView extends AbstractXlsxView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    private static final int LOGO_COLUMN = 0;
    private static final int LOGO_START_ROW = 2;
    private static final int LOGO_END_ROW = 4;

    private final EnumLocaliser localiser;
    private final LargeCarnivoreExcelExportDTO dto;

    private Font titleFont;
    private CellStyle titleStyle;

    private Font textFont;
    private CellStyle tableTextStyle;
    private CellStyle wrappedTableTextStyle;
    private CellStyle tableDateStyle;
    private CellStyle tableDateTimeStyle;

    private Font boldFont;
    private CellStyle boldTextStyle;
    private CellStyle boldTextGrayBackgroundStyle;

    private CellStyle tableHeaderStyle;

    private Font summaryTableNumberFont;
    private CellStyle summaryTableNumberStyle;
    private CellStyle tableTextBoldStyle;

    public LargeCarnivoreReportExcelView(final EnumLocaliser localiser,
                                         final LargeCarnivoreExcelExportDTO dto) {
        this.localiser = localiser;
        this.dto = dto;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        loadLogo(workbook);

        createStyles(workbook);

        createSheets(workbook);
    }

    private void loadLogo(final Workbook workbook) throws IOException {

        final InputStream is = getClass().getResourceAsStream("/riista-logo.png");
        final byte[] bytes = IOUtils.toByteArray(is);
        workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        is.close();
    }

    private void insertLogo(final Workbook workbook, final String sheetName, final float scaleX, final float scaleY) {
        final CreationHelper helper = workbook.getCreationHelper();
        final int sheetIndex = workbook.getSheetIndex(sheetName);
        final Drawing drawingPatriarch = workbook.getSheetAt(sheetIndex).createDrawingPatriarch();

        final ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(LOGO_COLUMN);
        anchor.setCol2(LOGO_COLUMN);
        anchor.setRow1(LOGO_START_ROW);
        anchor.setRow2(LOGO_END_ROW);
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);

        final Picture pict = drawingPatriarch.createPicture(anchor, 0);
        pict.resize(scaleX, scaleY);
    }

    private void createStyles(final Workbook wb) {
        this.titleFont = wb.createFont();
        this.titleFont.setFontHeightInPoints((short)20);
        this.titleFont.setFontName("Arial");
        this.titleFont.setBold(true);

        this.titleStyle = wb.createCellStyle();
        this.titleStyle.setFont(titleFont);

        this.textFont = wb.createFont();
        this.textFont.setFontHeightInPoints((short) 11);
        this.textFont.setFontName("Arial");

        this.tableTextStyle = wb.createCellStyle();
        this.tableTextStyle.setBorderTop(THIN);
        this.tableTextStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextStyle.setBorderBottom(THIN);
        this.tableTextStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextStyle.setBorderLeft(THIN);
        this.tableTextStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextStyle.setBorderRight(THIN);
        this.tableTextStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextStyle.setFont(textFont);

        this.wrappedTableTextStyle = wb.createCellStyle();
        this.wrappedTableTextStyle.setBorderTop(THIN);
        this.wrappedTableTextStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.wrappedTableTextStyle.setBorderBottom(THIN);
        this.wrappedTableTextStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.wrappedTableTextStyle.setBorderLeft(THIN);
        this.wrappedTableTextStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.wrappedTableTextStyle.setBorderRight(THIN);
        this.wrappedTableTextStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.wrappedTableTextStyle.setFont(textFont);
        this.wrappedTableTextStyle.setWrapText(true);

        this.tableDateStyle = wb.createCellStyle();
        this.tableDateStyle.setBorderTop(THIN);
        this.tableDateStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateStyle.setBorderBottom(THIN);
        this.tableDateStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateStyle.setBorderLeft(THIN);
        this.tableDateStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateStyle.setBorderRight(THIN);
        this.tableDateStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateStyle.setFont(textFont);
        this.tableDateStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy"));

        this.tableDateTimeStyle = wb.createCellStyle();
        this.tableDateTimeStyle.setBorderTop(THIN);
        this.tableDateTimeStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateTimeStyle.setBorderBottom(THIN);
        this.tableDateTimeStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateTimeStyle.setBorderLeft(THIN);
        this.tableDateTimeStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateTimeStyle.setBorderRight(THIN);
        this.tableDateTimeStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableDateTimeStyle.setFont(textFont);
        this.tableDateTimeStyle.setDataFormat(wb.createDataFormat().getFormat("d.m.yyyy h:mm"));

        this.boldFont = wb.createFont();
        this.boldFont.setFontHeightInPoints((short) 11);
        this.boldFont.setBold(true);
        this.boldFont.setFontName("Arial");

        this.boldTextStyle = wb.createCellStyle();
        this.boldTextStyle.setFont(boldFont);

        this.boldTextGrayBackgroundStyle = wb.createCellStyle();
        this.boldTextGrayBackgroundStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        this.boldTextGrayBackgroundStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.boldTextGrayBackgroundStyle.setFont(boldFont);

        this.tableHeaderStyle = wb.createCellStyle();
        this.tableHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        this.tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.tableHeaderStyle.setBorderTop(THIN);
        this.tableHeaderStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.tableHeaderStyle.setBorderBottom(THIN);
        this.tableHeaderStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.tableHeaderStyle.setBorderLeft(THIN);
        this.tableHeaderStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableHeaderStyle.setBorderRight(THIN);
        this.tableHeaderStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableHeaderStyle.setFont(boldFont);

        this.summaryTableNumberFont = wb.createFont();
        this.summaryTableNumberFont.setFontHeightInPoints((short)20);

        this.summaryTableNumberStyle = wb.createCellStyle();
        this.summaryTableNumberStyle.setDataFormat(wb.createDataFormat().getFormat("# ##0"));
        this.summaryTableNumberStyle.setBorderTop(THIN);
        this.summaryTableNumberStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.summaryTableNumberStyle.setBorderBottom(THIN);
        this.summaryTableNumberStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.summaryTableNumberStyle.setBorderLeft(THIN);
        this.summaryTableNumberStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.summaryTableNumberStyle.setBorderRight(THIN);
        this.summaryTableNumberStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.summaryTableNumberStyle.setFont(summaryTableNumberFont);
        this.summaryTableNumberStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        this.tableTextBoldStyle = wb.createCellStyle();
        this.tableTextBoldStyle.setBorderTop(THIN);
        this.tableTextBoldStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextBoldStyle.setBorderBottom(THIN);
        this.tableTextBoldStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextBoldStyle.setBorderLeft(THIN);
        this.tableTextBoldStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextBoldStyle.setBorderRight(THIN);
        this.tableTextBoldStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableTextBoldStyle.setFont(boldFont);
        this.tableTextBoldStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx",
                localiser.getTranslation("LargeCarnivoreReportExcel.report"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createSheets(final Workbook workbook) {
        createSummarySheet(workbook);

        dto.getReportSpecies().forEach(species -> {
            final String sheetName = StringUtils.capitalize(localiser.getTranslation(LocalisedString.fromMap(species.getName())));
            final ExcelHelper excelHelper = new ExcelHelper(workbook, sheetName);

            final int speciesCode = species.getCode();

            final StringBuilder sb = new StringBuilder();
            sb.append(sheetName).append(" - ");
            if (speciesCode == OFFICIAL_CODE_BEAR) {
                sb.append(localiser.getTranslation("LargeCarnivoreReportExcel.bearHeaderTitle"));
            } else {
                sb.append(localiser.getTranslation("LargeCarnivoreReportExcel.headerTitle"));
            }

            createHeader(excelHelper, sb.toString(), false);

            // Derogations
            createPermitInfo(excelHelper, localiser.getTranslation("LargeCarnivoreReportExcel.derogations"), dto.getDerogations().get(speciesCode));

            // Stock management
            if (speciesCode == OFFICIAL_CODE_BEAR || speciesCode == OFFICIAL_CODE_LYNX || speciesCode == OFFICIAL_CODE_WOLF) {
                List<LargeCarnivorePermitInfoDTO> stockMgmts;
                switch (speciesCode) {
                    case OFFICIAL_CODE_BEAR:
                        stockMgmts = dto.getStockManagements().get(LARGE_CARNIVORE_BEAR);
                        break;
                    case OFFICIAL_CODE_LYNX:
                        stockMgmts = Streams.concat(
                                dto.getStockManagements().get(LARGE_CARNIVORE_LYNX).stream(),
                                dto.getStockManagements().get(LARGE_CARNIVORE_LYNX_PORONHOITO).stream())
                                .collect(Collectors.toList());
                        break;
                    case OFFICIAL_CODE_WOLF:
                        stockMgmts = Streams.concat(
                                        dto.getStockManagements().get(LARGE_CARNIVORE_WOLF).stream(),
                                        dto.getStockManagements().get(LARGE_CARNIVORE_WOLF_PORONHOITO).stream())
                                .collect(Collectors.toList());
                        break;
                    default:
                        stockMgmts = Collections.emptyList();
                }
                createPermitInfo(excelHelper, localiser.getTranslation("LargeCarnivoreReportExcel.stockMgmt"), stockMgmts);
            }

            // Quota
            if (speciesCode == OFFICIAL_CODE_BEAR) {
                createBearQuota(excelHelper);
            }

            // Deportations
            createPermitInfo(excelHelper, localiser.getTranslation("LargeCarnivoreReportExcel.deportations"), dto.getDeportations().get(speciesCode));

            // Research
            createPermitInfo(excelHelper, localiser.getTranslation("LargeCarnivoreReportExcel.research"), dto.getResearch().get(speciesCode));

            // SRVA
            excelHelper
                    .appendRow()
                    .appendRow()
                    .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.srva"), boldTextStyle)
                    .appendRow();
            appendSrvaEvents(excelHelper, dto.getSrvas().get(speciesCode));

            // Otherwise deceased
            excelHelper
                    .appendRow()
                    .appendRow()
                    .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.otherwiseDeceased"), boldTextStyle)
                    .appendRow();
            appendOtherwiseDeceased(excelHelper, dto.getOtherwiseDeceased().get(speciesCode));

            excelHelper
                    .appendRow()
                    .appendRow()
                    .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.killingOrders"), boldTextStyle)
                    .appendRow()
                    .appendRow()
                    .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.deportationOrders"), boldTextStyle)
                    .appendRow()
                    .appendRow()
                    .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.otherEvents"), boldTextStyle);

            excelHelper
                    .setColumnWidth(0, 13000)
                    .setColumnWidth(1, 6300)
                    .setColumnWidth(2, 3500)
                    .setColumnWidth(3, 8000)
                    .setColumnWidth(4, 7000)
                    .setColumnWidth(5, 11500)
                    .setColumnWidth(6, 11500)
                    .setColumnWidth(7, 11500)
                    .setColumnWidth(8, 11500)
                    .setColumnWidth(9, 11500)
                    .setColumnWidth(10, 9600);

            insertLogo(workbook, sheetName, 0.6f, 1f);
        });
    }

    private void createHeader(final ExcelHelper excelHelper,
                              final String title,
                              final boolean isSummarySheet) {
        final String dateStr = new StringBuilder()
                .append(localiser.getTranslation("LargeCarnivoreReportExcel.date"))
                .append(": ")
                .append(DATE_FORMAT.print(DateUtil.today()))
                .toString();

        excelHelper
                .appendRow()
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.team"), boldTextStyle)
                .spanCurrentColumn(4)
                .appendEmptyCell(isSummarySheet ? 1 : 3)
                .appendTextCell(dateStr, boldTextStyle)
                .appendRow().appendRow().appendRow().appendRow()
                .appendEmptyCell(isSummarySheet ? 5 : 7)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.time"), boldTextStyle);

        final int huntingYear = dto.getHuntingYear();
        final String huntingYearStr = new StringBuilder()
                .append(DATE_FORMAT.print(DateUtil.huntingYearBeginDate(huntingYear)))
                .append(" - ")
                .append(DATE_FORMAT.print(DateUtil.huntingYearEndDate(huntingYear)))
                .toString();

        excelHelper.appendRow()
                .appendTextCell(title, titleStyle)
                .spanCurrentColumn(isSummarySheet ? 4 : 6)
                .appendEmptyCell(1)
                .appendTextCell(huntingYearStr, boldTextStyle);
    }

    private void createSummarySheet(final Workbook workbook) {
        final String sheetName = localiser.getTranslation("LargeCarnivoreReportExcel.sum");
        final ExcelHelper excelHelper = new ExcelHelper(workbook, sheetName);

        final String title = localiser.getTranslation("LargeCarnivoreReportExcel.totalDeceased");
        createHeader(excelHelper, title, true);

        excelHelper
                .appendRow()
                .appendRow()
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.species"), tableHeaderStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.sum"), tableHeaderStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.nonReindeerAreaHarvests"), tableHeaderStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.reindeerAreaHarvests"), tableHeaderStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.nonReindeerAreaDeceased"), tableHeaderStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.reindeerAreaDeceased"), tableHeaderStyle);

        dto.getReportSpecies().forEach(species -> {
            final int speciesCode = species.getCode();
            int quotaHarvests = 0;
            if (speciesCode == OFFICIAL_CODE_BEAR) {
                quotaHarvests = Optional.ofNullable(dto.getBearQuotaHarvests().get(PORONHOITOALUE_ITAINEN)).orElse(0) +
                        Optional.ofNullable(dto.getBearQuotaHarvests().get(PORONHOITOALUE_LANTINEN)).orElse(0);
            }
            final int totalHarvests = dto.getTotalHarvests().get(speciesCode) + quotaHarvests;
            final int reindeerAreaHarvests = dto.getReindeerAreaHarvests().get(speciesCode) + quotaHarvests;
            final int nonReindeerAreaHarvests = totalHarvests - reindeerAreaHarvests;
            final int totalOtherwiseDeceased = dto.getTotalOtherwiseDeceased().get(speciesCode);
            final int reindeerAreaOtherwiseDeceased = dto.getReindeerAreaOtherwiseDeceased().get(speciesCode);
            final int nonReindeerAreaOtherwiseDeceased = totalOtherwiseDeceased - reindeerAreaOtherwiseDeceased;

            excelHelper
                    .appendRow()
                    .setCurrentRowHeight(35)
                    .appendTextCell(StringUtils.capitalize(localiser.getTranslation(LocalisedString.fromMap(species.getName()))), tableTextBoldStyle)
                    .appendNumberCell(totalHarvests + totalOtherwiseDeceased, summaryTableNumberStyle)
                    .appendNumberCell(nonReindeerAreaHarvests, summaryTableNumberStyle)
                    .appendNumberCell(reindeerAreaHarvests, summaryTableNumberStyle)
                    .appendNumberCell(nonReindeerAreaOtherwiseDeceased, summaryTableNumberStyle)
                    .appendNumberCell(reindeerAreaOtherwiseDeceased, summaryTableNumberStyle);
        });

        excelHelper
                .setColumnWidth(0, 3000)
                .setColumnWidth(1, 4000)
                .setColumnWidth(2, 7700)
                .setColumnWidth(3, 10600)
                .setColumnWidth(4, 8400)
                .setColumnWidth(5, 9400);

        insertLogo(workbook, sheetName, 2.2f, 1f);
    }

    private void createPermitInfo(final ExcelHelper excelHelper,
                                  final String title,
                                  final List<LargeCarnivorePermitInfoDTO> permitInfoList) {
        excelHelper
                .appendRow()
                .appendRow()
                .appendTextCell(title, boldTextStyle)
                .appendRow();
        appendPermitTableHeader(excelHelper);
        appendPermitInfo(excelHelper, permitInfoList);
    }

    private void appendPermitTableHeader(final ExcelHelper excelHelper) {
        final String HEADER_PREXIX = "LargeCarnivoreReportExcel.";
        final String[] HEADERS = new String[]{
                "permitNumber",
                "decisionType",
                "decisionTime",
                "permitPeriod",
                "applied",
                "granted",
                "harvest",
                "rhy",
                "rka",
                "reindeerArea",
                "additionalInfo"};
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header, tableHeaderStyle));
    }

    private void appendPermitInfo(final ExcelHelper excelHelper,
                                  final List<LargeCarnivorePermitInfoDTO> permitInfoList) {
        if (permitInfoList.isEmpty()) {
            excelHelper
                    .appendRow()
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle);
            return;
        }

        permitInfoList.forEach(permitInfo -> {
            excelHelper.appendRow();

            final String permitNumber = permitInfo.getPermitNumber();
            if (permitNumber != null) {
                excelHelper.appendTextCell(permitNumber, tableTextStyle);
            } else {
                excelHelper.appendNumberCell(permitInfo.getApplicationNumber(), tableTextStyle);
            }

            excelHelper
                    .appendTextCell(localiser.getTranslation(permitInfo.getDecisionType()), tableTextStyle)
                    .appendDateCell(DateUtil.toLocalDateNullSafe(permitInfo.getDecisionTime()), tableDateStyle);

            final StringBuilder sb = new StringBuilder();
            if (permitInfo.getBeginDate() != null) {
                sb.append(DATE_FORMAT.print(permitInfo.getBeginDate()))
                        .append(" - ")
                        .append(DATE_FORMAT.print(permitInfo.getEndDate()));
                if (permitInfo.getBeginDate2() != null) {
                    sb.append(", ")
                            .append(DATE_FORMAT.print(permitInfo.getBeginDate2()))
                            .append(" - ")
                            .append(DATE_FORMAT.print(permitInfo.getEndDate2()));
                }
            }
            excelHelper
                    .appendTextCell(sb.toString(), tableTextStyle)
                    .appendNumberCell(permitInfo.getApplied(), tableTextStyle);

            if (permitNumber != null) {
                excelHelper.appendNumberCell(permitInfo.getGranted(), tableTextStyle);
                excelHelper.appendNumberCell(permitInfo.getHarvests(), tableTextStyle);
            } else {
                excelHelper.appendTextCell("", tableTextStyle)
                        .appendTextCell("", tableTextStyle);
            }

            excelHelper
                    .appendTextCell(localiser.getTranslation(permitInfo.getRhy()), tableTextStyle)
                    .appendTextCell(localiser.getTranslation(permitInfo.getRka()), tableTextStyle)
                    .appendTextCell(permitInfo.isOnReindeerArea() ? "x" : "", tableTextStyle)
                    .appendTextCell("", tableTextStyle);
        });
    }

    private void createBearQuota(final ExcelHelper excelHelper) {
        excelHelper
                .appendRow()
                .appendRow()
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaTitle"), boldTextStyle)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quota"), boldTextGrayBackgroundStyle)
                .withBorders(THIN, NONE, THIN, THIN)
                .appendTextCell("", boldTextGrayBackgroundStyle).withBorders(THIN, NONE, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaHarvest"), boldTextGrayBackgroundStyle)
                .withBorders(THIN, NONE, THIN, THIN)
                .appendTextCell("", boldTextGrayBackgroundStyle).withBorders(THIN, NONE, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaHarvest"), boldTextGrayBackgroundStyle)
                .withBorders(THIN, NONE, THIN, THIN);

        excelHelper
                .appendRow()
                .appendEmptyCell(1)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaEastern"), boldTextGrayBackgroundStyle)
                .withBorders(NONE, THIN, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaWestern"), boldTextGrayBackgroundStyle)
                .withBorders(NONE, THIN, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaEastern"), boldTextGrayBackgroundStyle)
                .withBorders(NONE, THIN, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.quotaWestern"), boldTextGrayBackgroundStyle)
                .withBorders(NONE, THIN, THIN, THIN)
                .appendTextCell(localiser.getTranslation("LargeCarnivoreReportExcel.sum"), boldTextGrayBackgroundStyle)
                .withBorders(NONE, THIN, THIN, THIN);

        final int easternHarvests = Optional.ofNullable(dto.getBearQuotaHarvests().get(PORONHOITOALUE_ITAINEN)).orElse(0);
        final int westernHarvests = Optional.ofNullable(dto.getBearQuotaHarvests().get(PORONHOITOALUE_LANTINEN)).orElse(0);
        excelHelper
                .appendRow()
                .appendEmptyCell(1)
                .appendNumberCell(dto.getBearQuotas().get(PORONHOITOALUE_ITAINEN), tableTextStyle)
                .appendNumberCell(dto.getBearQuotas().get(PORONHOITOALUE_LANTINEN), tableTextStyle)
                .appendNumberCell(easternHarvests, tableTextStyle)
                .appendNumberCell(westernHarvests, tableTextStyle)
                .appendNumberCell(easternHarvests + westernHarvests, tableTextStyle);
    }

    private void appendSrvaTableHeader(final ExcelHelper excelHelper) {
        final String HEADER_PREXIX = "LargeCarnivoreReportExcel.";
        final String[] HEADERS = new String[]{
                "date",
                "event",
                "amount",
                "result",
                "gender",
                "age",
                "rhy",
                "rka"};
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header, tableHeaderStyle));
    }

    private void appendSrvaEvents(final ExcelHelper excelHelper, final List<LargeCarnivoreSrvaEventDTO> srvaEvents) {
        appendSrvaTableHeader(excelHelper);

        if (srvaEvents == null || srvaEvents.isEmpty()) {
            excelHelper.appendRow()
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle);
            return;
        }

        srvaEvents.forEach(eventDto -> {
            final SrvaEventDTO event = eventDto.getEvent();

            excelHelper.appendRow();

            excelHelper
                    .appendDateTimeCell(event.getPointOfTime(), tableDateTimeStyle)
                    .appendTextCell(localiser.getTranslation(event.getEventName()), tableTextStyle)
                    .appendNumberCell(event.getTotalSpecimenAmount(),  tableTextStyle)
                    .appendTextCell(localiser.getTranslation(event.getEventResult()), tableTextStyle);

            final Optional<List<SrvaSpecimenDTO>> specimenOpt = Optional.ofNullable(event.getSpecimens());
            if (specimenOpt.isPresent()) {
                final List<String> genders = specimenOpt.get().stream()
                        .map(s -> localiser.getTranslation(s.getGender()))
                        .collect(Collectors.toList());
                excelHelper.appendTextCell(String.join("\n", genders), wrappedTableTextStyle);

                final List<String> ages = specimenOpt.get().stream()
                        .map(s -> localiser.getTranslation(s.getAge()))
                        .collect(Collectors.toList());
                excelHelper.appendTextCell(String.join("\n", ages), wrappedTableTextStyle);
            } else {
                excelHelper.appendTextCell("", tableTextStyle)
                        .appendTextCell("", tableTextStyle);
            }

            final RiistanhoitoyhdistysDTO rhy = eventDto.getRhy();
            if (rhy != null) {
                excelHelper.appendTextCell(localiser.getTranslation(LocalisedString.of(rhy.getNameFI(), rhy.getNameSV())), tableTextStyle);

                final OrganisationNameDTO rka = eventDto.getRka();
                excelHelper.appendTextCell(localiser.getTranslation(LocalisedString.of(rka.getNameFI(), rka.getNameSV())), tableTextStyle);
            } else {
                excelHelper.appendTextCell("", tableTextStyle)
                        .appendTextCell("", tableTextStyle);
            }
        });
    }

    private void appendOtherwiseDeceasedTableHeader(final ExcelHelper excelHelper) {
        final String HEADER_PREXIX = "LargeCarnivoreReportExcel.";
        final String[] HEADERS = new String[]{
                "date",
                "gender",
                "age",
                "weight",
                "reason",
                "otherReason",
                "source",
                "otherSource",
                "municipality",
                "rhy",
                "rka",
                "description"};
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header, tableHeaderStyle));
    }

    private void appendOtherwiseDeceased(final ExcelHelper excelHelper,
                                         final List<LargeCarnivoreOtherwiseDeceasedDTO> dtoList) {
        appendOtherwiseDeceasedTableHeader(excelHelper);

        if (dtoList == null || dtoList.isEmpty()) {
            excelHelper.appendRow()
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle)
                    .appendTextCell("", tableTextStyle);

            return;
        }

        dtoList.forEach(dto -> {
            final OtherwiseDeceasedDTO deceased = dto.getOtherwiseDeceased();

            excelHelper.appendRow()
                    .appendDateTimeCell(deceased.getPointOfTime(), tableDateTimeStyle)
                    .appendTextCell(localiser.getTranslation(deceased.getGender()), tableTextStyle)
                    .appendTextCell(localiser.getTranslation(deceased.getAge()), tableTextStyle)
                    .appendNumberCell(deceased.getWeight(), tableTextStyle)
                    .appendTextCell(localiser.getTranslation(deceased.getCause()), tableTextStyle)
                    .appendTextCell(deceased.getCauseOther(), tableTextStyle)
                    .appendTextCell(localiser.getTranslation(deceased.getSource()), tableTextStyle)
                    .appendTextCell(deceased.getSourceOther(), tableTextStyle);

            final OrganisationNameDTO municipality = deceased.getMunicipality();
            excelHelper
                    .appendTextCell(localiser.getTranslation(LocalisedString.of(municipality.getNameFI(), municipality.getNameSV())), tableTextStyle);

            final OrganisationNameDTO rhy = deceased.getRhy();
            excelHelper
                    .appendTextCell(localiser.getTranslation(LocalisedString.of(rhy.getNameFI(), rhy.getNameSV())), tableTextStyle);

            final OrganisationNameDTO rka = deceased.getRka();
            excelHelper
                    .appendTextCell(localiser.getTranslation(LocalisedString.of(rka.getNameFI(), rka.getNameSV())), tableTextStyle)
                    .appendTextCell(deceased.getDescription(), tableTextStyle);

        });
    }
}
