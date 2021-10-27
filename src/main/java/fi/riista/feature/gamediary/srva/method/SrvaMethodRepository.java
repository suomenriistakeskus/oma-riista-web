package fi.riista.feature.gamediary.srva.method;


import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.srva.SrvaEvent;

import java.util.List;

public interface SrvaMethodRepository extends BaseRepository<SrvaMethod, Long> {
    List<SrvaMethod> findByEvent(SrvaEvent event);
    List<SrvaMethod> findByEventAndIsChecked(SrvaEvent event, boolean isChecked);
}
