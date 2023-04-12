package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.person.Person;

import java.util.List;

public interface OccupationTrainingRepository extends BaseRepository<OccupationTraining, Long> {

    List<OccupationTraining> findByPerson(Person person);

}
