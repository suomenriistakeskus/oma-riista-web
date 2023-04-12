package fi.riista.feature.permit.decision.revision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermitDecisionRevisionAttachmentRepository extends JpaRepository<PermitDecisionRevisionAttachment, Long> {

    List<PermitDecisionRevisionAttachment> findAllByDecisionRevision(PermitDecisionRevision revision);

}
