package fi.riista.feature.huntingclub.moosedatacard;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MooseDataCardImportRepository extends BaseRepository<MooseDataCardImport, Long> {

    List<MooseDataCardImport> findByGroupOrderByIdAsc(@Param("group") HuntingClubGroup group);

}
