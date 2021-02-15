package fi.riista.feature.common.decision.nomination.revision;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NominationDecisionRevisionAttachmentRepositoryCustom {

    List<NominationDecisionRevisionDTO.AttachmentDTO> findByNominationDecisionRevision(final NominationDecisionRevision revision);

    Map<NominationDecisionRevision, Set<NominationDecisionRevisionDTO.AttachmentDTO>> findByNominationDecisionRevisionIn(final Collection<NominationDecisionRevision> revision);

}
