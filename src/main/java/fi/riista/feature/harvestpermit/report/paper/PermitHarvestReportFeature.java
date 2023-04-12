package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
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

import static com.google.common.base.Preconditions.checkArgument;
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

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public PermitHarvestReportPdf getPdf(final long decisionId) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId,
                EntityPermission.READ);
        final List<PermitDecisionSpeciesAmount> speciesAmountList =
                permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision);
        final Map<Integer, LocalisedString> nameIndex = gameSpeciesService.getNameIndex();
        final PermitHarvestReportModel model = PermitHarvestReportModel.createFromDecision(permitDecision, speciesAmountList);
        final PermitHarvestReportI18n i18n = new PermitHarvestReportI18n(nameIndex, permitDecision.getLocale());

        // Deduce through application in order to resolve original type in case of forbidden methods
        final HarvestPermitApplication application = permitDecision.getApplication();
        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(
                application.getHarvestPermitCategory(), application.getValidityYears());

        switch (permitTypeCode) {
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

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public PermitHarvestReportPdf getRenewedPermitHarvestReportPdf(final long permitId) throws IOException {
        final HarvestPermit harvestPermit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        checkArgument(PermitTypeCode.isAnnualUnprotectedBird(harvestPermit.getPermitTypeCode()),
                "Only annual renewal permit is supported");

        final Map<Integer, LocalisedString> nameIndex = gameSpeciesService.getNameIndex();

        final List<HarvestPermitSpeciesAmount> speciesAmountList =
                harvestPermitSpeciesAmountRepository.findByHarvestPermit(harvestPermit);

        final PermitHarvestReportModel model = PermitHarvestReportModel.createFromPermit(harvestPermit, speciesAmountList);
        final PermitHarvestReportI18n i18n = new PermitHarvestReportI18n(nameIndex, model.getLocale());

        return PermitHarvestReportPdf.create(BirdHarvestReportPdfBuilder.getPdf(model, i18n));
    }
}
