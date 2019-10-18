package fi.riista.feature.organization.rhy.annualstats.statechange;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsState;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTime.now;

public class AnnualStatisticsSendersExcelView extends AbstractXlsxView {

    private final int calendarYear;
    private final List<RhyAnnualStatisticsProgressDTO> annualStatisticsProgressList;
    private final Locale locale;
    private final EnumLocaliser localiser;
    private final RhyAnnualStatisticsState statisticsState;

    public AnnualStatisticsSendersExcelView(final int calendarYear,
                                            @Nonnull final List<RhyAnnualStatisticsProgressDTO> annualStatisticsProgressList,
                                            @Nonnull final Locale locale,
                                            @Nonnull final EnumLocaliser localiser,
                                            @Nonnull final RhyAnnualStatisticsState statisticsState) {
        this.calendarYear = calendarYear;
        this.annualStatisticsProgressList = annualStatisticsProgressList;
        this.locale = locale;
        this.localiser = localiser;
        this.statisticsState = statisticsState;
    }

    @Override
    protected final void buildExcelDocument(final Map<String, Object> map,
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
                "%s_%s_%s_%s.xlsx",
                localiser.getTranslation("annualStatistics"),
                localiser.getTranslation("AnnualStatisticsSenderExcel.senders"),
                calendarYear,
                Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private void createSheet(final Workbook workbook) {
        final String sheetName = localiser.getTranslation("AnnualStatisticsSenderExcel.senders") + " " + calendarYear;
        final ExcelHelper helper = new ExcelHelper(workbook, sheetName);
        final List<String> titleStrings;
        if(statisticsState == RhyAnnualStatisticsState.UNDER_INSPECTION) {
            titleStrings = Arrays.asList("rhyNumber", "rhy", "AnnualStatisticsSenderExcel.time", "fullNameOfAuthor", "AnnualStatisticsSenderExcel.readyForApproval");
        } else {
            titleStrings = Arrays.asList("rhyNumber", "rhy", "AnnualStatisticsSenderExcel.time", "fullNameOfAuthor");
        }

        List<String> titles = titleStrings.stream()
                .map(item -> localiser.getTranslation(item))
                .collect(toList());
        helper.appendHeaderRow(titles);

        for (RhyAnnualStatisticsProgressDTO progressDTO : annualStatisticsProgressList) {
            helper.appendRow();

            helper.appendTextCell(progressDTO.getRhyCode())
                    .appendTextCell(progressDTO.getRhyName().get(locale.getLanguage()))
                    .appendDateTimeCell(progressDTO.getSubmitEvent().getEventTime().toLocalDateTime())
                    .appendTextCell(progressDTO.getSubmitEvent().getActor().getFullName());

            if (statisticsState == RhyAnnualStatisticsState.UNDER_INSPECTION) {
                String completeText = progressDTO.isCompleteForApproval() ?
                        localiser.getTranslation("Boolean.true") :
                        localiser.getTranslation("Boolean.false");
                helper.appendTextCell(completeText);
            }
        }

        helper.autoSizeColumns();
    }

}
