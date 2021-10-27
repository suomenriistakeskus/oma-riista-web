package fi.riista.feature.permit.application.search.excel;

import fi.riista.config.Constants;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountDTO;
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

import static fi.riista.util.DateUtil.DATE_FORMAT_FINNISH;
import static fi.riista.util.DateUtil.now;
import static fi.riista.util.F.mapNullable;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class HarvestPermitApplicationSearchExcelView extends AbstractXlsxView {
    private static final String LOCALISATION_PREFIX = "HarvestPermitApplicationSearchExcelFeature.";

    private final List<HarvestPermitApplicationExcelResultDTO> results;
    private final EnumLocaliser localiser;

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
                "permitTypeCode",
                "rkaName",
                "rhyName",
                "submitDate",
                "handlerName",
                "species",
                "decisionType",
                "decisionStatus",
                "decisionPublishDate",
                "decisionGrantStatus",
                "decisionLegalStatus",
                "protectedAreaType",
                "derogationReasons",
                "forbiddenMethods",
                "appliedAmount",
                "year",
                "grantedAmount"
        }));

        results.forEach(dto -> {

            final List<GameSpeciesDTO> appliedGameSpecies = dto.getGameSpecies();
            final Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> appliedAmounts = dto.getAppliedSpeciesAmountsBySpecies();
            final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> decisionSpeciesAmounts = dto.getDecisionSpeciesAmountsBySpecies();

            if (appliedGameSpecies == null || appliedGameSpecies.isEmpty()) {
                // No species in application
                appendRow(helper, dto);
            } else {
                appliedGameSpecies.forEach(appliedSpecies -> {
                    if (decisionSpeciesAmounts == null || decisionSpeciesAmounts.isEmpty()) {
                        // Only applied amounts, no decision available
                        final int officialCode = appliedSpecies.getCode();
                        final Float appliedAmount = appliedAmounts.get(officialCode).getSpecimenAmount();
                        appendRow(helper, dto, LocalisedString.fromMap(appliedSpecies.getName()), appliedAmount);
                    } else {
                        // Decision and application species available
                        final List<Integer> grantedYears = decisionSpeciesAmounts.keySet().stream()
                                .distinct()
                                .sorted()
                                .collect(toList());

                        grantedYears.forEach(grantedYear -> {
                            final int officialCode = appliedSpecies.getCode();
                            final Float appliedAmount = appliedAmounts.get(officialCode).getSpecimenAmount();
                            final Float decisionAmount = decisionSpeciesAmounts.get(grantedYear).get(officialCode).getSpecimenAmount();
                            appendRow(helper, dto, LocalisedString.fromMap(appliedSpecies.getName()), appliedAmount, grantedYear, decisionAmount);
                        });
                    }
                });
            }
        });

        helper.autoSizeColumns();
    }

    private void appendRow(final ExcelHelper helper,
                           final HarvestPermitApplicationExcelResultDTO dto) {
        appendRow(helper, dto, null, null);
    }

    private void appendRow(final ExcelHelper helper,
                           final HarvestPermitApplicationExcelResultDTO dto,
                           final LocalisedString speciesName,
                           final Float appliedAmount) {
        appendRow(helper, dto, speciesName, appliedAmount, null, null);
    }

    private void appendRow(final ExcelHelper helper,
                           final HarvestPermitApplicationExcelResultDTO dto,
                           final LocalisedString speciesName,
                           final Float appliedAmount,
                           final Integer grantedYear,
                           final Float decisionAmount) {
        helper.appendRow()
                .appendNumberCell(dto.getApplicationNumber())
                .appendTextCell(dto.getContactPerson())
                .appendTextCell(dto.getPermitHolder().getName())
                .appendNumberCell(dto.getApplicationYear())
                .appendTextCell(i18nEnum(dto.getHarvestPermitCategory()))
                .appendTextCell(mapNullable(dto.getPermitTypeCode(), code -> localiser.getTranslation("PermitTypeCode." + code)))
                .appendTextCell(localiser.getTranslation(dto.getRkaName()))
                .appendTextCell(localiser.getTranslation(dto.getRhyName()))
                .appendTextCell(dto.getSubmitDate().toString(DATE_FORMAT_FINNISH))
                .appendTextCell(dto.getHandler())
                .appendTextCellWrapping(localiser.getTranslation(speciesName))
                .appendTextCell(i18nEnum(dto.getDecisionType()))
                .appendTextCell(resolveUnifiedStatus(dto))
                .appendTextCell(ofNullable(dto.getDecisionPublishDate())
                        .map(tstamp -> tstamp.toString(DATE_FORMAT_FINNISH))
                        .orElse(null))
                .appendTextCell(resolveDecisionGrantState(dto))
                .appendTextCell(i18nEnum(dto.getAppealStatus()))
                .appendTextCellWrapping(i18nEnumSet(dto.getProtectedAreaTypes()))
                .appendTextCellWrapping(i18nEnumSet(dto.getDecisionDerogationReasonTypes()))
                .appendTextCellWrapping(i18nEnumSet(dto.getForbiddenMethodTypes()))
                .appendNumberCell(appliedAmount)
                .appendNumberCell(grantedYear)
                .appendNumberCell(decisionAmount);

    }

    private String createFilename() {
        return String.format("%s-%s.xlsx", i18n("title"), Constants.FILENAME_TS_PATTERN.print(now()));
    }

    private String resolveDecisionGrantState(final HarvestPermitApplicationExcelResultDTO dto) {
        if (dto.getDecisionStatus() == DecisionStatus.DRAFT) {
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
        return localiser.getTranslation(EnumLocaliser.resourceKey(value));
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
