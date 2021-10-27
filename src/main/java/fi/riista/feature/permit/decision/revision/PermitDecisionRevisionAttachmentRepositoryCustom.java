package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;

import java.util.List;
import java.util.Optional;

public interface PermitDecisionRevisionAttachmentRepositoryCustom {

    List<PersistentFileMetadata> findLatestPublicDecisionAttachmentsPdf(final int decisionNumber);
}
