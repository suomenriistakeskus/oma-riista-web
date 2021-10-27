package fi.riista.feature.gamediary.srva;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SrvaEventRepositoryCustom {
    Map<Integer, List<SrvaEvent>> findBySpeciesCodeAndPointOfTime(Collection<Integer> speciesCodes, int huntingYear);
}
