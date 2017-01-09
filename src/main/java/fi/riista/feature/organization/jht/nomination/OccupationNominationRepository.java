package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface OccupationNominationRepository extends BaseRepository<OccupationNomination, Long>,
        OccupationNominationRepositoryCustom {

    List<OccupationNomination> findByPersonAndRhyAndOccupationType(
            Person person,
            Riistanhoitoyhdistys rhy,
            OccupationType occupationType);

    @Query("SELECT o.person.id FROM #{#entityName} o WHERE o.occupationType = ?1 AND o.nominationStatus IN ?2")
    List<Long> findPersonIdByOccupationTypeAndNominationStatusIn(
            OccupationType occupationType, Set<OccupationNomination.NominationStatus> nominationStatus);
}
