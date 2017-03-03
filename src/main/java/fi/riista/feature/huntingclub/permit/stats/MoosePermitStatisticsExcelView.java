package fi.riista.feature.huntingclub.permit.stats;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class MoosePermitStatisticsExcelView extends AbstractXlsView {

    private static final DateTimeFormatter DATETIME_PATTERN = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String HEADER_PREFIX = "MoosePermitStatisticsExcel.";
    private static final String[] HEADERS = new String[]{
            "permitNumber", "permitHolder", "permitAmount",
            "adultMales", "adultFemales", "adults",
            "youngMales", "youngFemales", "young",
            "total", "permitUsedPercentage", "youngPercentage", "adultMalePercentage",
            "remainingPopulationInTotalArea", "remainingPopulationInEffectiveArea",
            "totalAreaSize", "effectiveAreaSize",
            "remainingPopulationInTotalAreaPer1000ha", "remainingPopulationInEffectiveAreaPer1000ha"
    };

    private final Locale locale;
    private final EnumLocaliser localiser;
    private final List<MoosePermitStatisticsDTO> stats;

    public MoosePermitStatisticsExcelView(final Locale locale,
                                          final EnumLocaliser localiser,
                                          final List<MoosePermitStatisticsDTO> stats) {
        this.locale = locale;
        this.localiser = localiser;
        this.stats = stats;
    }

    private static String createFilename() {
        return String.format("RHY_hirvielaintilasto_%s.xls", DATETIME_PATTERN.print(DateUtil.now()));
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        response.setHeader(ContentDispositionUtil.HEADER_NAME, ContentDispositionUtil.encodeAttachmentFilename(createFilename()));

        createSheet(workbook);
    }

    private void createSheet(final Workbook workbook) {

        final ExcelHelper helper = new ExcelHelper(workbook);
        helper.appendHeaderRow(createHeaderRow(HEADERS));

        for (MoosePermitStatisticsDTO stat : stats) {
            // this is summary row, should be the first dto
            if (stat.getPermitNumber() == null) {
                continue;
            }
            createRow(helper, stat);
        }

        helper.autoSizeColumns();
    }

    private void createRow(final ExcelHelper helper, final MoosePermitStatisticsDTO dto) {
        helper.appendRow();
        helper.appendTextCell(dto.getPermitNumber());
        helper.appendTextCell(dto.getPermitHolderLocalisedString().getAnyTranslation(locale.getLanguage()));
        helper.appendNumberCell(dto.getPermitAmount());

        final MoosePermitStatisticsCount count = dto.getHarvestCount();

        helper.appendNumberCell(count.getAdultMales());
        helper.appendNumberCell(count.getAdultFemales());
        helper.appendNumberCell(count.getAdults());

        helper.appendNumberCell(count.getYoungMales());
        helper.appendNumberCell(count.getYoungFemales());
        helper.appendNumberCell(count.getYoung());

        helper.appendNumberCell(count.getTotal());

        helper.appendNumberCell(100 * (count.getAdults() + count.getYoung() / 2) / dto.getPermitAmount());
        helper.appendNumberCell(count.getYoungPercentage());
        helper.appendNumberCell(count.getAdultMalePercentage());

        helper.appendNumberCell(count.getRemainingPopulationInTotalArea());
        helper.appendNumberCell(count.getRemainingPopulationInEffectiveArea());

        helper.appendNumberCell(count.getTotalAreaSize());
        helper.appendNumberCell(count.getEffectiveAreaSize());

        helper.appendNumberCell(count.getRemainingPopulationInTotalAreaPer1000ha());
        helper.appendNumberCell(count.getRemainingPopulationInEffectiveAreaPer1000ha());
    }

    private String[] createHeaderRow(String[] headers) {
        return Stream.of(headers)
                .map(key -> localiser.getTranslation(HEADER_PREFIX + key))
                .toArray(String[]::new);
    }
}
