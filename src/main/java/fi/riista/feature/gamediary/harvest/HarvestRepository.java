package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;

import java.util.List;

public interface HarvestRepository extends BaseRepository<Harvest, Long>, HarvestRepositoryCustom {

    List<Harvest> findByActualShooter(Person person);

    List<Harvest> findByHuntingDayOfGroup(GroupHuntingDay day);

    Harvest findByAuthorAndMobileClientRefId(Person author, Long refId);
}
