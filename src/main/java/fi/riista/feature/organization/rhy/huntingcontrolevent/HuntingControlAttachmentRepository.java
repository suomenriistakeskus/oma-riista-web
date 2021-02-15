package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface HuntingControlAttachmentRepository extends BaseRepository<HuntingControlAttachment, Long> {
    List<HuntingControlAttachment> findByHuntingControlEventIn(final Collection<HuntingControlEvent> eventSet);

    List<HuntingControlAttachment> findByHuntingControlEventId(final long id);

    List<HuntingControlAttachment> findByHuntingControlEvent(final HuntingControlEvent event);
}
