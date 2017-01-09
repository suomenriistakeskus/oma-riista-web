package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;

import java.util.List;

public interface GroupHuntingDayRepository extends BaseRepository<GroupHuntingDay, Long>,
        GroupHuntingDayRepositoryCustom {

    List<GroupHuntingDay> findByGroup(HuntingClubGroup group);
}
