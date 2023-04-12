package fi.riista.feature.storage.metadata;

import java.util.List;

public interface PersistentFileMetadataRepositoryCustom {
    List<PersistentFileMetadata> findLatestDecisionAttachmentsMetadataForInformationRequest(int decisionNumber);
    List<PersistentFileMetadata> findLatestPublicDecisionAttachmentsPdf(final int decisionNumber);
}
