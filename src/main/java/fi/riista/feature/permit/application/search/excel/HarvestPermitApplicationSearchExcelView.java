package fi.riista.feature.permit.application.search.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.ExcelHelper;
import fi.riista.util.LocalisedString;
import fi.riista.util.MediaTypeExtras;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.util.DateUtil.now;

public class HarvestPermitApplicationSearchExcelView extends AbstractXlsxView {
    private static final String LOCALISATION_PREFIX = "HarvestPermitApplicationSearchExcelFeature.";

    private List<HarvestPermitApplicationExcelResultDTO> results;

    private EnumLocaliser localiser;

    public HarvestPermitApplicationSearchExcelView(final List<HarvestPermitApplicationExcelResultDTO> results,
                                                   final EnumLocaliser localiser) {
        this.results = results;
        this.localiser = localiser;
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final Workbook workbook,
                                      final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        setContentType(MediaTypeExtras.APPLICATION_EXCEL_VALUE);
        response.setCharacterEncoding(Constants.DEFAULT_ENCODING);
        ContentDispositionUtil.addHeader(response, createFilename());

        final ExcelHelper helper = new ExcelHelper(workbook);

        helper.appendHeaderRow(localiser.translate(LOCALISATION_PREFIX, new String[]{
                "applicationNumber",
                "contactPerson",
                "permitHolder",
                "applicationYear",
                "harvestPermitCategory",
                "rkaName",
                "rhyName",
                "submitDate",
                "handlerName",
                "species",
                "decisionType",
                "decisionStatus",
                "decisionGrantStatus",
                "decisionLegalStatus",
                "protectedAreaType",
                "derogationReasons",
                "forbiddenMethods"
        }));


        results.forEach(dto -> {

            helper.appendRow()
                    .appendNumberCell(dto.getApplicationNumber())
                    .appendTextCell(dto.getContactPerson())
                    .appendTextCell(dto.getPermitHolder().getName())
                    .appendNumberCell(dto.getApplicationYear())
                    .appendTextCell(i18nEnum(dto.getHarvestPermitCategory()))
                    .appendTextCell(localiser.getTranslation(dto.getRkaName()))
                    .appendTextCell(localiser.getTranslation(dto.getRhyName()))
                    .appendTextCell(dto.getSubmitDate().toString("d.M.yyyy"))
                    .appendTextCell(dto.getHandler())
                    .appendTextCellWrapping(i18n(dto.getGameSpeciesNames()))
                    .appendTextCell(i18nEnum(dto.getDecisionType()))
                    .appendTextCell(resolveUnifiedStatus(dto))
                    .appendTextCell(resolveDecisionGrantState(dto))
                    .appendTextCell(i18nEnum(dto.getAppealStatus()))
                    .appendTextCellWrapping(i18nEnumSet(dto.getProtectedAreaTypes()))
                    .appendTextCellWrapping(i18nEnumSet(dto.getDecisionDerogationReasonTypes()))
                    .appendTextCellWrapping(i18nEnumSet(dto.getForbiddenMethodTypes()));

        });

        helper.autoSizeColumns();
    }

    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n("title"), Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String resolveDecisionGrantState(final HarvestPermitApplicationExcelResultDTO dto) {
        if (dto.getDecisionStatus() == PermitDecision.Status.DRAFT) {
            return "";
        }
        return i18nEnum(dto.getGrantStatus());
    }

    private String resolveUnifiedStatus(final HarvestPermitApplicationExcelResultDTO dto) {
        if (dto.getStatus() == HarvestPermitApplication.Status.AMENDING) {
            return i18nEnum(HarvestPermitApplication.Status.AMENDING);
        }
        if (dto.getStatus() == HarvestPermitApplication.Status.ACTIVE && dto.getHandler() == null) {
            return i18nEnum(HarvestPermitApplication.Status.ACTIVE);
        }
        return i18nEnum(dto.getDecisionStatus());
    }

    private String i18nEnum(final Enum<?> value) {
        return localiser.getTranslation(localiser.resourceKey(value));
    }

    private String i18n(final String key) {
        return localiser.getTranslation(LOCALISATION_PREFIX + key);
    }

    private String i18n(final Set<LocalisedString> strings) {
        if (strings == null) {
            return null;
        }
        return strings.stream()
                .map(localiser::getTranslation)
                .collect(Collectors.joining(",\n"));
    }

    private <E extends Enum> String i18nEnumSet(final Set<E> enumSet) {
        return enumSet.stream()
                .map(e -> i18nEnum(e))
                .collect(Collectors.joining(",\n"));
    }
}
