package fi.riista.feature.organization.occupation;

import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class OccupationCrudFeature extends SimpleAbstractCrudFeature<Long, Occupation, OccupationDTO> {

    private static final Comparator<Occupation> OCCUPATION_SORT = OccupationSort.BY_TYPE
            .thenComparing(OccupationSort.BY_CALL_ORDER_ONLY_FOR_APPLICABLE_TYPES)
            .thenComparing(OccupationSort.BY_LAST_NAME)
            .thenComparing(OccupationSort.BY_FIRST_NAME);

    private static final Comparator<Person> CANDIDATE_PERSON_SORT =
            comparing(Person::getLastName).thenComparing(Person::getFirstName);

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private AuditService auditService;

    @Resource
    private RequireEntityService requireEntityService;

    @Override
    protected JpaRepository<Occupation, Long> getRepository() {
        return occupationRepository;
    }

    @Transactional(readOnly = true)
    public List<OccupationType> getApplicableOccupationTypes(final long organisationId) {
        final Organisation org = getOrganisation(organisationId);
        return OccupationType.getListOfApplicableTypes(org.getOrganisationType());
    }

    @Override
    protected void updateEntity(final Occupation entity, final OccupationDTO dto) {
        if (entity.isNew()) {
            auditService.log("createOccupation", dto, auditService.extra("orgId", dto.getOrganisationId()));
        } else {
            auditService.log("updateOccupation", dto, auditService.extra("orgId", dto.getOrganisationId()));
        }

        assertCoordinatorCanNotEditCoordinator(entity.getOccupationType(), dto.getOccupationType());
        assertCoordinatorCannotModifyJHTOccupation(entity.getOccupationType(), dto.getOccupationType());

        if (entity.isNew()) {
            // Organisation and Person should not be modified after creation
            final Organisation organisation = getOrganisation(dto);
            final Person person = personRepository.getOne(dto.getPerson().getId());

            assertCanNotCreateForDeceased(person);

            entity.setOrganisationAndOccupationType(organisation, dto.getOccupationType());
            entity.setPerson(person);
        }

        assertCanEditOccupationsForCorrectOrganisationTypes(entity.getOrganisation().getOrganisationType());

        entity.setEndDate(dto.getEndDate());
        entity.setBeginDate(dto.getBeginDate());
        entity.setCallOrder(dto.getCallOrder());
        entity.setAdditionalInfo(dto.getAdditionalInfo());

        if (dto.getPerson() != null) {
            updatePersonInformation(entity.getPerson(), dto.getPerson());

            if (dto.getPerson().getAddress() != null) {
                updatePersonAddress(entity.getPerson(), dto.getPerson().getAddress());
            }
        }
    }

    private void updatePersonInformation(final Person person, final PersonDTO dto) {
        if (!person.isRegistered() && !byNameAndPhoneAndEmailSame(dto, person)) {
            if (StringUtils.hasText(dto.getPhoneNumber())) {
                person.setPhoneNumber(dto.getPhoneNumber());
            }

            if (StringUtils.hasText(dto.getEmail())) {
                person.setEmail(dto.getEmail());
            }

            if (StringUtils.hasText(dto.getByName())) {
                person.setByName(dto.getByName());
            }

            auditService.log("updatePersonInformation", person);
        }
    }

    private void updatePersonAddress(final @Nonnull Person person, final @Nonnull AddressDTO dto) {
        if (person.getOtherAddress() != null && addressIsSame(person.getOtherAddress(), dto)) {
            return;
        }

        if (!person.isRegistered() && person.isAddressEditable()) {
            if (person.getOtherAddress() == null) {
                person.setOtherAddress(new Address());
            }

            final Address otherAddress = person.getOtherAddress();
            otherAddress.setStreetAddress(dto.getStreetAddress());
            otherAddress.setPostalCode(dto.getPostalCode());
            otherAddress.setCity(dto.getCity());
            otherAddress.setCountry(dto.getCountry());

            auditService.log("updatePersonAddress", person);
        }
    }

    private static boolean byNameAndPhoneAndEmailSame(final PersonDTO dto, final Person person) {
        return Objects.equals(dto.getEmail(), person.getEmail())
                && Objects.equals(dto.getPhoneNumber(), person.getPhoneNumber())
                && Objects.equals(dto.getByName(), person.getByName());
    }

    private static boolean addressIsSame(final Address address, final AddressDTO dto) {
        return Objects.equals(dto.getStreetAddress(), address.getStreetAddress())
                && Objects.equals(dto.getCity(), address.getCity())
                && Objects.equals(dto.getPostalCode(), address.getPostalCode())
                && Objects.equals(dto.getCountry(), address.getCountry());
    }

    @Override
    protected void delete(final Occupation entity) {
        auditService.log("deleteOccupation", entity.getId());

        assertCanEditOccupationsForCorrectOrganisationTypes(entity.getOrganisation().getOrganisationType());
        assertCoordinatorCanNotEditCoordinator(entity.getOccupationType());
        assertCoordinatorCannotModifyJHTOccupation(entity.getOccupationType());

        super.delete(entity);
    }

    private static void assertCanEditOccupationsForCorrectOrganisationTypes(final OrganisationType organisationType) {
        if (!organisationType.allowGenericOccupationEdit()) {
            throw new AccessDeniedException("Cannot access occupation for organisationType: " + organisationType);
        }
    }

    private void assertCoordinatorCanNotEditCoordinator(final OccupationType... types) {
        if (!activeUserService.isModeratorOrAdmin()
                && Arrays.asList(types).contains(OccupationType.TOIMINNANOHJAAJA)) {

            throw new AccessDeniedException("OccupationType.TOIMINNANOHJAAJA can be edited only by moderator or admin");
        }
    }

    private void assertCoordinatorCannotModifyJHTOccupation(final OccupationType... occupationType) {
        if (activeUserService.isModeratorOrAdmin()) {
            return;
        }

        if (Arrays.stream(occupationType).filter(Objects::nonNull).anyMatch(OccupationType::isJHTOccupation)) {
            throw new AccessDeniedException("JHT occupation can be edited only by moderator or admin");
        }
    }

    private static void assertCanNotCreateForDeceased(final Person person) {
        if (person.isDeceased()) {
            throw new PersonIsDeceasedException("Cannot create occupation for a deceased person");
        }
    }

    private static void assertCanListOccupations(final Organisation organisation) {
        if (!organisation.getOrganisationType().allowListOccupations()) {
            throw new AccessDeniedException("Cannot list occupations for organisationType "
                    + organisation.getOrganisationType());
        }
    }

    @Override
    protected Function<Occupation, OccupationDTO> entityToDTOFunction() {
        return OccupationDTO::createWithPerson;
    }

    @Transactional(readOnly = true)
    public List<OccupationDTO> listOccupations(final Long organisationId) {
        final Organisation organisation =
                requireEntityService.requireOrganisation(organisationId, EntityPermission.READ);
        assertCanListOccupations(organisation);

        return occupationRepository.findNotDeletedByOrganisation(organisation).stream()
                .sorted(OCCUPATION_SORT)
                .map(OccupationDTO::createWithPerson)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PersonDTO> listCandidateForNewOccupation(final long organisationId) {
        final Organisation organisation =
                requireEntityService.requireOrganisation(organisationId, EntityPermission.READ);
        assertCanListOccupations(organisation);

        return occupationRepository.findNotDeletedByOrganisation(organisation).stream()
                .map(Occupation::getPerson)
                .distinct()
                .sorted(CANDIDATE_PERSON_SORT)
                .filter(p -> !p.isDeceased())
                .map(PersonDTO::create)
                .collect(toList());
    }

    private Organisation getOrganisation(final OccupationDTO dto) {
        return getOrganisation(dto.getOrganisationId());
    }

    private Organisation getOrganisation(final Long organisationId) {
        return organisationRepository.getOne(organisationId);
    }
}
