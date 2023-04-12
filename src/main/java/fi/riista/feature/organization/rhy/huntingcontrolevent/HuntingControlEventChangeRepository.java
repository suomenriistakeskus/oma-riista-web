package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.Collection;
import java.util.List;

public interface HuntingControlEventChangeRepository extends BaseRepository<HuntingControlEventChange, Long> {

    List<HuntingControlEventChange> findAllByHuntingControlEvent(HuntingControlEvent event);
    List<HuntingControlEventChange> findAllByHuntingControlEventIn(Collection<HuntingControlEvent> events);

}
