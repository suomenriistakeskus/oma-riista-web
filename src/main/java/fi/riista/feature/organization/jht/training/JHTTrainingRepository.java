package fi.riista.feature.organization.jht.training;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.person.Person;

import java.util.List;

public interface JHTTrainingRepository extends BaseRepository<JHTTraining, Long>, JHTTrainingRepositoryCustom {

    List<JHTTraining> findByPerson(Person person);
}
