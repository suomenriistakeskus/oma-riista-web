package fi.riista.feature.permit.invoice.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesService;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class PermitHarvestInvoicePdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getHarvestInvoicePdfFile(final long decisionId, final int gameSpeciesCode) throws IOException {
        final PermitDecision permitDecision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.READ);
        final GameSpecies gameSpecies = gameSpeciesService.requireByOfficialCode(gameSpeciesCode);

        return PermitHarvestInvoicePdf.createInvoice(permitDecision, gameSpecies).asResponseEntity();
    }
}
