package fi.riista.feature.permit.decision.attachment;

import fi.riista.feature.permit.decision.PermitDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermitDecisionAttachmentRepository extends JpaRepository<PermitDecisionAttachment, Long>,
        PermitDecisionAttachmentRepositoryCustom {

    List<PermitDecisionAttachment> findAllByPermitDecision(final PermitDecision decision);

}
