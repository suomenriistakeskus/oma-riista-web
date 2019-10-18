package fi.riista.feature.harvestpermit.statistics;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
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
    private static final String[] HEADERS = new String[]{
            "speciesName", "rka", "rhy", "hta",
            "permitNumber", "permitHolder",
            "partnerCount", "totalLandAreaSize", "effectiveLandAreaSize", "permitLandAreaSize",
            "applicationPermitAmount", "applicationPermitAmountPer1000ha",
            "originalPermitAmount", "amendmentAmount",
            "totalPermitAmount", "totalPermitAmountPer1000ha",
            "usedPermitAmount", "usedPermitPercentage",
            "restrictionAdult", "restrictionAdultMale", "restrictedYoungPercentage",
            "adultMales", "adultFemales", "adults",
            "youngMales", "youngFemales", "young",
            "totalHarvest", "totalHarvestPer1000ha",
            "youngPercentage", "youngMalePercentage", "adultMalePercentage",
            "remainingPopulationInTotalArea", "remainingPopulationInEffectiveArea",
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
        return String.format("hirvielaintilasto_%s.xlsx", Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
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
            createRow(helper, stat);
        }

        helper.autoSizeColumns();
    }

    private void createRow(final ExcelHelper helper, final MoosePermitStatisticsDTO dto) {
        final MoosePermitStatisticsAmountDTO permitAmount = dto.getPermitAmount();
        final HarvestCountDTO harvestCount = dto.getHarvestCount();
        final MoosePermitStatisticsAreaAndPopulation areaAndPopulation = dto.getAreaAndPopulation();

        helper.appendRow()
                .appendTextCell(localiser.getTranslation(dto.getSpeciesName()))
                .appendTextCell(localiser.getTranslation(dto.getRka()))
                .appendTextCell(localiser.getTranslation(dto.getRhy()))
                .appendTextCell(localiser.getTranslation(dto.getHta()))

                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getPermitHolder()))
                .appendNumberCell(dto.getPartnerCount())
                .appendNumberCell(areaAndPopulation.getTotalAreaSize())
                .appendNumberCell(areaAndPopulation.getEffectiveAreaSize())
                .appendNumberCell(dto.getPermitLandAreaSize())

                .appendDoubleCell(permitAmount.getApplication(), 1)
                .appendDoubleCell(dto.getApplicationPermitsPer1000ha(), 2)

                .appendDoubleCell(permitAmount.getOriginal(), 1)
                .appendDoubleCell(permitAmount.getAmendment(), 1)
                .appendDoubleCell(permitAmount.getTotal(), 1)
                .appendDoubleCell(dto.getTotalPermitsPer1000ha(), 2)

                .appendDoubleCell(dto.getHarvestCount().getRequiredPermitAmount(), 1)
                .appendDoubleCell(dto.getUsedPermitPercentage(), 1)

                .appendNumberCell(permitAmount.getRestrictionAdult())
                .appendNumberCell(permitAmount.getRestrictionAdultMale())
                .appendDoubleCell(permitAmount.getRestrictedYoungPercentage(), 1)

                .appendNumberCell(harvestCount.getNumberOfAdultMales())
                .appendNumberCell(harvestCount.getNumberOfAdultFemales())
                .appendNumberCell(harvestCount.getNumberOfAdults())

                .appendNumberCell(harvestCount.getNumberOfYoungMales())
                .appendNumberCell(harvestCount.getNumberOfYoungFemales())
                .appendNumberCell(harvestCount.getNumberOfYoung())

                .appendNumberCell(harvestCount.getTotal())
                .appendDoubleCell(dto.getTotalHarvestPer1000ha(), 2)

                .appendDoubleCell(harvestCount.getYoungPercentage(), 1)
                .appendDoubleCell(harvestCount.getYoungMalePercentage(), 1)
                .appendDoubleCell(harvestCount.getAdultMalePercentage(), 1)

                .appendNumberCell(areaAndPopulation.getRemainingPopulationInTotalArea())
                .appendNumberCell(areaAndPopulation.getRemainingPopulationInEffectiveArea())

                .appendDoubleCell(areaAndPopulation.getRemainingPopulationInTotalAreaPer1000ha(), 2)
                .appendDoubleCell(areaAndPopulation.getRemainingPopulationInEffectiveAreaPer1000ha(), 2);
    }
}
