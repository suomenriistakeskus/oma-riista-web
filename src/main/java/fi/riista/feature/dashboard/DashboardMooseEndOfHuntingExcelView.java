package fi.riista.feature.dashboard;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import fi.riista.util.NumberUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.DateUtil.now;
import static java.util.Objects.requireNonNull;

public class DashboardMooseEndOfHuntingExcelView extends AbstractXlsxView {
    private static final String LOCALISATION_PREFIX = "DashboardMooseEndOfHuntingExcelView.";

    private final List<DashboardHuntingSummaryDTO> summaryDTOS;
    private final Map<Long, PermitInfoDTO> permitInfoMap;
    private EnumLocaliser localiser;

    public DashboardMooseEndOfHuntingExcelView(final List<DashboardHuntingSummaryDTO> summaryDTOS,
                                               final Map<Long, PermitInfoDTO> permitInfoMap,
                                               final EnumLocaliser localiser) {
        this.summaryDTOS = summaryDTOS;
        this.permitInfoMap = permitInfoMap;
        this.localiser = localiser;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map, final Workbook workbook,
                                      final HttpServletRequest httpServletRequest,
                                      final HttpServletResponse httpServletResponse) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        httpServletResponse.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(httpServletResponse, createFilename());

        // Split data into four different sheets
        final ExcelHelper mooseSheet = appendMooseSheetWithHeaderRows(workbook);
        final ExcelHelper deerSheet = appendDeerSheetWithHeaderRows(workbook);
        final ExcelHelper otherSpeciesSheet = appendOtherSpeciesSheetWithHeaderRows(workbook);
        final ExcelHelper otherInfoSheet = appendOtherInfoSheetWithHeaderRows(workbook);

        // Append data
        summaryDTOS.forEach(dto -> {
            appendMooseData(mooseSheet, dto);
            appendDeerObservationData(deerSheet, dto);
            appendOtherSpeciesData(otherSpeciesSheet, dto);
            appendOtherObservationData(otherInfoSheet, dto);

        });

        mooseSheet.autoSizeColumns();
        deerSheet.autoSizeColumns();
        otherInfoSheet.autoSizeColumns();
        otherSpeciesSheet.autoSizeColumns();

    }

    // SHEETS WITH HEADER CELLS

    private ExcelHelper appendMooseSheetWithHeaderRows(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n("mooseObservationsTitle"));

        helper.appendRow()
                .appendEmptyCell(7)
                .appendTextCell(i18n("huntingArea")).spanCurrentColumn(4)
                .appendTextCell(i18n("remainingPopulation")).spanCurrentColumn(2)
                .appendTextCell(i18n("deadMooseTitle")).spanCurrentColumn(10);

        helper.appendRow()
                .appendTextCell(i18n("permitNumber"))
                .appendTextCell(i18n("huntingClub"))
                .appendTextCell(i18n("rhy"))
                .appendTextCell(i18n("rka"))
                .appendTextCell(i18n("latitude"))
                .appendTextCell(i18n("longitude"))
                .appendTextCell(i18n("endOfHuntingDate"))

                .appendTextCell(i18n("landArea"))
                .appendTextCell(i18n("effectiveArea"))
                .appendTextCell(i18n("effectiveAreaPercentage"))
                .appendTextCell(i18n("huntingAreaType"))

                .appendTextCell(i18n("remainingPopulationTotal"))
                .appendTextCell(i18n("remainingPopulationEffectiveArea"))

                .appendTextCell(i18n("numberOfDrownedMooses"))
                .appendTextCell(i18n("numberOfMoosesKilledByBear"))
                .appendTextCell(i18n("numberOfMoosesKilledByWolf"))
                .appendTextCell(i18n("numberOfMoosesKilledInTrafficAccident"))
                .appendTextCell(i18n("numberOfMoosesKilledByPoaching"))
                .appendTextCell(i18n("numberOfMoosesKilledInRutFight"))
                .appendTextCell(i18n("numberOfStarvedMooses"))
                .appendTextCell(i18n("numberOfMoosesDeceasedByOtherReason"))
                .appendTextCell(i18n("causeOfDeath"))
                .appendTextCell(i18n("totalKilled"));

        helper.withFreezePane(2, 2);
        return helper;
    }

    private ExcelHelper appendDeerSheetWithHeaderRows(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n("otherMooselikeSpeciesTitle"));

        helper.appendRow()
                .appendEmptyCell(6)
                .appendTextCell(i18n("fallowDeerTitle")).spanCurrentColumn(2)
                .appendTextCell(i18n("roeDeerTitle")).spanCurrentColumn(2)
                .appendTextCell(i18n("whiteTailedDeerTitle")).spanCurrentColumn(2)
                .appendTextCell(i18n("wildForestReindeerTitle")).spanCurrentColumn(2);

        helper.appendRow()
                .appendTextCell(i18n("permitNumber"))
                .appendTextCell(i18n("huntingClub"))
                .appendTextCell(i18n("rhy"))
                .appendTextCell(i18n("rka"))
                .appendTextCell(i18n("latitude"))
                .appendTextCell(i18n("longitude"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("estimatedAmountOfSpecimens"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("estimatedAmountOfSpecimens"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("estimatedAmountOfSpecimens"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("estimatedAmountOfSpecimens"));

        helper.withFreezePane(2, 2);
        return helper;
    }

    private ExcelHelper appendOtherSpeciesSheetWithHeaderRows(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n("otherSpeciesTitle"));

        helper.appendRow()
                .appendEmptyCell(6)
                .appendTextCell(i18n("wildBoarTitle")).spanCurrentColumn(3)
                .appendTextCell(i18n("beaverTitle")).spanCurrentColumn(6);

        helper.appendRow()
                .appendTextCell(i18n("permitNumber"))
                .appendTextCell(i18n("huntingClub"))
                .appendTextCell(i18n("rhy"))
                .appendTextCell(i18n("rka"))
                .appendTextCell(i18n("latitude"))
                .appendTextCell(i18n("longitude"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("estimatedAmountOfSpecimens"))
                .appendTextCell(i18n("estimatedAmountOfSowWithPiglets"))
                .appendTextCell(i18n("trendOfMooselikePopulationGrowth"))
                .appendTextCell(i18n("amountOfInhabitedWinterNests"))
                .appendTextCell(i18n("beaverHarvestAmount"))
                .appendTextCell(i18n("areaOfDamage"))
                .appendTextCell(i18n("areaOccupiedByWater"))
                .appendTextCell(i18n("additionalBeaverInfo"));

        helper.withFreezePane(2, 2);
        return helper;
    }

    private ExcelHelper appendOtherInfoSheetWithHeaderRows(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, i18n("otherObservationsTitle"));

        helper.appendRow().appendEmptyCell(6)
                .appendTextCell(i18n("mooseHeatTitle")).spanCurrentColumn(2)
                .appendTextCell(i18n("mooseFawnTitle")).spanCurrentColumn(2)
                .appendTextCell(i18n("deerFlyTitle")).spanCurrentColumn(5);

        helper.appendRow()
                .appendTextCell(i18n("permitNumber"))
                .appendTextCell(i18n("huntingClub"))
                .appendTextCell(i18n("rhy"))
                .appendTextCell(i18n("rka"))
                .appendTextCell(i18n("latitude"))
                .appendTextCell(i18n("longitude"))
                .appendTextCell(i18n("mooseHeatBeginDate"))
                .appendTextCell(i18n("mooseHeatEndDate"))
                .appendTextCell(i18n("mooseFawnBeginDate"))
                .appendTextCell(i18n("mooseFawnEndDate"))
                .appendTextCell(i18n("trendOfDeerFlyPopulationGrowth"))
                .appendTextCell(i18n("dateOfFirstDeerFlySeen"))
                .appendTextCell(i18n("dateOfLastDeerFlySeen"))
                .appendTextCell(i18n("numberOfAdultMoosesHavingFlies"))
                .appendTextCell(i18n("numberOfYoungMoosesHavingFlies"));

        helper.withFreezePane(2, 2);
        return helper;
    }

    // PARTNER DATA

    private void appendMooseData(final ExcelHelper helper, final DashboardHuntingSummaryDTO dto) {
        final PermitInfoDTO permitInfoDTO = requireNonNull(permitInfoMap.get(dto.getPermitId()));
        helper.appendRow()
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getClubName()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRhy()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRka()))
                .appendNumberCell(dto.getClubLatitude())
                .appendNumberCell(dto.getClubLongitude())
                .appendDateCell(dto.getHuntingEndDate())
                .appendNumberCell(dto.getTotalHuntingArea())
                .appendNumberCell(dto.getEffectiveHuntingArea())
                .appendNumberCell(dto.getEffectiveHuntingAreaPercentage())
                .appendTextCell(i18nEnum(dto.getHuntingAreaType()))
                .appendNumberCell(dto.getRemainingPopulationInTotalArea())
                .appendNumberCell(dto.getRemainingPopulationInEffectiveArea())

                .appendNumberCell(dto.getNumberOfDrownedMooses())
                .appendNumberCell(dto.getNumberOfMoosesKilledByBear())
                .appendNumberCell(dto.getNumberOfMoosesKilledByWolf())
                .appendNumberCell(dto.getNumberOfMoosesKilledInTrafficAccident())
                .appendNumberCell(dto.getNumberOfMoosesKilledByPoaching())
                .appendNumberCell(dto.getNumberOfMoosesKilledInRutFight())
                .appendNumberCell(dto.getNumberOfStarvedMooses())
                .appendNumberCell(dto.getNumberOfMoosesDeceasedByOtherReason())
                .appendTextCell(dto.getCauseOfDeath())
                .appendNumberCell(calculateSumOfDeadMoose(dto));
    }

    private void appendDeerObservationData(final ExcelHelper observationSheet, final DashboardHuntingSummaryDTO dto) {
        final PermitInfoDTO permitInfoDTO = requireNonNull(permitInfoMap.get(dto.getPermitId()));
        observationSheet.appendRow()
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getClubName()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRhy()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRka()))
                .appendNumberCell(dto.getClubLatitude())
                .appendNumberCell(dto.getClubLongitude());

        // Deer appearances
        appendSpeciesEstimatedAppearance(dto.getFallowDeerAppearance(), observationSheet);
        appendSpeciesEstimatedAppearance(dto.getRoeDeerAppearance(), observationSheet);
        appendSpeciesEstimatedAppearance(dto.getWhiteTailedDeerAppearance(), observationSheet);
        appendSpeciesEstimatedAppearance(dto.getWildForestReindeerAppearance(), observationSheet);

    }

    private void appendOtherSpeciesData(final ExcelHelper observationSheet, final DashboardHuntingSummaryDTO dto) {
        final PermitInfoDTO permitInfoDTO = requireNonNull(permitInfoMap.get(dto.getPermitId()));
        observationSheet.appendRow()
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getClubName()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRhy()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRka()))
                .appendNumberCell(dto.getClubLatitude())
                .appendNumberCell(dto.getClubLongitude());

        // Wild boar appearance
        Optional.ofNullable(dto.getWildBoarAppearance())
                .map(wildBoarAppearance ->
                        observationSheet
                                .appendTextCell(i18nEnum(wildBoarAppearance.getTrendOfPopulationGrowth()))
                                .appendNumberCell(wildBoarAppearance.getEstimatedAmountOfSpecimens())
                                .appendNumberCell(wildBoarAppearance.getEstimatedAmountOfSowWithPiglets()))
                .orElseGet(()->observationSheet.appendEmptyCell(3));

        // Beaver appearance
        Optional.ofNullable(dto.getBeaverAppearance())
                .map(beaverAppearance ->
                        observationSheet
                                .appendTextCell(i18nEnum(beaverAppearance.getTrendOfPopulationGrowth()))
                                .appendNumberCell(beaverAppearance.getAmountOfInhabitedWinterNests())
                                .appendNumberCell(beaverAppearance.getHarvestAmount())
                                .appendNumberCell(beaverAppearance.getAreaOfDamage())
                                .appendNumberCell(beaverAppearance.getAreaOccupiedByWater())
                                .appendTextCell(beaverAppearance.getAdditionalInfo()))
                .orElseGet(()->observationSheet.appendEmptyCell(6));
    }

    private void appendOtherObservationData(final ExcelHelper otherObservationSheet,
                                            final DashboardHuntingSummaryDTO dto) {
        final PermitInfoDTO permitInfoDTO = requireNonNull(permitInfoMap.get(dto.getPermitId()));
        otherObservationSheet.appendRow()
                .appendTextCell(dto.getPermitNumber())
                .appendTextCell(localiser.getTranslation(dto.getClubName()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRhy()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRka()))
                .appendNumberCell(dto.getClubLatitude())
                .appendNumberCell(dto.getClubLongitude());

        otherObservationSheet.appendDateCell(dto.getMooseHeatBeginDate())
                .appendDateCell(dto.getMooseHeatEndDate())
                .appendDateCell(dto.getMooseFawnBeginDate())
                .appendDateCell(dto.getMooseFawnEndDate());

        otherObservationSheet.appendTextCell(i18nEnum(dto.getTrendOfDeerFlyPopulationGrowth()))
                .appendDateCell(dto.getDateOfFirstDeerFlySeen())
                .appendDateCell(dto.getDateOfLastDeerFlySeen())
                .appendNumberCell(dto.getNumberOfAdultMoosesHavingFlies())
                .appendNumberCell(dto.getNumberOfYoungMoosesHavingFlies());
    }

    private void appendSpeciesEstimatedAppearance(final SpeciesEstimatedAppearance appearanceEntity,
                                                  final ExcelHelper helper) {
        Optional.ofNullable(appearanceEntity)
                .map(appearance ->
                        helper.appendTextCell(i18nEnum(appearance.getTrendOfPopulationGrowth()))
                                .appendNumberCell(appearance.getEstimatedAmountOfSpecimens()))
                .orElseGet(() -> helper.appendEmptyCell(2));
    }

    private static Integer calculateSumOfDeadMoose(final DashboardHuntingSummaryDTO dto) {
        return NumberUtils.nullableIntSum(
                dto.getNumberOfDrownedMooses(),
                dto.getNumberOfMoosesKilledByBear(),
                dto.getNumberOfMoosesKilledByWolf(),
                dto.getNumberOfMoosesKilledInTrafficAccident(),
                dto.getNumberOfMoosesKilledByPoaching(),
                dto.getNumberOfMoosesKilledInRutFight(),
                dto.getNumberOfStarvedMooses(),
                dto.getNumberOfMoosesDeceasedByOtherReason());
    }


    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n("title"), Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String i18nEnum(final Enum<?> value) {
        return localiser.getTranslation(EnumLocaliser.resourceKey(value));
    }

    private String i18n(final String key) {
        return localiser.getTranslation(LOCALISATION_PREFIX + key);
    }

}
