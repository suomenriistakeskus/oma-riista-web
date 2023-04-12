package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.storage.metadata.PersistentFileMetadata;

import java.util.Optional;

public interface PermitDecisionRevisionRepositoryCustom {

    Optional<PersistentFileMetadata> findLatestDecisionMetadataForInformationRequest(final int decisionNumber);

    Optional<PersistentFileMetadata> findLatestPublicDecisionPdf(final int decisionNumber);

    Optional<PermitDecisionRevision> findCarnivoreRevisionWithNoPublicPdf();
}
