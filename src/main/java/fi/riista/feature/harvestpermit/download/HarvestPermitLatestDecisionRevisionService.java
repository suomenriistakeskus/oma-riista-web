package fi.riista.feature.harvestpermit.download;

import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HarvestPermitLatestDecisionRevisionService {

    @Resource
    private PermitDecisionRevisionRepository permitDecisionRevisionRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<PermitDecisionRevision> getLatestRevisionArchivePdfId(final PermitDecision permitDecision) {
        final List<PermitDecisionRevision> all = permitDecisionRevisionRepository.findByPermitDecision(permitDecision);

        return all.stream()
                .filter(revision -> !revision.isCancelled())
                .filter(revision -> revision.getPublishDate() != null)
                .max(Comparator.comparing(PermitDecisionRevision::getPublishDate));
    }
}
