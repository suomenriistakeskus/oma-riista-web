package fi.riista.feature.permit.decision.derogation.pdf;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import fi.riista.security.EntityPermission;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

@Service
public class AnnualRenewalPermitPdfFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private PermitDecisionRevisionRepository revisionRepository;

    @Transactional(readOnly = true)
    public AnnualRenewalPermitPdfModelDTO getModel(final long id) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(id, EntityPermission.READ);
        final PermitDecision decision = permit.getPermitDecision();

        final DateTime firstRevisionPublishDate = revisionRepository.findByPermitDecision(decision).stream()
                .map(PermitDecisionRevision::getPublishDate)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Function.identity()))
                .orElseThrow(() -> new NotFoundException("Published revision not found"));
        return new AnnualRenewalPermitPdfModelDTO(decision, permit, firstRevisionPublishDate);
    }
}
