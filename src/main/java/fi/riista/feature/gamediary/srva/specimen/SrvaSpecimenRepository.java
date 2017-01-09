package fi.riista.feature.gamediary.srva.specimen;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.gamediary.srva.SrvaEvent;

import java.util.List;

public interface SrvaSpecimenRepository extends BaseRepository<SrvaSpecimen, Long> {
    List<SrvaSpecimen> findByEventOrderById(SrvaEvent event);
}
