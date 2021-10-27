package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface OtherwiseDeceasedChangeRepository extends BaseRepository<OtherwiseDeceasedChange, Long> {

    List<OtherwiseDeceasedChange> findAllByOtherwiseDeceasedOrderByPointOfTime(final OtherwiseDeceased otherwiseDeceased);
}
