package fi.riista.feature.organization;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.repository.BaseRepository;

import java.util.List;

public interface RiistakeskusRepository extends BaseRepository<Riistakeskus, Long> {

    default Riistakeskus get() {
        final List<Riistakeskus> list = findAll();

        Preconditions.checkState(!list.isEmpty(), "Could not find Riistakeskus root organisation");
        Preconditions.checkState(list.size() == 1, "Multiple Riistakeskus root organisations found");

        return list.get(0);
    }
}
