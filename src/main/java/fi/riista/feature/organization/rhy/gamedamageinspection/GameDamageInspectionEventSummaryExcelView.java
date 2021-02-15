package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.util.DateUtil.now;
import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

public class GameDamageInspectionEventSummaryExcelView extends AbstractXlsxView {

    private ExcelHelper excelHelper;

    private Locale locale;
    private EnumLocaliser localiser;
    private int year;
    private GameDamageType gameDamageType;
    private List<GameDamageInspectionEventSummaryDTO> dtos;

    private CellStyle headerStyle;
    private CellStyle tableCellCurrencyStyle;

    private Font tableFont;

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    public GameDamageInspectionEventSummaryExcelView(final Locale locale,
                                                     final EnumLocaliser localiser,
                                                     final int year,
                                                     final GameDamageType gameDamageType,
                                                     final List<GameDamageInspectionEventSummaryDTO> dtos) {
        this.locale = locale;
        this.localiser = localiser;
        this.year = year;
        this.gameDamageType = gameDamageType;
        this.dtos = dtos;
    }

    private String createFilename()        {
        return String.format("%s-%s-%s-%s.xlsx",
                localiser.getTranslation("GameDamageInspectionEventExcelView.gameDamageInspectionEvent"),
                localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.summary"),
                localiser.getTranslation(gameDamageType == GameDamageType.MOOSELIKE ?
                        "GameDamageInspectionEventExcelView.GAME_TYPE_MOOSELIKE" :
                        "GameDamageInspectionEventExcelView.GAME_TYPE_LARGE_CARNIVORE"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createStyles() {
        final Workbook wb = excelHelper.getSheet().getWorkbook();

        final XSSFColor gray = new XSSFColor(new java.awt.Color(242, 242, 242));
        this.headerStyle = wb.createCellStyle();
        ((XSSFCellStyle)this.headerStyle).setFillForegroundColor(gray);
        this.headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.headerStyle.setBorderTop(THIN);
        this.headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyle.setBorderBottom(THIN);
        this.headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyle.setBorderLeft(THIN);
        this.headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyle.setBorderRight(THIN);
        this.headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());

        this.headerStyle.setAlignment(HorizontalAlignment.LEFT);
        this.headerStyle.setVerticalAlignment(VerticalAlignment.TOP);

        final Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short)11);
        headerFont.setBold(true);
        this.headerStyle.setFont(headerFont);
        this.headerStyle.setWrapText(true);

        this.tableFont = wb.createFont();
        this.tableFont.setFontHeightInPoints((short)10);

        this.tableCellCurrencyStyle = wb.createCellStyle();
        this.tableCellCurrencyStyle.setFont(this.tableFont);
        this.tableCellCurrencyStyle.setBorderLeft(THIN);
        this.tableCellCurrencyStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyle.setBorderRight(THIN);
        this.tableCellCurrencyStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));

    }

    private void createApplicationTitleSection() {
        excelHelper.appendRow();
        excelHelper.appendTextCell(localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.riistakeskus"))
                .spanCurrentColumn(5)
                .appendDateCell(DateUtil.today())
                .appendTextCell(localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.appendix"));

        excelHelper.appendRow().appendRow();

        final String applicationTitleFirstRow = this.gameDamageType == GameDamageType.MOOSELIKE ?
                localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.TITLE_FIRST_ROW_MOOSELIKE") :
                localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.TITLE_FIRST_ROW_LARGE_CARNIVORE");
        excelHelper.appendTextCell(applicationTitleFirstRow).spanCurrentColumn(7);

        excelHelper.appendRow();

        final String applicationTitleSecondRow = this.gameDamageType == GameDamageType.MOOSELIKE ?
                localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.TITLE_SECOND_ROW_MOOSELIKE") :
                localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.TITLE_SECOND_ROW_LARGE_CARNIVORE");
        excelHelper.appendTextCell(applicationTitleSecondRow).spanCurrentColumn(7);

        excelHelper.appendRow().appendRow();

        final String expensesText = localiser.getTranslation("GameDamageInspectionEventSummaryExcelView.expenses") +
                " 1.1. - 31.12." + year;
        excelHelper.appendTextCell(expensesText).spanCurrentColumn(7);
    }

    private void createTableHeader() {
        final String HEADER_PREXIX = "GameDamageInspectionEventSummaryExcelView.";
        final String[] HEADERS = new String[]{
                "rhyNr",
                "rhy",
                "kilometerExpenses",
                "dailyAllowances",
                "rhyExpenses",
                "totalExpenses",
                "bankAccount"
        };
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));

        excelHelper.appendRow().appendRow();
        headers.forEach(header -> excelHelper.appendTextCell(header, headerStyle));
    }

    private void createDataRows() {
        if (dtos.size() == 0) {
            dtos.add(GameDamageInspectionEventSummaryDTO.createEmpty());
        }

        dtos.forEach(dto -> {
            excelHelper.appendRow();

            final String officialCode = dto.getRhy() != null ? dto.getRhy().getOfficialCode() : " ";
            final String rhyName = dto.getRhy() != null ?
                    (Locales.isSwedish(locale) ? dto.getRhy().getNameSV() : dto.getRhy().getNameFI()) :
                    " ";
            final String iban = dto.getIban() != null ? dto.getIban().toFormattedString() : " ";

            excelHelper.appendTextCell(officialCode).withFont(tableFont).withBorders(NONE, NONE, THIN, THIN)
                    .appendTextCell(rhyName).withFont(tableFont).withBorders(NONE, NONE, THIN, THIN)
                    .appendBigDecimalCell(dto.getKilometerExpenses(), tableCellCurrencyStyle)
                    .appendBigDecimalCell(dto.getDailyAllowances(), tableCellCurrencyStyle)
                    .appendBigDecimalCell(dto.getRhyExpenses(), tableCellCurrencyStyle)
                    .appendBigDecimalCell(dto.getTotalExpenses(), tableCellCurrencyStyle)
                    .appendTextCell(iban).withFont(tableFont).withBorders(NONE, NONE, THIN, THIN);
        });
    }

    private void createSummaryRow() {
        final int TABLE_START_ROW = 8;

        excelHelper.appendRow();

        excelHelper.appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE)
                .appendTextCell(localiser.getTranslation("GameDamageInspectionEventExcelView.total"))
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableFont)
                // Kilometer expenses
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableFont)
                // Daily allowances
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableFont)
                // Rhy expenses
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableFont)
                // Total expenses
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableFont)
                .appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE);
    }

    private void adjustColumnWidths() {
        int column = 0;

        excelHelper.setColumnWidth(column++, 1700);

        excelHelper.setColumnWidth(column++, 7000);
        
        excelHelper.setColumnWidth(column++, 4000);

        excelHelper.setColumnWidth(column++, 2800);
        excelHelper.setColumnWidth(column++, 2800);

        excelHelper.setColumnWidth(column++, 4800);

        excelHelper.setColumnWidth(column++, 7000);
    }

    private void createSheet(final Workbook workbook){
        excelHelper = new ExcelHelper(workbook);

        createStyles();

        createApplicationTitleSection();

        createTableHeader();

        createDataRows();

        createSummaryRow();

        adjustColumnWidths();
    }
}
