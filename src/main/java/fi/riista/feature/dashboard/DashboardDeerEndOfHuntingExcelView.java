package fi.riista.feature.dashboard;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoDTO;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static fi.riista.util.DateUtil.now;
import static java.util.Objects.requireNonNull;

public class DashboardDeerEndOfHuntingExcelView extends AbstractXlsxView {
    private static final String LOCALISATION_PREFIX = "DashboardDeerEndOfHuntingExcelView.";

    private final List<ClubHuntingSummaryBasicInfoDTO> summaryDTOS;
    private final Map<Long, String> permitIdToNumber;
    private final Map<Long, HuntingClubInfoDTO> clubIdToInfo;
    private final Map<Long, PermitInfoDTO> permitIdToInfo;
    private final EnumLocaliser localiser;

    public DashboardDeerEndOfHuntingExcelView(final List<ClubHuntingSummaryBasicInfoDTO> summaryDTOS,
                                              final Map<Long, String> permitIdToNumber,
                                              final Map<Long, HuntingClubInfoDTO> clubIdToInfo,
                                              final Map<Long, PermitInfoDTO> permitIdToInfo,
                                              final EnumLocaliser localiser) {
        this.summaryDTOS = summaryDTOS;
        this.permitIdToNumber = permitIdToNumber;
        this.clubIdToInfo = clubIdToInfo;
        this.permitIdToInfo = permitIdToInfo;
        this.localiser = localiser;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map, final Workbook workbook,
                                      final HttpServletRequest httpServletRequest,
                                      final HttpServletResponse httpServletResponse) throws Exception {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        httpServletResponse.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(httpServletResponse, createFilename());

        final ExcelHelper deerSheet = appendDeerSheetWithHeaderRows(workbook);

        // Append data
        summaryDTOS.forEach(dto -> {
            appendMooseData(deerSheet, dto);

        });

        deerSheet.autoSizeColumns();

    }

    // SHEETS WITH HEADER CELLS

    private ExcelHelper appendDeerSheetWithHeaderRows(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook);

        helper.appendRow()
                .appendEmptyCell(7)
                .appendTextCell(i18n("huntingArea")).spanCurrentColumn(2)
                .appendTextCell(i18n("remainingPopulation")).spanCurrentColumn(2);

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
                .appendTextCell(i18n("remainingPopulationTotal"))
                .appendTextCell(i18n("remainingPopulationEffectiveArea"));

        helper.withFreezePane(2, 2);
        return helper;
    }


    // PARTNER DATA

    private void appendMooseData(final ExcelHelper helper, final ClubHuntingSummaryBasicInfoDTO dto) {
        final HuntingClubInfoDTO huntingClubInfoDTO = requireNonNull(clubIdToInfo.get(dto.getClubId()));
        final PermitInfoDTO permitInfoDTO = requireNonNull(permitIdToInfo.get(dto.getPermitId()));
        helper.appendRow()
                .appendTextCell(requireNonNull(permitIdToNumber.get(dto.getPermitId())))
                .appendTextCell(localiser.getTranslation(huntingClubInfoDTO.getHuntingClub()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRhy()))
                .appendTextCell(localiser.getTranslation(permitInfoDTO.getRka()))
                .appendNumberCell(huntingClubInfoDTO.getClubLatitude())
                .appendNumberCell(huntingClubInfoDTO.getClubLongitude())
                .appendDateCell(dto.getHuntingEndDate())
                .appendNumberCell(dto.getTotalHuntingArea())
                .appendNumberCell(dto.getEffectiveHuntingArea())
                .appendNumberCell(dto.getRemainingPopulationInTotalArea())
                .appendNumberCell(dto.getRemainingPopulationInEffectiveArea());
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n("title"), Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String i18n(final String key) {
        return localiser.getTranslation(LOCALISATION_PREFIX + key);
    }

}
