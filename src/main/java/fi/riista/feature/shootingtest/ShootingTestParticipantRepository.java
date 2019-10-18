package fi.riista.feature.shootingtest;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.person.Person;

import java.util.List;
import java.util.Optional;

public interface ShootingTestParticipantRepository
        extends BaseRepository<ShootingTestParticipant, Long>, ShootingTestParticipantRepositoryCustom {

    List<ShootingTestParticipant> findByShootingTestEvent(ShootingTestEvent event);

    List<ShootingTestParticipant> findByShootingTestEventAndCompleted(ShootingTestEvent event, boolean completed);

    Optional<ShootingTestParticipant> findByShootingTestEventAndPerson(ShootingTestEvent event, Person person);


}
