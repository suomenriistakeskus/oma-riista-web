package fi.riista.feature.organization.jht.training;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.EHDOLLA;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.NIMITETTY;
import static java.util.stream.Collectors.toList;

@Component
public class JHTTrainingCrudFeature extends AbstractCrudFeature<Long, JHTTraining, JHTTrainingDTO> {

    @Resource
    private JHTTrainingRepository jhtTrainingRepository;

    @Resource
    private JHTTrainingDTOTransformer jhtTrainingDTOTransformer;

    @Resource
    private PersonLookupService personLookupService;

    @Resource
    private OccupationNominationRepository occupationNominationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Override
    protected JpaRepository<JHTTraining, Long> getRepository() {
        return jhtTrainingRepository;
    }

    @Override
    protected JHTTrainingDTO toDTO(@Nonnull final JHTTraining entity) {
        return jhtTrainingDTOTransformer.apply(entity);
    }

    @Override
    protected void updateEntity(final JHTTraining entity, final JHTTrainingDTO dto) {
        if (entity.isNew()) {
            entity.setOccupationType(dto.getOccupationType());
            entity.setTrainingDate(dto.getTrainingDate());
            entity.setTrainingLocation(dto.getTrainingLocation());
            entity.setTrainingType(dto.getTrainingType());
            entity.setPerson(personLookupService.findById(dto.getPerson().getId())
                    .orElseThrow(() -> new NotFoundException("No such person")));
        }
    }

    @Override
    protected void delete(final JHTTraining entity) {
        Preconditions.checkState(entity.getExternalId() == null,
                "cannot delete when externalId != null");
        Preconditions.checkState(entity.getTrainingType() == JHTTraining.TrainingType.LAHI,
                "cannot delete when trainingType != LAHI");
        super.delete(entity);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_MODERATOR,ROLE_COORDINATOR')")
    public Page<JHTTrainingDTO> search(final JHTTrainingSearchDTO dto) {
        final Sort sortSpec = new JpaSort(Sort.Direction.DESC, JHTTraining_.trainingDate)
                .and(Sort.Direction.ASC, JpaSort.path(JHTTraining_.person).dot(Person_.lastName))
                .and(Sort.Direction.ASC, JpaSort.path(JHTTraining_.person).dot(Person_.firstName));
        final PageRequest pageRequest = new PageRequest(dto.getPage(), dto.getPageSize(), sortSpec);
        final Optional<Person> person = getPerson(dto);
        final Optional<Riistanhoitoyhdistys> rhy = getRhy(dto.getRhyCode());

        if (!canSearch(dto, person, rhy)) {
            return new PageImpl<>(Collections.emptyList());
        }

        final Page<JHTTraining> trainingList = jhtTrainingRepository.searchPage(
                pageRequest, dto.getSearchType(),
                dto.getOccupationType(), dto.getTrainingType(), dto.getTrainingLocation(),
                rhy.orElse(null), person.orElse(null),
                dto.getBeginDate(), dto.getEndDate());

        final Page<JHTTrainingDTO> dtoList = jhtTrainingDTOTransformer.apply(trainingList, pageRequest);

        final List<Long> selectedPersonIds = occupationNominationRepository.findPersonIdByOccupationTypeAndNominationStatusIn(
                dto.getOccupationType(), EnumSet.of(EHDOLLA, ESITETTY));

        final List<Long> acceptedPersonIds = occupationNominationRepository.findPersonIdByOccupationTypeAndNominationStatusIn(
                dto.getOccupationType(), EnumSet.of(NIMITETTY));

        for (final JHTTrainingDTO trainingDTO : dtoList.getContent()) {
            final Long personId = trainingDTO.getPerson().getId();

            trainingDTO.setNominated(selectedPersonIds.contains(personId));
            trainingDTO.setAccepted(acceptedPersonIds.contains(personId));
        }

        return dtoList;
    }

    private static boolean canSearch(final JHTTrainingSearchDTO dto,
                                     final Optional<Person> person,
                                     final Optional<Riistanhoitoyhdistys> rhy) {
        final boolean searchByPerson = dto.getSearchType() == JHTTrainingSearchDTO.SearchType.PERSON;
        final boolean searchByLocation = dto.getSearchType() == JHTTrainingSearchDTO.SearchType.TRAINING_LOCATION;

        return searchByLocation ||
                (person.isPresent() && searchByPerson) ||
                (rhy.isPresent() && !searchByPerson);
    }

    private Optional<Person> getPerson(@Nonnull final JHTTrainingSearchDTO dto) {
        if (StringUtils.hasText(dto.getHunterNumber())) {
            return personLookupService.findByHunterNumber(dto.getHunterNumber());
        }

        if (StringUtils.hasText(dto.getSsn())) {
            return personLookupService.findBySsnNoFallback(dto.getSsn());
        }

        return Optional.empty();
    }

    private Optional<Riistanhoitoyhdistys> getRhy(final String officialCode) {
        return Optional.ofNullable(officialCode).map(riistanhoitoyhdistysRepository::findByOfficialCode);
    }

    @Transactional
    public OccupationNomination propose(final Long jhtTrainingId,
                                        final Long rhyOrganisationId) {
        final JHTTraining jhtTraining = requireEntity(jhtTrainingId, JHTTrainingAuthorization.Permission.PROPOSE);
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyOrganisationId, EntityPermission.READ);

        final Person person = jhtTraining.getPerson();
        final OccupationType occupationType = jhtTraining.getOccupationType();

        final Optional<OccupationNomination> existingNomination = occupationNominationRepository
                .findByPersonAndRhyAndOccupationType(person, rhy, occupationType).stream()
                .filter(o -> !o.getNominationStatus().isFinal())
                .findAny();

        if (existingNomination.isPresent()) {
            return existingNomination.get();
        }

        final OccupationNomination occupationNomination = new OccupationNomination();

        occupationNomination.setNominationStatus(EHDOLLA);
        occupationNomination.setOccupationType(occupationType);
        occupationNomination.setPerson(person);
        occupationNomination.setRhy(rhy);
        occupationNomination.setRhyPerson(activeUserService.getActiveUser().getPerson());

        return occupationNominationRepository.save(occupationNomination);
    }

    @Transactional(readOnly = true)
    public List<JHTTrainingDTO> listMine() {
        final SystemUser activeUser = activeUserService.getActiveUser();

        if (activeUser.getRole() == SystemUser.Role.ROLE_USER && activeUser.getPerson() != null) {
            final List<JHTTraining> byPerson = jhtTrainingRepository.findByPerson(activeUser.getPerson());

            return jhtTrainingDTOTransformer.apply(byPerson.stream()
                    .filter(training -> !training.isArtificialTraining())
                    .collect(toList()));
        }

        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    public List<JHTTrainingDTO> listForPerson(final long personId) {
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);

        return jhtTrainingDTOTransformer.apply(jhtTrainingRepository.findByPerson(person));
    }
}
