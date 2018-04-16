package fi.riista.feature.organization.jht.nomination;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.organization.jht.JHTPeriod;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.Person_;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OccupationNominationCrudFeature extends AbstractCrudFeature<Long, OccupationNomination, OccupationNominationDTO> {

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    @Resource
    private OccupationNominationDTOTransformer occupationNominationDTOTransformer;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Override
    protected JpaRepository<OccupationNomination, Long> getRepository() {
        return occupationNominationRepository;
    }

    @Override
    protected OccupationNominationDTO toDTO(@Nonnull final OccupationNomination entity) {
        return occupationNominationDTOTransformer.apply(entity);
    }

    @Override
    protected void updateEntity(final OccupationNomination entity,
                                final OccupationNominationDTO dto) {
    }

    @Transactional(readOnly = true)
    public Map<OccupationNomination.NominationStatus, Long> count(final String rhyOfficialCode,
                                                                  final OccupationType occupationType) {
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.findByOfficialCode(rhyOfficialCode);

        if (activeUserService.checkHasPermission(rhy, EntityPermission.READ)) {
            return occupationNominationRepository.countByNominationStatus(rhy, occupationType);
        }
        return Collections.emptyMap();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public Page<OccupationNominationDTO> search(final OccupationNominationSearchDTO dto) {
        final Sort sortSpec =
                new JpaSort(Sort.Direction.ASC, JpaSort.path(OccupationNomination_.person).dot(Person_.lastName))
                        .and(Sort.Direction.ASC, JpaSort.path(OccupationNomination_.person).dot(Person_.firstName));
        final PageRequest pageRequest = new PageRequest(dto.getPage(), dto.getPageSize(), sortSpec);
        return occupationNominationDTOTransformer.apply(find(dto, pageRequest), pageRequest);
    }

    private Page<OccupationNomination> find(final OccupationNominationSearchDTO dto, final Pageable pageRequest) {
        final Riistanhoitoyhdistys rhy;
        final Person person;
        final boolean isPersonSearch;
        final boolean isRhySearch;

        if (dto.getRhyCode() != null) {
            isRhySearch = true;
            rhy = riistanhoitoyhdistysRepository.findByOfficialCode(dto.getRhyCode());
        } else {
            isRhySearch = false;
            rhy = null;
        }

        if (dto.getHunterNumber() != null) {
            isPersonSearch = true;
            person = personLookupService.findByHunterNumber(dto.getHunterNumber()).orElse(null);
        } else if (dto.getSsn() != null) {
            isPersonSearch = true;
            person = personLookupService.findBySsnNoFallback(dto.getSsn().toUpperCase()).orElse(null);
        } else {
            isPersonSearch = false;
            person = null;
        }

        if (isPersonSearch && person == null || isRhySearch && rhy == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        if (!activeUserService.isModeratorOrAdmin() && !dto.getNominationStatus().isFinal()) {
            if (isRhySearch) {
                // Check user is coordinator
                activeUserService.assertHasPermission(rhy, EntityPermission.READ);

            } else {
                throw new IllegalArgumentException("Cannot search non-final nominations without RHY");
            }
        }

        return occupationNominationRepository.searchPage(
                pageRequest, dto.getOccupationType(), dto.getNominationStatus(),
                rhy, person, dto.getBeginDate(), dto.getEndDate());
    }

    @Transactional
    public void propose(final Long occupationNominationId) {
        final OccupationNomination entity = requireEntity(occupationNominationId,
                OccupationNominationAuthorization.Permission.PROPOSE);

        entity.propose(activeUserService.getActiveUser().getPerson());
    }

    @Transactional
    public void cancelPropose(final Long occupationNominationId) {
        final OccupationNomination occupationNomination =
                requireEntity(occupationNominationId, OccupationNominationAuthorization.Permission.CANCEL);

        if (!occupationNomination.canCancel()) {
            throw new IllegalStateException("cannot cancel nomination withs status "
                    + occupationNomination.getNominationStatus());
        }

        occupationNominationRepository.delete(occupationNominationId);
    }

    @Transactional
    public void accept(final Long occupationNominationId, final JHTPeriod jhtPeriod) {
        Preconditions.checkState(activeUserService.isModeratorOrAdmin(), "user should be admin or moderator");

        final OccupationNomination entity = requireEntity(occupationNominationId,
                OccupationNominationAuthorization.Permission.ACCEPT);

        final Occupation occupation = createOccupationForNomination(entity, jhtPeriod);

        entity.acceptByModerator(activeUserService.getActiveUser(), occupation);
    }

    @Transactional
    public void reject(final Long occupationNominationId) {
        Preconditions.checkState(activeUserService.isModeratorOrAdmin(), "user should be admin or moderator");

        final OccupationNomination entity = requireEntity(occupationNominationId,
                OccupationNominationAuthorization.Permission.REJECT);

        entity.rejectByModerator(activeUserService.getActiveUser());
    }

    private Occupation createOccupationForNomination(@Nonnull final OccupationNomination occupationNomination,
                                                     @Nonnull final JHTPeriod occupationPeriod) {
        Objects.requireNonNull(occupationNomination, "occupationNomination is null");
        Objects.requireNonNull(occupationNomination, "occupationPeriod is null");

        Preconditions.checkArgument(occupationPeriod.validateBeforeAndEnd(), "invalid jhtPeriod");
        Preconditions.checkArgument(occupationPeriod.validateEndDate(), "invalid jhtPeriod");
        Preconditions.checkArgument(OccupationType.isValidJhtOccupationType(occupationNomination.getOccupationType()),
                "only RHY occupationNominations are supported");

        final List<Occupation> activeRhyOccupations = occupationRepository.findActiveByOrganisationAndPerson(
                occupationNomination.getRhy(), occupationNomination.getPerson());

        // Expire previous occupations
        activeRhyOccupations.stream()
                .filter(o -> o.getOccupationType() == occupationNomination.getOccupationType())
                .forEach(o -> o.setEndDate(occupationPeriod.getBeginDate().minusDays(1)));

        // Make sure no duplicate occupations are created in the future
        occupationRepository.deleteOccupationInFuture(
                occupationNomination.getRhy(),
                occupationNomination.getOccupationType(),
                occupationNomination.getPerson());

        // Create new occupation
        final Occupation jhtOccupation = new Occupation(
                occupationNomination.getPerson(),
                occupationNomination.getRhy(),
                occupationNomination.getOccupationType());

        jhtOccupation.setBeginDate(occupationPeriod.getBeginDate());
        jhtOccupation.setEndDate(occupationPeriod.getEndDate());

        return occupationRepository.save(jhtOccupation);
    }
}
