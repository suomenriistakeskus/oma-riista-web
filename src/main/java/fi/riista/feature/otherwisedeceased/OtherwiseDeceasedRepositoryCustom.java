package fi.riista.feature.otherwisedeceased;

import java.util.Collection;
import java.util.List;

public interface OtherwiseDeceasedRepositoryCustom {

    List<Long> findReindeerAreaLocated(Collection<OtherwiseDeceased> otherwiseDeceasedCollection);
}
