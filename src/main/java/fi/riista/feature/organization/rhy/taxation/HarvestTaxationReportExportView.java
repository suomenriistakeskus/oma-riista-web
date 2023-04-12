package fi.riista.feature.organization.rhy.taxation;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.Localiser;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Excel is used to export members list from the club members section
 */
public class HarvestTaxationReportExportView extends AbstractXlsxView {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final String filename;
    private final List<HarvestTaxationReportExcelRowDTO> rows;

    private ExcelHelper excelHelper;
    private final EnumLocaliser localiser;

    public HarvestTaxationReportExportView(final EnumLocaliser localiser,
                                           final GameSpecies species,
                                           final int huntingYear,
                                           final Riistanhoitoyhdistys rhy,
                                           final List<HarvestTaxationReportExcelRowDTO> rows) {
        this.localiser = localiser;
        this.filename = createFilename(rhy, species, huntingYear);
        this.rows = rows;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        excelHelper = new ExcelHelper(workbook, "TaxationReports");
        createHeaderRow();

        HarvestTaxationReportExcelRowDTO lastHtaRow = null;
        for (final HarvestTaxationReportExcelRowDTO dto : rows) {
            lastHtaRow = handleHtaRow(lastHtaRow, dto);

            excelHelper.appendRow()
                    .appendTextCell(dto.getRhyName().getFinnish())
                    .appendTextCell(dto.getRhyName().getSwedish())
                    .appendTextCell(dto.getHtaCode())
                    .appendNumberCell(dto.getAreaSize())
                    .appendNumberCell(dto.getPlanningBasisPopulation())
                    .appendNumberCell(dto.getPlannedRemainingPopulation())
                    .appendNumberCell(dto.getGenderDistribution())
                    .appendPercentageCell(convertIntegerToDouble(dto.getYoungPercent()))
                    .appendPercentageCell(convertIntegerToDouble(dto.getPlannedUtilizationRateOfThePermits()))
                    .appendPercentageCell(convertIntegerToDouble(dto.getShareOfBankingPermits()))
                    .appendNumberCell(dto.getPlannedPermitMin())
                    .appendNumberCell(dto.getPlannedPermitMax())
                    .appendNumberCell(dto.getPlannedCatchMin())
                    .appendNumberCell(dto.getPlannedCatchMax())
                    .appendNumberCell(dto.getPlannedPreyDensityMin())
                    .appendNumberCell(dto.getPlannedPreyDensityMax())
                    .appendNumberCell(dto.getPlannedPermitDensityMin())
                    .appendNumberCell(dto.getPlannedPermitDensityMax())
                    .appendPercentageCell(convertIntegerToDouble(dto.getPlannedCatchYoungPercent()))
                    .appendPercentageCell(convertIntegerToDouble(dto.getPlannedCatchMalePercent()))
                    .appendDateCell(dto.getApprovedAtTheBoardMeeting())
                    .appendDateCell(dto.getConfirmedDate());
        }
        appendHTARowToExport(lastHtaRow);

        excelHelper.autoSizeColumns();
    }
    private Double convertIntegerToDouble(final Integer value) {
        return value == null? null: value.doubleValue();
    }

    private HarvestTaxationReportExcelRowDTO handleHtaRow(HarvestTaxationReportExcelRowDTO lastHtaRow, final HarvestTaxationReportExcelRowDTO dto) {
        if (lastHtaRow == null) {
            lastHtaRow = new HarvestTaxationReportExcelRowDTO(dto.getHtaCode());
        }

        // if HTA is changed append previous HTA to export
        if (!lastHtaRow.getHtaCode().equals(dto.getHtaCode())) {
            appendHTARowToExport(lastHtaRow);
            lastHtaRow = new HarvestTaxationReportExcelRowDTO(dto.getHtaCode());
        }

        // sum data to hta row
        lastHtaRow.addDataFromAnotherDTO(dto);
        return lastHtaRow;
    }

    private void appendHTARowToExport(final HarvestTaxationReportExcelRowDTO lastHtaRow) {
        if (lastHtaRow == null) {
            return;
        }
        excelHelper.appendRow()
                .appendTextCell(localiser.getTranslation("HarvestTaxationReportExportView.htaSum"))
                .appendTextCell("")
                .appendTextCell("")
                .appendNumberCell(lastHtaRow.getAreaSize())
                .appendTextCell("")  // PlanningBasisPopulation
                .appendTextCell("")  // PlannedRemainingPopulation
                .appendTextCell("")  // GenderDistribution
                .appendTextCell("")  // youngPercent
                .appendTextCell("")  // plannedUtilizationRateOfThePermits
                .appendTextCell("")  // shareOfBankingPermits
                .appendNumberCell(lastHtaRow.getPlannedPermitMin())
                .appendNumberCell(lastHtaRow.getPlannedPermitMax())
                .appendNumberCell(lastHtaRow.getPlannedCatchMin())
                .appendNumberCell(lastHtaRow.getPlannedCatchMax());
        excelHelper.appendRow();
    }

    private void createHeaderRow() {
        final String HEADER_PREFIX = "HarvestTaxationReportExportView.";

        final String[] HEADERS = new String[]{
                "rhyNameFinnish",
                "rhyNameSwedish",
                "htaCode",
                "areaSize",
                "planningBasisPopulation",
                "plannedRemainingPopulation",
                "genderDistribution",
                "youngPercent",
                "plannedUtilizationRateOfThePermits",
                "shareOfBankingPermits",
                "plannedPermitMin",
                "plannedPermitMax",
                "plannedCatchMin",
                "plannedCatchMax",
                "plannedPreyDensityMin",
                "plannedPreyDensityMax",
                "plannedPermitDensityMin",
                "plannedPermitDensityMax",
                "plannedCatchYoungPercent",
                "plannedCatchMalePercent",
                "approvedAtTheBoardMeeting",
                "confirmed"
        };

        final List<String> headers = Arrays.asList(localiser.translate(HEADER_PREFIX, HEADERS));

        excelHelper.appendRow();
        headers.forEach(header -> excelHelper.appendTextCellBold(header));
    }

    private String createFilename(final Riistanhoitoyhdistys rhy,
                                  final GameSpecies species,
                                  final int huntingYear) {
        if (rhy == null) {
            return ContentDispositionUtil.cleanFileName(String.format("%s-%s-%s.xlsx",
                    Localiser.select(species.getNameLocalisation()), huntingYear, DATE_FORMAT.print(DateUtil.today())));
        } else {
            return ContentDispositionUtil.cleanFileName(String.format("%s-%s-%s-%s.xlsx",
                    Localiser.select(new LocalisedString(rhy.getNameFinnish(), rhy.getNameSwedish())),
                    Localiser.select(species.getNameLocalisation()), huntingYear, DATE_FORMAT.print(DateUtil.today())));
        }
    }
}
