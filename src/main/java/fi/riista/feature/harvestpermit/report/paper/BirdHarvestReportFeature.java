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

@Component
public class BirdHarvestReportFeature {

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public BirdHarvestReportPdf getPdf(final long decisionId) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final List<PermitDecisionSpeciesAmount> speciesAmountList = permitDecisionSpeciesAmountRepository.findByPermitDecision(permitDecision);
        final Map<Integer, LocalisedString> nameIndex = gameSpeciesService.getNameIndex();
        final BirdHarvestReportModel model = BirdHarvestReportModel.create(permitDecision, speciesAmountList);
        final BirdHarvestReportI18n i18n = new BirdHarvestReportI18n(nameIndex, permitDecision.getLocale());

        return BirdHarvestReportPdf.create(model, i18n);
    }
}
