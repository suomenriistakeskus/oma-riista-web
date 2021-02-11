package fi.riista.feature.harvestpermit.search;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountDTO;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.DateUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.F;
import fi.riista.util.Localiser;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;

public class HarvestPermitSearchExcelView extends AbstractXlsxView {

    private static String LOCALISATION_PREFIX = "HarvestPermitSearchExcel.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT_FINNISH);

    private final EnumLocaliser localiser;
    private final List<HarvestPermitSearchExportDTO> permits;
    private final String filename;

    public HarvestPermitSearchExcelView(final EnumLocaliser localiser,
                                        final List<HarvestPermitSearchExportDTO> permitDTOs) {

        this.localiser = localiser;
        this.permits = permitDTOs;
        this.filename = String.format("%s-%s.xlsx",
                                      localiser.getTranslation("HarvestPermitSearchExcel.filename"),
                                      Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));

    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map,
                                      final Workbook workbook,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, filename);

        createPermitSheet(workbook);
    }

    private void createPermitSheet(final Workbook workbook) {
        final ExcelHelper helper = new ExcelHelper(workbook, localiser.getTranslation("HarvestPermitSearchExcel.workbook"));
        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, new String[] {
                "permitNumber",
                "permitType",
                "species",
                "state",
                "validity",
                "rka",
                "contactName",
                "contactHunterNumber",
                "contactEmail",
                "contactPhoneNumber",
                "permitHolderName",
                "permitHolderType"
        }));

        permits.forEach(permit -> helper.appendRow()
                    .appendTextCell(permit.getPermitNumber())
                    .appendTextCell(permit.getPermitType())
                    .appendWrappedTextCell(String.join(",\n", F.mapNonNullsToList(permit.getSpeciesAmounts(), this::formatSpeciesAmounts)))
                    .appendTextCell(permit.getHarvestReportState() == null ? i18n("reportStateEmpty"): localiser.getTranslation(permit.getHarvestReportState()))
                    .appendTextCell(localiser.getTranslation(permit.getValidity()))
                    .appendTextCell(localiser.getTranslation(permit.getRka()))
                    .appendWrappedTextCell(String.join(",\n", F.mapNonNullsToList(permit.getContacts(), HarvestPermitSearchExportDTO.ContactPersonDTO::getFullName)))
                    .appendWrappedTextCell(String.join(",\n", F.mapNonNullsToList(permit.getContacts(), HarvestPermitSearchExportDTO.ContactPersonDTO::getHunterNumber)))
                    .appendWrappedTextCell(String.join(",\n", F.mapNonNullsToList(permit.getContacts(), HarvestPermitSearchExportDTO.ContactPersonDTO::getEmail)))
                    .appendWrappedTextCell(String.join(",\n", F.mapNonNullsToList(permit.getContacts(), HarvestPermitSearchExportDTO.ContactPersonDTO::getPhoneNumber)))
                    .appendTextCell(permit.getPermitHolderName())
                    .appendTextCell(translateHolderType(permit.getPermitHolderType())));
        helper.autoSizeColumns();
    }

    private String translateHolderType(final PermitHolder.PermitHolderType permitHolderType) {
        return permitHolderType == null ? "" : localiser.getTranslation(permitHolderType);
    }

    private String formatSpeciesAmounts(final HarvestPermitSpeciesAmountDTO amount) {
        final StringBuilder sb = new StringBuilder();

        sb.append(Localiser.select(amount.getGameSpecies().getName().get("fi"), amount.getGameSpecies().getName().get("sv")))
                .append(" ")
                .append(formatAmounts(amount))
                .append(" (")
                .append(formatDates(amount.getBeginDate(), amount.getEndDate()));

        if (amount.getBeginDate2() != null) {
            sb.append(", ").append(formatDates(amount.getBeginDate2(), amount.getEndDate2()));
        }
        sb.append(")");

        return sb.toString();
    }

    private String formatAmounts(final HarvestPermitSpeciesAmountDTO amount) {
        final List<String> amounts = new ArrayList<>();
        Optional.ofNullable(amount.getAmount()).ifPresent(a -> amounts.add(String.format("%d %s", Math.round(a), i18n("amount"))));
        Optional.ofNullable(amount.getNestAmount()).ifPresent(a -> amounts.add(String.format("%d %s", a, i18n("nestAmount"))));
        Optional.ofNullable(amount.getEggAmount()).ifPresent(a -> amounts.add(String.format("%d %s", a, i18n("eggAmount"))));
        Optional.ofNullable(amount.getConstructionAmount()).ifPresent(a -> amounts.add(String.format("%d %s", a, i18n("constructionAmount"))));
        return String.join(", ", amounts);
    }

    private String formatDates(LocalDate beginDate, LocalDate endDate) {
        return String.format("%s - %s", beginDate.toString(DATE_FORMATTER), endDate.toString(DATE_FORMATTER));
    }

    private String i18n(final String keyPostfix) {
        return localiser.getTranslation(LOCALISATION_PREFIX + keyPostfix);
    }

}
