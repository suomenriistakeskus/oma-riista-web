package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.organization.jht.email.NotifyJhtOccupationNominationToRkaEmailDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OccupationNominationRepositoryCustom {
    Map<OccupationNomination.NominationStatus, Long> countByNominationStatus(Riistanhoitoyhdistys rhy, final OccupationType occupationType);

    Page<OccupationNomination> searchPage(
            Pageable pageRequest, OccupationType occupationType,
            OccupationNomination.NominationStatus nominationStatus,
            Riistanhoitoyhdistys rhy,
            Person person,
            LocalDate beginDate,
            LocalDate endDate);

    List<NotifyJhtOccupationNominationToRkaEmailDTO> findRkaNotifications(LocalDate nominationDate);
}
