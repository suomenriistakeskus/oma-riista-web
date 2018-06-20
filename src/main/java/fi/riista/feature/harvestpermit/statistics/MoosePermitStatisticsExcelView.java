package fi.riista.feature.harvestpermit.statistics;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class MoosePermitStatisticsExcelView extends AbstractXlsxView {

    private static final String HEADER_PREFIX = "MoosePermitStatisticsExcel.";
    private static final String[] HEADERS = new String[] {
            "permitNumber", "permitHolder", "permitAmount",
            "adultMales", "adultFemales", "adults",
            "youngMales", "youngFemales", "young",
            "total", "permitUsedPercentage", "youngPercentage", "adultMalePercentage",
            "remainingPopulationInTotalArea", "remainingPopulationInEffectiveArea",
            "totalAreaSize", "effectiveAreaSize",
            "remainingPopulationInTotalAreaPer1000ha", "remainingPopulationInEffectiveAreaPer1000ha"
    };

    private final EnumLocaliser localiser;
    private final List<MoosePermitStatisticsDTO> stats;

    public MoosePermitStatisticsExcelView(final EnumLocaliser localiser,
                                          final List<MoosePermitStatisticsDTO> stats) {
        this.localiser = localiser;
        this.stats = stats;
    }

    private static String createFilename() {
        return String.format("RHY_hirvielaintilasto_%s.xlsx", Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
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

    private void createSheet(final Workbook workbook) {

        final ExcelHelper helper = new ExcelHelper(workbook)
                .appendHeaderRow(localiser.translate(HEADER_PREFIX, HEADERS));

        for (final MoosePermitStatisticsDTO stat : stats) {
            // this is summary row, should be the first dto
            if (stat.getPermitNumber() == null) {
                continue;
            }
            createRow(helper, stat);
        }

        helper.autoSizeColumns();
    }

    private void createRow(final ExcelHelper helper, final MoosePermitStatisticsDTO dto) {
        final MoosePermitStatisticsCount count = dto.getHarvestCount();

        helper.appendRow()
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getPermitHolderLocalisedString()))
                .appendNumberCell(dto.getPermitAmount())

                .appendNumberCell(count.getAdultMales())
                .appendNumberCell(count.getAdultFemales())
                .appendNumberCell(count.getAdults())

                .appendNumberCell(count.getYoungMales())
                .appendNumberCell(count.getYoungFemales())
                .appendNumberCell(count.getYoung())

                .appendNumberCell(count.getTotal())

                .appendNumberCell(100 * (count.getAdults() + count.getYoung() / 2) / dto.getPermitAmount())
                .appendNumberCell(count.getYoungPercentage())
                .appendNumberCell(count.getAdultMalePercentage())

                .appendNumberCell(count.getRemainingPopulationInTotalArea())
                .appendNumberCell(count.getRemainingPopulationInEffectiveArea())

                .appendNumberCell(count.getTotalAreaSize())
                .appendNumberCell(count.getEffectiveAreaSize())

                .appendNumberCell(count.getRemainingPopulationInTotalAreaPer1000ha())
                .appendNumberCell(count.getRemainingPopulationInEffectiveAreaPer1000ha());
    }
}
