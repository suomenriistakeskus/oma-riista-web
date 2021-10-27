package fi.riista.feature.permit.decision.revision;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermitDecisionRevisionAttachmentRepository extends JpaRepository<PermitDecisionRevisionAttachment, Long>, PermitDecisionRevisionAttachmentRepositoryCustom {
}
