package fi.riista.feature.common.decision.nomination.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.decision.nomination.NominationDecisionDTO;
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

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.F.mapNullable;

public class NominationDecisionExcelView extends AbstractXlsxView {

    private static final String LOCALISATION_PREFIX = "NominationDecisionExcelView.";
    private static final String[] HEADERS = new String[]{
            "decisionNumber", "contactPerson", "rhyCode", "rhy", "decisionType",
            "proposalDate", "handler", "occupationType", "status", "publishDate", "appealStatus"};

    private final List<NominationDecisionDTO> dtos;
    private final EnumLocaliser i18n;

    public NominationDecisionExcelView(final List<NominationDecisionDTO> dtos, final EnumLocaliser i18n) {
        this.dtos = dtos;
        this.i18n = i18n;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> map, final Workbook workbook,
                                      final HttpServletRequest request, final HttpServletResponse response) {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper excelHelper = new ExcelHelper(workbook);

        excelHelper.appendRow().appendHeaderRow(i18n.translate(LOCALISATION_PREFIX, HEADERS));

        dtos.forEach(decision -> {
            excelHelper.appendRow()
                    .appendNumberCell(decision.getDecisionNumber())
                    .appendTextCell(mapNullable(decision.getContactPerson(), h -> h.getFirstName() + " " + h.getLastName()))
                    .appendTextCell(decision.getRhy().getOfficialCode())
                    .appendTextCell(i18n.getTranslation(decision.getRhy().getNameLocalisation()))
                    .appendTextCell(i18n.getTranslation(decision.getDecisionType()))
                    .appendTextCell(mapNullable(decision.getProposalDate(), d -> d.toString(DATE_FORMAT_FINNISH)))
                    .appendTextCell(mapNullable(decision.getHandler(), h -> h.getFirstName() + " " + h.getLastName()))
                    .appendTextCell(i18n.getTranslation(decision.getOccupationType()))
                    .appendTextCell(i18n.getTranslation(decision.getStatus()))
                    .appendTextCell(mapNullable(decision.getPublishDate(), d -> d.toString(DATE_FORMAT_FINNISH)))
                    .appendTextCell(i18n.getTranslation(decision.getAppealStatus()));
        });

        excelHelper.autoSizeColumns();
    }

    private String createFilename() {
        return String.format(
                "%s-%s.xlsx",
                i18n.getTranslation(LOCALISATION_PREFIX + "fileName"),
                Constants.FILENAME_TS_PATTERN.print(DateUtil.now()));
    }
}
