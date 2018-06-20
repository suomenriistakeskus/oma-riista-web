package fi.riista.feature.pub.occupation;

import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.Person_;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.util.F;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.JoinType;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.riista.util.Collect.idSet;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static fi.riista.util.jpa.JpaSpecs.inCollection;
import static java.util.stream.Collectors.toList;

@Component
public class PublicOccupationSearchFeature {

    public static final int MAX_RESULTS = 500;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private PublicDTOFactory dtoFactory;

    @Transactional(readOnly = true)
    public PublicOrganisationDTO getRiistakeskus() {
        // Sort order is not critical since only one result is expected from the query.
        List<Organisation> rk =
                organisationRepository.findByOrganisationType(OrganisationType.RK, new JpaSort(Organisation_.id));

        if (rk.size() != 1) {
            throw new IllegalStateException("Invariant violation: There must be only one Riistakeskus organisation");
        }

        return toDTO(rk.iterator().next());
    }

    @Transactional(readOnly = true)
    public PublicOrganisationDTO getByTypeAndOfficialCode(OrganisationType organisationType, String officialCode) {
        return toDTO(organisationRepository.findByTypeAndOfficialCode(organisationType, officialCode));
    }

    private PublicOrganisationDTO toDTO(Organisation org) {
        if (org == null) {
            throw new NotFoundException();
        }
        return dtoFactory.create(org);
    }

    @Transactional(readOnly = true)
    public PublicOccupationsAndOrganisationsDTO findOccupationsAndOrganisations(
            final PublicOccupationSearchParameters parameters) {

        return _findOccupationsAndOrganisations(parameters, MAX_RESULTS);
    }

    // For testing
    @Transactional(readOnly = true)
    public PublicOccupationsAndOrganisationsDTO _findOccupationsAndOrganisations(
            final PublicOccupationSearchParameters parameters, final int maxResults) {

        final List<Occupation> resultOccupations = occupationRepository.findAll(parameters.toJpaSpecification());

        if (resultOccupations.isEmpty()) {
            return PublicOccupationsAndOrganisationsDTO.EMPTY_RESULT;
        }

        // Fetch persons, organisations and their addresses into 1st level cache of JPA entity manager.
        final Set<Long> personIds = resultOccupations.stream().map(Occupation::getPerson).collect(idSet());
        personRepository.findAll(Specifications
                .where(inCollection(Person_.id, personIds))
                .and(fetch(Person_.mrAddress, JoinType.LEFT))
                .and(fetch(Person_.otherAddress, JoinType.LEFT)));

        final Set<Long> organisationIds = resultOccupations.stream().map(Occupation::getOrganisation).collect(idSet());
        final List<Organisation> resultOrganisations = organisationRepository.findAll(Specifications
                .where(inCollection(Organisation_.id, organisationIds))
                .and(fetch(Organisation_.address, JoinType.LEFT)));

        final List<PublicOccupationDTO> occupations = toOccupationDTOs(resultOccupations);
        if (occupations.size() > maxResults) {
            return PublicOccupationsAndOrganisationsDTO.TOO_MANY_RESULTS;
        }
        return new PublicOccupationsAndOrganisationsDTO(
                occupations,
                toOrganisationDTOs(resultOrganisations));
    }

    private List<PublicOccupationDTO> toOccupationDTOs(final List<Occupation> occupations) {
        return occupations.stream()
                .sorted(OccupationSort.BY_TYPE.thenComparing(OccupationSort.BY_CALL_ORDER_ONLY_FOR_APPLICABLE_TYPES))
                .map(occupation -> {
                    final PublicOccupationTypeDTO dto = dtoFactory.create(
                            occupation.getOccupationType(), occupation.getOrganisation().getOrganisationType());
                    return PublicDTOFactory.create(occupation, dto);
                })
                .collect(toList());
    }

    private List<PublicOrganisationDTO> toOrganisationDTOs(final List<Organisation> resultOrganisations) {
        final Map<Organisation, Occupation> rhyToCoordinator = occupationRepository.listCoordinators(resultOrganisations);

        return F.mapNonNullsToList(resultOrganisations, org -> {
            final PublicOrganisationDTO dto = dtoFactory.createWithoutSuborganisations(org);

            if (dto.getAddress() == null || dto.getPhoneNumber() == null || dto.getEmail() == null) {
                if (rhyToCoordinator.containsKey(org)) {
                    final Person coordinatorPerson = rhyToCoordinator.get(org).getPerson();

                    if (dto.getAddress() == null) {
                        dto.setAddress(AddressDTO.from(coordinatorPerson.getAddress()));
                    }

                    if (dto.getPhoneNumber() == null) {
                        dto.setPhoneNumber(coordinatorPerson.getPhoneNumber());
                    }

                    if (dto.getEmail() == null) {
                        dto.setEmail(coordinatorPerson.getEmail());
                    }
                }
            }

            return dto;
        });
    }

    public List<PublicOccupationTypeDTO> getAllOccupationTypes() {
        // Use EnumSet for correct ordering
        return EnumSet.of(OrganisationType.RHY, OrganisationType.RK, OrganisationType.VRN, OrganisationType.ARN,
                OrganisationType.RKA).stream()
                .flatMap(orgType -> OccupationType.getApplicableTypes(orgType).stream()
                        .map(occType -> dtoFactory.create(occType, orgType)))
                .collect(toList());
    }
}
