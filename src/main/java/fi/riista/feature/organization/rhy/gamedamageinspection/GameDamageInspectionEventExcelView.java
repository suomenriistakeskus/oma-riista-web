package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Locales;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.util.DateUtil.now;
import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER;
import static org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT;
import static org.apache.poi.ss.usermodel.VerticalAlignment.TOP;

public class GameDamageInspectionEventExcelView extends AbstractXlsxView {

    private ExcelHelper excelHelper;

    private Locale locale;
    private EnumLocaliser localiser;
    private GameDamageInspectionEventExportDTO exportDTO;

    private CellStyle titleStyle;

    private CellStyle headerStyleTopLeft;
    private CellStyle headerStyleCenter;

    private CellStyle tableCellCurrencyStyle;
    private CellStyle tableCellCurrencyStyleGray;
    private CellStyle sumColumnStyleGray;
    private CellStyle tableCellCurrencyStyleGreen;
    private CellStyle sumColumnStyleGreen;
    private CellStyle tableCellNumberStyle;

    private Font tableCellFont;
    private Font titleFont;

    public GameDamageInspectionEventExcelView(final Locale locale,
                                              final EnumLocaliser localiser,
                                              final GameDamageInspectionEventExportDTO exportDTO) {
        this.locale = locale;
        this.localiser = localiser;
        this.exportDTO = exportDTO;
    }

    private String createFilename() {
        return String.format("%s-%s-%s.xlsx",
                localiser.getTranslation("GameDamageInspectionEventExcelView.gameDamageInspectionEvent"),
                exportDTO.getGameDamageType() == GameDamageType.MOOSELIKE ?
                        localiser.getTranslation("GameDamageInspectionEventExcelView.GAME_TYPE_MOOSELIKE") :
                        localiser.getTranslation("GameDamageInspectionEventExcelView.GAME_TYPE_LARGE_CARNIVORE"),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createStyles() {

        final Workbook wb = excelHelper.getSheet().getWorkbook();

        final XSSFColor green = new XSSFColor(new java.awt.Color(235, 242, 221));
        final XSSFColor gray = new XSSFColor(new java.awt.Color(242, 242, 242));

        this.tableCellFont = wb.createFont();
        this.tableCellFont.setFontHeightInPoints((short)10);
        this.tableCellFont.setFontName("Arial Narrow");

        this.titleFont = wb.createFont();
        this.titleFont.setFontHeightInPoints((short)12);
        this.titleFont.setFontName("Arial Narrow");

        this.titleStyle = wb.createCellStyle();
        this.titleStyle.setFont(titleFont);

        this.headerStyleTopLeft = wb.createCellStyle();
        ((XSSFCellStyle)this.headerStyleTopLeft).setFillForegroundColor(green);
        this.headerStyleTopLeft.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.headerStyleTopLeft.setBorderTop(THIN);
        this.headerStyleTopLeft.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleTopLeft.setBorderBottom(THIN);
        this.headerStyleTopLeft.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleTopLeft.setBorderLeft(THIN);
        this.headerStyleTopLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleTopLeft.setBorderRight(THIN);
        this.headerStyleTopLeft.setRightBorderColor(IndexedColors.BLACK.getIndex());

        this.headerStyleTopLeft.setFont(tableCellFont);
        this.headerStyleTopLeft.setWrapText(true);

        this.headerStyleTopLeft.setAlignment(LEFT);
        this.headerStyleTopLeft.setVerticalAlignment(TOP);

        this.headerStyleCenter = wb.createCellStyle();
        ((XSSFCellStyle)this.headerStyleCenter).setFillForegroundColor(green);
        this.headerStyleCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        this.headerStyleCenter.setBorderTop(THIN);
        this.headerStyleCenter.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleCenter.setBorderBottom(THIN);
        this.headerStyleCenter.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleCenter.setBorderLeft(THIN);
        this.headerStyleCenter.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.headerStyleCenter.setBorderRight(THIN);
        this.headerStyleCenter.setRightBorderColor(IndexedColors.BLACK.getIndex());

        this.headerStyleCenter.setFont(tableCellFont);

        this.headerStyleCenter.setAlignment(CENTER);
        this.headerStyleCenter.setVerticalAlignment(TOP);

        this.tableCellCurrencyStyle = wb.createCellStyle();
        this.tableCellCurrencyStyle.setFont(this.tableCellFont);
        this.tableCellCurrencyStyle.setBorderLeft(THIN);
        this.tableCellCurrencyStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyle.setBorderRight(THIN);
        this.tableCellCurrencyStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));

        this.tableCellCurrencyStyleGreen = wb.createCellStyle();
        this.tableCellCurrencyStyleGreen.setFont(this.tableCellFont);
        this.tableCellCurrencyStyleGreen.setBorderLeft(THIN);
        this.tableCellCurrencyStyleGreen.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyleGreen.setBorderRight(THIN);
        this.tableCellCurrencyStyleGreen.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyleGreen.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        ((XSSFCellStyle)this.tableCellCurrencyStyleGreen).setFillForegroundColor(green);
        this.tableCellCurrencyStyleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.sumColumnStyleGreen = wb.createCellStyle();
        this.sumColumnStyleGreen.setFont(this.tableCellFont);
        this.sumColumnStyleGreen.setBorderTop(THIN);
        this.sumColumnStyleGreen.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGreen.setBorderBottom(THIN);
        this.sumColumnStyleGreen.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGreen.setBorderLeft(THIN);
        this.sumColumnStyleGreen.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGreen.setBorderRight(THIN);
        this.sumColumnStyleGreen.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGreen.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        ((XSSFCellStyle)this.sumColumnStyleGreen).setFillForegroundColor(green);
        this.sumColumnStyleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.tableCellCurrencyStyleGray = wb.createCellStyle();
        this.tableCellCurrencyStyleGray.setFont(this.tableCellFont);
        this.tableCellCurrencyStyleGray.setBorderLeft(THIN);
        this.tableCellCurrencyStyleGray.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyleGray.setBorderRight(THIN);
        this.tableCellCurrencyStyleGray.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellCurrencyStyleGray.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        ((XSSFCellStyle)this.tableCellCurrencyStyleGray).setFillForegroundColor(gray);
        this.tableCellCurrencyStyleGray.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.sumColumnStyleGray = wb.createCellStyle();
        this.sumColumnStyleGray.setFont(this.tableCellFont);
        this.sumColumnStyleGray.setBorderTop(THIN);
        this.sumColumnStyleGray.setTopBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGray.setBorderBottom(THIN);
        this.sumColumnStyleGray.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGray.setBorderLeft(THIN);
        this.sumColumnStyleGray.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGray.setBorderRight(THIN);
        this.sumColumnStyleGray.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.sumColumnStyleGray.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        ((XSSFCellStyle)this.sumColumnStyleGray).setFillForegroundColor(gray);
        this.sumColumnStyleGray.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        this.tableCellNumberStyle = wb.createCellStyle();
        this.tableCellNumberStyle.setFont(this.tableCellFont);
        this.tableCellNumberStyle.setBorderLeft(THIN);
        this.tableCellNumberStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellNumberStyle.setBorderRight(THIN);
        this.tableCellNumberStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        this.tableCellNumberStyle.setDataFormat(wb.createDataFormat().getFormat("#"));
    }

    private void createApplicationTitleSection() {
        excelHelper.appendRow().appendRow();

        final String applicationTitleFirstRow = exportDTO.getGameDamageType() == GameDamageType.MOOSELIKE ?
                localiser.getTranslation("GameDamageInspectionEventExcelView.TITLE_FIRST_ROW_MOOSELIKE") :
                localiser.getTranslation("GameDamageInspectionEventExcelView.TITLE_FIRST_ROW_LARGE_CARNIVORE");
        final String applicationDuration = "1.1.-31.12." + exportDTO.getYear();
        final String applicationGameType = exportDTO.getGameDamageType() == GameDamageType.MOOSELIKE ?
                localiser.getTranslation("GameDamageInspectionEventExcelView.GAME_TYPE_MOOSELIKE") :
                localiser.getTranslation("GameDamageInspectionEventExcelView.GAME_TYPE_LARGE_CARNIVORE");

        excelHelper.appendTextCell(applicationTitleFirstRow, titleStyle)
                .spanCurrentColumn(5)
                .appendEmptyCell(5)
                .appendTextCell(applicationDuration, titleStyle)
                .appendEmptyCell(1)
                .appendTextCell(applicationGameType, titleStyle);

        excelHelper.appendRow();

        final String applicationTitleSecondRow = exportDTO.getGameDamageType() == GameDamageType.MOOSELIKE ?
                localiser.getTranslation("GameDamageInspectionEventExcelView.TITLE_SECOND_ROW_MOOSELIKE") :
                localiser.getTranslation("GameDamageInspectionEventExcelView.TITLE_SECOND_ROW_LARGE_CARNIVORE");
        excelHelper.appendTextCell(applicationTitleSecondRow, titleStyle)
                .spanCurrentColumn(5);
    }

    private void createUserDetails() {
        excelHelper.appendRow().appendRow();

        excelHelper.appendTextCell(localiser.getTranslation("GameDamageInspectionEventExcelView.applicant"), titleStyle)
                .appendTextCell(exportDTO.getRhyName().getTranslation(locale), titleStyle)
                .spanCurrentColumnWithBottomBorder(3, THIN);

        excelHelper.appendRow().appendRow();

        final String iban =
                exportDTO.getBankAccount() != null ? exportDTO.getBankAccount().getIban().toFormattedString() : " ";
        final String bic =
                exportDTO.getBankAccount() != null ? exportDTO.getBankAccount().getBic().toString() : " ";
        excelHelper.appendTextCell(localiser.getTranslation("GameDamageInspectionEventExcelView.bank"), titleStyle)
                .appendTextCell(iban, titleStyle).spanCurrentColumnWithBottomBorder(3, THIN)
                .appendTextCell("BIC", HorizontalAlignment.RIGHT)
                .appendTextCell(bic, titleStyle).spanCurrentColumnWithBottomBorder(2, THIN);
    }

    private void createTableHeader() {
        final String HEADER_PREXIX = "GameDamageInspectionEventExcelView.";
        final String[] HEADERS = new String[]{
                "inspectorName",
                "date",
                "gameSpecies",
                "location",
                "eventDuration",
                "reward",
                "totalTravelExpenses",
                "totalExpenses",
                "beginTime",
                "endTime",
                "duration",
                "hourlyExpenses",
                "rhyExpenses",
                "kilometers",
                "kilometerExpenses",
                "dailyAllowance",
                "travelExpenses"
        };
        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREXIX, HEADERS));
        int headerIndex = 0;

        excelHelper.appendRow().appendRow();

        // Top title row
        // inspectorName, date, gameSpecies, location, eventDuration, reward, totalTravelExpenses, totalExpenses
        excelHelper.appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++), headerStyleCenter).spanCurrentColumnBordered(3)
                .appendTextCell(headers.get(headerIndex++), headerStyleCenter).spanCurrentColumnBordered(2)
                .appendTextCell(headers.get(headerIndex++), headerStyleCenter).spanCurrentColumnBordered(4)
                .appendTextCell(headers.get(headerIndex++));

        // Empty cells for merging and styles
        excelHelper.appendRow()
                .appendEmptyCell(4);

        // Second title row + empty cell for merging and styles
        // beginTime, endTime, duration, hourlyExpenses, rhyExpenses, kilometers, kilometerExpenses, dailyAllowance, travelExpenses
        excelHelper.appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendTextCell(headers.get(headerIndex++))
                .appendEmptyCell(1);

        // Empty cells for merging and styles
        for (int j = 0; j < 2; j++) {
            excelHelper.appendRow();
            excelHelper.appendEmptyCell(14);
        }

        // Create 4 merged and styled 4 rows x 1 column cells
        excelHelper.addMergedRegion(8, 11, 0, 0)
                .addMergedRegion(8, 11, 1, 1)
                .addMergedRegion(8, 11, 2, 2)
                .addMergedRegion(8, 11, 3, 3)
                .addCellStyleForRegion(headerStyleTopLeft, 8, 11, 0, 3);

        // Create 9 merged and styled 3 rows x 1 column cells
        excelHelper.addMergedRegion(9, 11, 4, 4)
                .addMergedRegion(9, 11, 5, 5)
                .addMergedRegion(9, 11, 6, 6)
                .addMergedRegion(9, 11, 7, 7)
                .addMergedRegion(9, 11, 8, 8)
                .addMergedRegion(9, 11, 9, 9)
                .addMergedRegion(9, 11, 10, 10)
                .addMergedRegion(9, 11, 11, 11)
                .addMergedRegion(9, 11, 12, 12)
                .addCellStyleForRegion(headerStyleTopLeft, 9, 11, 4, 12);

        // Create merged and styled 4 rows x 1 column cell
        excelHelper.addMergedRegion(8, 11, 13, 13)
                .addCellStyleForRegion(headerStyleTopLeft, 8, 11, 13, 13);
    }

    private void createDataRows() {
        final List<GameDamageInspectionEventDTO> dtos = exportDTO.getDtos();
        if (dtos.size() == 0) {
            // Create empty sheet with working formulas
            dtos.add(GameDamageInspectionEventDTO.createEmpty());
        }

        dtos.forEach(dto -> {
            excelHelper.appendRow();

            final GameSpecies species = exportDTO.getIdToGameSpecies().get(dto.getId());
            final String speciesName = species != null ?
                    (Locales.isSwedish(locale) ? species.getNameSwedish() : species.getNameFinnish()) :
                    "";
            final String location = dto.getGeoLocation() != null ?
                    dto.getGeoLocation().getLatitude() + " / " + dto.getGeoLocation().getLongitude() :
                    "";

            excelHelper.appendTextCell(dto.getInspectorName()).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)
                    .appendDateCell(dto.getDate()).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)
                    .appendTextCell(speciesName).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)
                    .appendTextCell(location).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont);

            if (dto.getDate() != null) {
                excelHelper.appendTimeCell(dto.getDate().toLocalDateTime(dto.getBeginTime()).toDate())
                        .withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)
                        .appendTimeCell(dto.getDate().toLocalDateTime(dto.getEndTime()).toDate())
                        .withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont);
            } else {
                excelHelper.appendEmptyCell(1).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)
                        .appendEmptyCell(1).withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont);
            }

            final Integer kilometers = dto.getGameDamageInspectionKmExpenses().stream()
                    .map(expense -> expense.getKilometers())
                    .mapToInt(Integer::intValue).sum();
            final BigDecimal kilometerExpenses = dto.getGameDamageInspectionKmExpenses().stream()
                    .map(expense -> expense.getExpenseUnit().multiply(BigDecimal.valueOf(expense.getKilometers())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Duration in hours (rounded down to full hours)
            excelHelper.appendFormula("FLOOR(ROUND((%s-%s)*24,3),1)", -1, -2)
                    .withBorders(NONE, NONE, THIN, THIN).withFont(tableCellFont)

                    .appendBigDecimalCell(dto.getHourlyExpensesUnit(), tableCellCurrencyStyle)
                    // Rhy paid expenses
                    .appendFormula("%s*%s", tableCellCurrencyStyleGray, -1, -2)

                    .appendNumberCell(kilometers).withFont(tableCellFont)
                    // Kilometer expenses
                    .appendBigDecimalCell(kilometerExpenses, tableCellCurrencyStyle)

                    .appendBigDecimalCell(dto.getDailyAllowance(), tableCellCurrencyStyle)
                    // Total travel expenses (kilometer expenses + daily allowance
                    .appendFormula("%s+%s", tableCellCurrencyStyleGray, -2, -1)

                    // Total expenses (Rhy expenses + total travel expenses)
                    .appendFormula("%s+%s", tableCellCurrencyStyleGreen, -5, -1);
        });
    }

    public void createSummaryRow() {
        final int TABLE_START_ROW = 12;

        excelHelper.appendRow();

        excelHelper.appendTextCell(localiser.getTranslation("GameDamageInspectionEventExcelView.total"))
                .withBorders(THIN, THIN, THIN, THIN).withFont(tableCellFont);

        for (int i = 0; i < 5; i++) {
            excelHelper.appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE);
        }

        // Duration
        excelHelper.appendColumnSummationFrom(TABLE_START_ROW, tableCellNumberStyle)
                .withBorders(THIN, THIN, THIN, THIN)
                .appendEmptyCell(1).withBorders(THIN, NONE, NONE, NONE)
                // Rhy paid expenses
                .appendColumnSummationFrom(TABLE_START_ROW, sumColumnStyleGray)
                // Kilometers
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellNumberStyle)
                .withBorders(THIN, THIN, THIN, THIN)
                // Kilometer expenses
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN)
                // Daily allowances
                .appendColumnSummationFrom(TABLE_START_ROW, tableCellCurrencyStyle)
                .withBorders(THIN, THIN, THIN, THIN)
                // Total travel expenses
                .appendColumnSummationFrom(TABLE_START_ROW, sumColumnStyleGray)
                // Total expenses
                .appendColumnSummationFrom(TABLE_START_ROW, sumColumnStyleGreen);
    }

    private void adjustColumnWidths() {
        int column = 0;

        excelHelper.setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 2800)
                .setColumnWidth(column++, 5200)
                .setColumnWidth(column++, 5200);
    }

    private void createSheet(final Workbook workbook) {
        excelHelper = new ExcelHelper(workbook);

        createStyles();

        createApplicationTitleSection();

        createUserDetails();

        createTableHeader();

        createDataRows();

        createSummaryRow();

        excelHelper.appendRow().appendRow()
                .appendTextCell(localiser.getTranslation("GameDamageInspectionEventExcelView.APPLICATION_ASTERISK"))
                .withFont(tableCellFont)
                .spanCurrentColumn(6);

        adjustColumnWidths();
    }

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
}
