package fi.riista.feature.huntingclub.members.club;

import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.members.HuntingClubOccupationDTOTransformer;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationSort;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubMemberCrudFeature extends AbstractCrudFeature<Long, Occupation, OccupationDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    protected PersonLookupService personLookupService;

    @Resource
    private ContactPersonCanExitClubService contactPersonCanExitClubService;

    @Resource
    private HuntingClubOccupationDTOTransformer clubOccupationDTOTransformer;

    @Resource
    protected OccupationRepository occupationRepository;

    @Resource
    protected HuntingClubRepository huntingClubRepository;

    @Override
    protected JpaRepository<Occupation, Long> getRepository() {
        return occupationRepository;
    }

    @Override
    protected OccupationDTO toDTO(@Nonnull final Occupation entity) {
        return clubOccupationDTOTransformer.apply(entity);
    }

    @Override
    @Transactional
    public OccupationDTO create(final OccupationDTO dto) {
        return occupationRepository.alreadyExists(dto) ? dto : super.create(dto);
    }

    @Transactional(readOnly = true)
    public boolean isLocked(final long occupationId) {
        return contactPersonCanExitClubService.isContactPersonLocked(occupationRepository.getOne(occupationId));
    }

    @Override
    protected void delete(Occupation entity) {
        contactPersonCanExitClubService.assertContactPersonNotLocked(entity);

        if (!entity.isDeleted()) {
            entity.setEndDate(DateUtil.today());
            entity.softDelete();
        }

        // delete all group occupations too
        occupationRepository.findNotDeletedByParentOrganisationAndPerson(
                entity.getOrganisation(), entity.getPerson()).forEach(groupOccupation -> {
            groupOccupation.setEndDate(DateUtil.today());
            groupOccupation.softDelete();
        });
    }

    @Override
    protected void updateEntity(final Occupation entity, final OccupationDTO dto) {
        if (entity.isNew()) {
            final HuntingClub org = huntingClubRepository.getOne(dto.getOrganisationId());
            final Person person = personLookupService.findById(dto.getPersonId())
                    .orElseThrow(() -> new NotFoundException("Person not found by personId: " + dto.getId()));

            entity.setOrganisationAndOccupationType(org, dto.getOccupationType());
            entity.setPerson(person);
            entity.setBeginDate(org.getOrganisationType().getBeginDateForNewOccupation());
        }
    }

    @Transactional
    public OccupationDTO updateOccupationType(final long id, final OccupationType occupationType) {
        final Occupation existingOccupation = requireEntity(id, EntityPermission.UPDATE);

        // changing occupation type to same is unnecessary
        if (Objects.equals(occupationType, existingOccupation.getOccupationType())) {
            return clubOccupationDTOTransformer.apply(existingOccupation);
        }

        contactPersonCanExitClubService.assertContactPersonNotLocked(existingOccupation);

        if (!existingOccupation.isDeleted()) {
            existingOccupation.setEndDate(DateUtil.today());
            existingOccupation.softDelete();
        }

        final Occupation newOccupation = new Occupation(
                existingOccupation.getPerson(), existingOccupation.getOrganisation(), occupationType);
        newOccupation.setBeginDate(existingOccupation.getBeginDate());
        newOccupation.setContactInfoShare(existingOccupation.getContactInfoShare());

        return clubOccupationDTOTransformer.apply(occupationRepository.saveAndFlush(newOccupation));
    }

    @Transactional(readOnly = true)
    public List<OccupationDTO> listMembers(final long orgId) {
        final Organisation org = requireEntityService.requireOrganisation(orgId, EntityPermission.READ);

        final Comparator<Occupation> sort = OccupationSort.BY_TYPE
                .thenComparing(OccupationSort.BY_CALL_ORDER)
                .thenComparing(OccupationSort.BY_LAST_NAME)
                .thenComparing(OccupationSort.BY_BYNAME);

        final List<Occupation> occupations = occupationRepository.findActiveByOrganisation(org);

        return clubOccupationDTOTransformer.apply(occupations.stream().sorted(sort).collect(toList()));
    }

    @Transactional
    public void updatePrimaryContact(Long orgId, Long id) {
        final Organisation organisation = requireEntityService.requireOrganisation(orgId, EntityPermission.UPDATE);

        occupationRepository.findActiveByOrganisationAndOccupationType(organisation, OccupationType.SEURAN_YHDYSHENKILO)
                .forEach(leader -> leader.setCallOrder(leader.getId().equals(id) ? 0 : null));
    }

    @Transactional
    public void updateContactInfoSharing(final List<ContactInfoShareUpdateDTO> updates) {
        final Person person = activeUserService.getActiveUser().getPerson();

        updates.forEach(dto -> {
            final Occupation occupation = occupationRepository.getOne(dto.getOccupationId());

            if (!person.equals(occupation.getPerson())) {
                throw new AccessDeniedException(
                        "Occupations contact share can be updated only by the occupation holder himself");
            }

            occupation.setContactInfoShare(dto.getShare());
            occupationRepository.findNotDeletedByParentOrganisationAndPerson(occupation.getOrganisation(), person)
                    .forEach(groupOccupation -> groupOccupation.setContactInfoShare(dto.getShare()));
        });
    }
}
