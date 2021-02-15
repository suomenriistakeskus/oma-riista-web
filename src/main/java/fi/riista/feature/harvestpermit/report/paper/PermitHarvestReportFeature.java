package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.LocalisedString;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.permit.PermitTypeCode.ANNUAL_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.FOWL_AND_UNPROTECTED_BIRD;
import static fi.riista.feature.permit.PermitTypeCode.MAMMAL_DAMAGE_BASED;

@Component
public class PermitHarvestReportFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public PermitHarvestReportPdf getPdf(final long decisionId) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                EntityPermission.READ);
        final List<PermitDecisionSpeciesAmount> speciesAmountList =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision);
        final Map<Integer, LocalisedString> nameIndex = gameSpeciesService.getNameIndex();
        final PermitHarvestReportModel model = PermitHarvestReportModel.create(permitDecision, speciesAmountList);
        final PermitHarvestReportI18n i18n = new PermitHarvestReportI18n(nameIndex, permitDecision.getLocale());

        switch (permitDecision.getPermitTypeCode()) {
            case FOWL_AND_UNPROTECTED_BIRD:
            case ANNUAL_UNPROTECTED_BIRD: {
                return PermitHarvestReportPdf.create(BirdHarvestReportPdfBuilder.getPdf(model, i18n));
            }
            case MAMMAL_DAMAGE_BASED: {
                return PermitHarvestReportPdf.create(MammalHarvestReportPdfBuilder.getPdf(model, i18n));
            }
            default: {
                throw new IllegalArgumentException("Invalid permit type code " + permitDecision.getPermitTypeCode());
            }

        }
    }
}
