package fi.riista.feature.organization.rhy.gamedamageinspection;

import com.google.common.base.Preconditions;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.security.EntityPermission;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class GameDamageInspectorLookupFeature {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true)
    public List<PersonContactInfoDTO> listActiveOccupations(final Long organisationId,
                                                            final OccupationType occupationType,
                                                            final LocalDate date) {
        final Organisation organisation =
                requireEntityService.requireOrganisation(organisationId, EntityPermission.READ);
        Preconditions.checkState(organisation.getOrganisationType().allowListOccupations(), "No occupations available for type");

        return occupationRepository.findActiveByOrganisationAndOccupationTypeAndDate(organisation, occupationType, date)
                .stream()
                .map(Occupation::getPerson)
                .map(PersonContactInfoDTO::create)
                .collect(toList());
    }
}
