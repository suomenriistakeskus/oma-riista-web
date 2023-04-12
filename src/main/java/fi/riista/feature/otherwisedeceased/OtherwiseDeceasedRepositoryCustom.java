package fi.riista.feature.otherwisedeceased;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.List;

public interface OtherwiseDeceasedRepositoryCustom {

    List<Long> findReindeerAreaLocated(Collection<OtherwiseDeceased> otherwiseDeceasedCollection);

    List<OtherwiseDeceased> search(OtherwiseDeceasedFilterDTO filterDTO);
    Slice<OtherwiseDeceased> searchPage(OtherwiseDeceasedFilterDTO filterDTO, Pageable pageable);

}
