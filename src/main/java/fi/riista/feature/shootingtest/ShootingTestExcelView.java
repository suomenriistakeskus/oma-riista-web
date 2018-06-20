package fi.riista.feature.shootingtest;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.shootingtest.ShootingTestStatisticsRowDTO.TestTypeStatisticsDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static fi.riista.util.DateUtil.now;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.capitalize;

public class ShootingTestExcelView extends AbstractXlsxView {

    private static final String HEADER_PREFIX = "ShootingTestExcel.";

    private final EnumLocaliser localiser;
    private final int calendarYear;
    private final LocalisedString rhyName;
    private final ShootingTestStatisticsDTO statistics;

    public ShootingTestExcelView(final EnumLocaliser localiser,
                                 final int calendarYear,
                                 final LocalisedString rhyName,
                                 final ShootingTestStatisticsDTO statistics) {

        this.localiser = localiser;
        this.calendarYear = calendarYear;
        this.rhyName = rhyName;
        this.statistics = statistics;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        createSheet(workbook);
    }

    private String createFilename() {
        return String.format(
                "Ampumakokeet_%s_%s_%s.xlsx",
                calendarYear,
                localiser.getTranslation(rhyName),
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String getTranslation(final String localisationKey) {
        return localiser.getTranslation(HEADER_PREFIX + localisationKey);
    }

    private void createSheet(final Workbook workbook) {
        final String sheetName = getTranslation("sheetName") + " " + calendarYear;
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);

        createTopRow(helper);
        createValueRows(helper, F.stream(statistics.getSummary(), statistics.getEventStatistics()).collect(toList()));

        helper.autoSizeColumns();
    }

    private void createTopRow(final ExcelHelper helper) {
        final String allForSelectedYear = getTranslation("all") + " " + calendarYear;

        helper.appendRow().appendEmptyCell(1).appendTextCell(allForSelectedYear, HorizontalAlignment.RIGHT);

        statistics.getEventStatistics().stream()
                .map(e -> e.getCalendarEvent().getDate())
                .forEach(helper::appendDateCell);
    }

    private void createValueRows(final ExcelHelper helper, final List<ShootingTestStatisticsRowDTO> list) {
        for (ShootingTestType testType : ShootingTestType.values()) {
            helper.appendRow();
            append(helper, list, testType, "total", TestTypeStatisticsDTO::getTotal, helper::appendNumberCell);
            append(helper, list, testType, "qualified", TestTypeStatisticsDTO::getQualified, helper::appendNumberCell);
            append(helper, list, testType, "unqualified", t -> t.getTotal() - t.getQualified(), helper::appendNumberCell);
            append(helper, list, testType, "qualifiedPercentage", TestTypeStatisticsDTO::getQualifiedPercentage, helper::appendPercentageCell);
            append(helper, list, testType, "totalDueAmount", TestTypeStatisticsDTO::getDueAmount, helper::appendCurrencyCell);
        }

        appendTotal(helper, list, "totalDueAmount", ShootingTestStatisticsRowDTO::getDueAmount);
        appendTotal(helper, list, "totalPaidAmount", ShootingTestStatisticsRowDTO::getPaid);
    }

    private <N extends Number> void append(final ExcelHelper helper,
                                           final List<ShootingTestStatisticsRowDTO> list,
                                           final ShootingTestType testType,
                                           final String localisationKey,
                                           final Function<TestTypeStatisticsDTO, N> extractor,
                                           final Consumer<N> appender) {

        helper.appendTextCell(getTranslation(toLowerCamelCase(testType) + capitalize(localisationKey)));

        for (ShootingTestStatisticsRowDTO dto : list) {
            appender.accept(extractor.apply(dto.getTestTypes().getOrDefault(testType, TestTypeStatisticsDTO.EMPTY)));
        }
        helper.appendRow();
    }

    private void appendTotal(final ExcelHelper helper,
                             final List<ShootingTestStatisticsRowDTO> list,
                             final String localisationKey,
                             final Function<ShootingTestStatisticsRowDTO, BigDecimal> extractor) {
        helper.appendRow();
        helper.appendTextCell(getTranslation(localisationKey));
        for (ShootingTestStatisticsRowDTO dto : list) {
            helper.appendCurrencyCell(extractor.apply(dto));
        }
    }

    private static String toLowerCamelCase(final ShootingTestType testType) {
        switch (testType) {
            case BEAR:
                return "bear";
            case BOW:
                return "bow";
            case MOOSE:
                return "moose";
            case ROE_DEER:
                return "roeDeer";
            default:
                throw new IllegalArgumentException("Unsupported ShootingTestType");
        }
    }
}
