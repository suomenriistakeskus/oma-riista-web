package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface OtherwiseDeceasedAttachmentRepository extends BaseRepository<OtherwiseDeceasedAttachment, Long> {
    List<OtherwiseDeceasedAttachment> findAllByOtherwiseDeceased(final OtherwiseDeceased otherwiseDeceased);
}
