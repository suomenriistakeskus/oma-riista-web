package fi.riista.feature.organization.occupation;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.audit.AuditService;
import fi.riista.feature.account.mobile.MobileOccupationDTO;
import fi.riista.feature.account.mobile.MobileOccupationDTOFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.person.PersonIsDeceasedException;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.organization.occupation.OccupationAuthorization.OccupationPermission.UPDATE_CONTACT_INFO_VISIBILITY;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.ALWAYS;
import static fi.riista.feature.organization.occupation.OccupationContactInfoVisibilityRule.VisibilitySetting.NEVER;
import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class OccupationCrudFeature extends AbstractCrudFeature<Long, Occupation, OccupationDTO> {

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

    @Resource
    private MobileOccupationDTOFactory mobileOccupationDTOFactory;

    @Override
    protected JpaRepository<Occupation, Long> getRepository() {
        return occupationRepository;
    }

    @Override
    protected OccupationDTO toDTO(@Nonnull final Occupation entity) {
        return OccupationDTO.createWithPerson(entity);
    }

    @Transactional(readOnly = true)
    public List<OccupationType> getApplicableOccupationTypes(final long organisationId) {
        final Organisation org = getOrganisation(organisationId);
        return new ArrayList<>(OccupationType.getApplicableTypes(org.getOrganisationType()));
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

        final Organisation organisation = getOrganisation(dto);

        if (entity.isNew()) {
            // Organisation and Person should not be modified after creation
            final Person person = personRepository.getOne(dto.getPerson().getId());

            assertCanNotCreateForDeceased(person);

            entity.setOrganisationAndOccupationType(organisation, dto.getOccupationType());
            entity.setPerson(person);

            entity.setDefaultContactInfoVisibility(organisation.getOrganisationType(), dto.getOccupationType());
        }

        assertCanEditOccupationsForCorrectOrganisationTypes(entity.getOrganisation().getOrganisationType());

        entity.setEndDate(dto.getEndDate());
        entity.setBeginDate(dto.getBeginDate());
        entity.setCallOrder(dto.getCallOrder());
        entity.setAdditionalInfo(dto.getAdditionalInfo());
        entity.setBoardRepresentation(dto.getBoardRepresentation());

        if (dto.getPerson() != null) {
            updatePersonInformation(entity.getPerson(), dto.getPerson());

            if (dto.getPerson().getAddress() != null) {
                updatePersonAddress(entity.getPerson(), dto.getPerson().getAddress());
            }
        }

        final PersonContactInfoDTO substitute = dto.getSubstitute();
        assertCanCreateSubstituteOnlyBoardMembers(dto, substitute);
        assertBoardMemberMustHaveSubstitute(dto, substitute);
        assertRegionalMeetingRepresentativeMustHaveSubstitute(dto, substitute);

        if (substitute != null) {

            final Person substitutePerson = personRepository.getOne(substitute.getId());

            assertCanNotCreateForDeceased(substitutePerson);

            entity.setSubstitute(substitutePerson);
        }
    }

    private void updatePersonInformation(final Person person, final PersonContactInfoDTO dto) {
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

    private static boolean byNameAndPhoneAndEmailSame(final PersonContactInfoDTO dto, final Person person) {
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

    private static void assertCanCreateSubstituteOnlyBoardMembers(final OccupationDTO occupation,
                                                                  final PersonContactInfoDTO substitute) {
        if (substitute != null &&
                !(isBoardOccupationWithSubstitute(occupation)
                        || occupation.getOccupationType() == OccupationType.ALUEKOKOUKSEN_EDUSTAJA)) {
            throw new AccessDeniedException("Substitute not allowed");
        }
    }

    private static boolean isBoardOccupationWithSubstitute(final OccupationDTO occupation) {
        return occupation.getOccupationType().isBoardSpecific() &&
                occupation.getOccupationType() != OccupationType.HALLITUKSEN_VARAJASEN;
    }

    private static void assertBoardMemberMustHaveSubstitute(final OccupationDTO occupation,
                                                            final PersonContactInfoDTO substitute) {
        if (isBoardOccupationWithSubstitute(occupation) &&
                substitute == null) {
            throw new AccessDeniedException("RHY board member must have substitute");
        }
    }

    private static void assertRegionalMeetingRepresentativeMustHaveSubstitute(final OccupationDTO occupation,
                                                                              final PersonContactInfoDTO substitute) {
        if (occupation.getOccupationType() == OccupationType.ALUEKOKOUKSEN_EDUSTAJA &&
                substitute == null) {
            throw new AccessDeniedException("Regional meeting representative must have substitute");
        }
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
    public List<OccupationDTO> listOccupationsByType(final Long organisationId,
                                                     final OccupationType occupationType) {
        final Organisation organisation =
                requireEntityService.requireOrganisation(organisationId, EntityPermission.READ);
        assertCanListOccupations(organisation);

        return occupationRepository.findNotDeletedByOrganisationAndType(organisation, occupationType).stream()
                .sorted(OCCUPATION_SORT)
                .map(OccupationDTO::createWithPerson)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PersonContactInfoDTO> listCandidatesForNewOccupation(final long organisationId) {
        final Organisation organisation =
                requireEntityService.requireOrganisation(organisationId, EntityPermission.READ);
        assertCanListOccupations(organisation);

        return occupationRepository.findNotDeletedByOrganisation(organisation).stream()
                .map(Occupation::getPerson)
                .distinct()
                .sorted(CANDIDATE_PERSON_SORT)
                .filter(p -> !p.isDeceased())
                .map(PersonContactInfoDTO::create)
                .collect(toList());
    }

    @Transactional
    public void updateContactInfoVisibility(final List<OccupationContactInfoVisibilityDTO> dtoList) {
        dtoList.forEach(dto -> {
            final Occupation occupation = requireEntityService.requireOccupation(dto.getId(), UPDATE_CONTACT_INFO_VISIBILITY);
            final Organisation organisation = occupation.getOrganisation();

            assertContactInfoVisibilitySettings(organisation.getOrganisationType(), occupation.getOccupationType(), dto);

            occupation.setNameVisibility(dto.isNameVisibility());
            occupation.setPhoneNumberVisibility(dto.isPhoneNumberVisibility());
            occupation.setEmailVisibility(dto.isEmailVisibility());
        });
    }

    private Organisation getOrganisation(final OccupationDTO dto) {
        return getOrganisation(dto.getOrganisationId());
    }

    private Organisation getOrganisation(final Long organisationId) {
        return organisationRepository.getOne(organisationId);
    }

    private static void assertContactInfoVisibilitySettings(final OrganisationType organisationType,
                                                            final OccupationType occupationType,
                                                            final OccupationContactInfoVisibilityDTO dto) {
        final OccupationContactInfoVisibilityRule rule =
                OccupationContactInfoVisibilityRuleMapping.get(organisationType, occupationType);

        assertContactInfoVisibilitySetting(dto.isNameVisibility(), rule.getNameVisibility());
        assertContactInfoVisibilitySetting(dto.isPhoneNumberVisibility(), rule.getPhoneNumberVisibility());
        assertContactInfoVisibilitySetting(dto.isEmailVisibility(), rule.getEmailVisibility());
    }

    private static void assertContactInfoVisibilitySetting(final boolean visibility,
                                                           final VisibilitySetting setting) {
        if (visibility && setting == NEVER || !visibility && setting == ALWAYS) {
            throw new IllegalArgumentException("Incorrect contact info visibility");
        }
    }

    @Transactional(readOnly = true)
    public List<MobileOccupationDTO> listMyClubMemberships() {
        final Person person = activeUserService.requireActivePerson();
        final List<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOrganisationTypes(person, singleton(OrganisationType.CLUB));

        return mobileOccupationDTOFactory.create(occupations);
    }
}
