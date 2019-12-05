package fi.riista.feature.permit.decision.attachment;

import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;

import java.util.List;

public interface PermitDecisionAttachmentRepositoryCustom {

    public List<PermitDecisionAttachment> findListedAttachmentsByPermitDecisionRevision(final PermitDecisionRevision revision);
}
