package fi.riista.feature.harvestpermit.report.excel;

import fi.riista.config.Constants;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.Localiser;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static fi.riista.util.F.mapNullable;

public class HarvestReportReviewExcelView extends AbstractXlsxView {
    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    public HarvestReportReviewExcelView(final List<HarvestReportReviewDTO> data, final String filename, final Localiser localiser) {
        this.data = data;
        this.filename = filename;
        this.localiser = localiser;
        this.rowHeaders = localiser.translate(LOCALISATION_PREFIX, HEADER_LOCALIZATION_KEYS);
    }

    public static HarvestReportReviewExcelView create(final Localiser localiser,
                                                      final List<HarvestReportReviewDTO> data) {
        final String filename = String.format("%s-%s.xlsx",
                StringUtils.uncapitalize(localiser.getTranslation(LOCALISATION_PREFIX + "harvestReportReview")),
                DATETIME_PATTERN.print(DateUtil.now()));

        return new HarvestReportReviewExcelView(data, filename, localiser);
    }

    private static final String LOCALISATION_PREFIX = "HarvestReportReviewExcelView.";
    /*package*/ static final String[] HEADER_LOCALIZATION_KEYS = new String[]{
            "harvestId", "calendarYear", "huntingYear", "species", "rka", "rhy", "permitType", "permitNumber",
            "partnerOfficialCode", "partner", "caughtDate", "caughtTime", "creationDate", "creationTime",
            "approveDate", "approveTime", "delayMinutes", "delayDays", "authorHunterNumber", "shooterHunterNumber",
            "createdByModerator"};

    private final List<HarvestReportReviewDTO> data;
    private final String filename;
    private final Localiser localiser;
    private final String[] rowHeaders;

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        final ExcelHelper excelHelper = new ExcelHelper(workbook).appendHeaderRow(rowHeaders);

        data.forEach(dto -> {

            final Minutes delayMinutes = Minutes.minutesBetween(dto.getPointOfTime(), dto.getCreationTime());
            final Days delayDays = Days.daysBetween(dto.getPointOfTime(), dto.getCreationTime());
            excelHelper.appendRow()
                    .appendNumberCell(dto.getId())
                    .appendNumberCell(dto.getPointOfTime().getYear())
                    .appendNumberCell(DateUtil.huntingYearContaining(dto.getPointOfTime().toLocalDate()))
                    .appendTextCell(localiser.getTranslation(dto.getSpecies()))
                    .appendTextCell(localiser.getTranslation(dto.getRkaName()))
                    .appendTextCell(localiser.getTranslation(dto.getRhyName()))
                    .appendTextCell(dto.getPermitType())
                    .appendTextCell(dto.getPermitNumber())
                    .appendTextCell(dto.getPartnerOfficialCode())
                    .appendTextCell(mapNullable(dto.getPartner(), localiser::getTranslation))
                    .appendDateCell(dto.getPointOfTime().toLocalDate())
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(dto.getPointOfTime().toLocalTime()))
                    .appendDateCell(dto.getCreationTime().toLocalDate())
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(dto.getCreationTime().toLocalTime()))
                    .appendDateCell(mapNullable(dto.getHarvestReportDate(), LocalDateTime::toLocalDate))
                    .appendTimeCell(DateUtil.toDateTodayNullSafe(mapNullable(dto.getCreationTime(), LocalDateTime::toLocalTime)))
                    .appendNumberCell(delayMinutes.getMinutes())
                    .appendNumberCell(delayDays.getDays())
                    .appendTextCell(dto.getAuthorHunterNumber())
                    .appendTextCell(dto.getShooterHunterNumber())
                    .appendBoolCell(dto.isCreatedByModerator());
        });

        excelHelper.autoSizeColumns();
    }
}
