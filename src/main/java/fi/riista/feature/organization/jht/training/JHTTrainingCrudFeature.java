package fi.riista.feature.organization.jht.training;

import com.google.common.base.Preconditions;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.RiistakeskuksenAlueRepository;
import fi.riista.feature.organization.jht.nomination.OccupationNomination;
import fi.riista.feature.organization.jht.nomination.OccupationNominationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.person.PersonNotFoundException;
import fi.riista.feature.organization.person.Person_;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.security.EntityPermission;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.EHDOLLA;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.ESITETTY;
import static fi.riista.feature.organization.jht.nomination.OccupationNomination.NominationStatus.NIMITETTY;
import static fi.riista.feature.organization.occupation.Occupation.FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION;
import static java.util.Optional.ofNullable;
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
    private RiistakeskuksenAlueRepository riistakeskuksenAlueRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

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

            final Long personId = dto.getPerson().getId();
            entity.setPerson(personLookupService
                    .findById(personId, JHTTraining.FOREIGN_PERSON_ELIGIBLE_FOR_JHT_TRAINING)
                    .orElseThrow(() -> PersonNotFoundException.byPersonId(personId)));
        }
    }

    @Override
    protected void delete(final JHTTraining entity) {
        checkState(entity.getExternalId() == null, "cannot delete when externalId != null");
        checkState(entity.getTrainingType() == JHTTraining.TrainingType.LAHI,
                "cannot delete when trainingType != LAHI");
        super.delete(entity);
    }

    @Transactional(readOnly = true)
    public Page<JHTTrainingDTO> search(final JHTTrainingSearchDTO dto) {
        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();

        final Sort sortSpec = JpaSort.of(Sort.Direction.DESC, JHTTraining_.trainingDate)
                .and(Sort.Direction.ASC, JpaSort.path(JHTTraining_.person).dot(Person_.lastName))
                .and(Sort.Direction.ASC, JpaSort.path(JHTTraining_.person).dot(Person_.firstName));
        final PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getPageSize(), sortSpec);
        final Optional<Person> person = getPerson(dto);
        final Optional<RiistakeskuksenAlue> rka = getRka(dto.getAreaCode());
        final Optional<Riistanhoitoyhdistys> rhy = getRhy(dto.getRhyCode());

        Preconditions.checkArgument(rhy.isPresent()
                        || !rka.isPresent()
                        || activeUserService.isModeratorOrAdmin(),
                "Only moderators can search by rka.");

        if (!canSearch(dto, person, rka, rhy)) {
            return new PageImpl<>(Collections.emptyList());
        }

        final Page<JHTTraining> trainingList = jhtTrainingRepository.searchPage(
                pageRequest, dto.getSearchType(),
                dto.getOccupationType(), dto.getTrainingType(), dto.getTrainingLocation(),
                rka.orElse(null), rhy.orElse(null), person.orElse(null),
                dto.getBeginDate(), dto.getEndDate());

        final Page<JHTTrainingDTO> dtoList = jhtTrainingDTOTransformer.apply(trainingList, pageRequest);

        final List<Long> selectedPersonIds =
                occupationNominationRepository.findPersonIdByOccupationTypeAndNominationStatusIn(
                        dto.getOccupationType(), EnumSet.of(EHDOLLA, ESITETTY));

        final List<Long> acceptedPersonIds =
                occupationNominationRepository.findPersonIdByOccupationTypeAndNominationStatusIn(
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
                                     final Optional<RiistakeskuksenAlue> rka,
                                     final Optional<Riistanhoitoyhdistys> rhy) {
        final boolean searchByPerson = dto.getSearchType() == JHTTrainingSearchDTO.SearchType.PERSON;
        final boolean searchByLocation = dto.getSearchType() == JHTTrainingSearchDTO.SearchType.TRAINING_LOCATION;
        final boolean organisationPresent = rka.isPresent() || rhy.isPresent();
        return searchByLocation ||
                (person.isPresent() && searchByPerson) ||
                (organisationPresent && !searchByPerson);
    }

    private Optional<Person> getPerson(@Nonnull final JHTTrainingSearchDTO dto) {
        if (StringUtils.hasText(dto.getHunterNumber())) {
            return personLookupService
                    .findByHunterNumber(dto.getHunterNumber(), JHTTraining.FOREIGN_PERSON_ELIGIBLE_FOR_JHT_TRAINING);
        }

        if (StringUtils.hasText(dto.getSsn())) {
            return personLookupService.findBySsnNoFallback(dto.getSsn());
        }

        return Optional.empty();
    }

    private Optional<Riistanhoitoyhdistys> getRhy(final String officialCode) {
        return ofNullable(officialCode).map(riistanhoitoyhdistysRepository::findByOfficialCode);
    }

    private Optional<RiistakeskuksenAlue> getRka(final String officialCode) {
        return ofNullable(officialCode).map(riistakeskuksenAlueRepository::findByOfficialCode);
    }

    @Transactional
    public OccupationNomination propose(final Long jhtTrainingId, final Long rhyOrganisationId) {
        final JHTTraining jhtTraining = requireEntity(jhtTrainingId, JHTTrainingAuthorization.Permission.PROPOSE);
        final Riistanhoitoyhdistys rhy =
                requireEntityService.requireRiistanhoitoyhdistys(rhyOrganisationId, EntityPermission.READ);

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
        occupationNomination.setRhyPerson(activeUserService.requireActiveUser().getPerson());

        return occupationNominationRepository.save(occupationNomination);
    }

    @Transactional(readOnly = true)
    public List<JHTTrainingDTO> listMine() {
        final SystemUser activeUser = activeUserService.requireActiveUser();

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

    @Transactional(readOnly = true)
    public Map<Long, LocalDate> lastTrainings(final List<Long> personIds,
                                              final OccupationType occupationType) {
        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();
        return jhtTrainingRepository.findLatestTrainingDatesForOccupation(personIds, occupationType);
    }

    @Transactional
    public void createMulti(final JHTMultiTrainingDTO multiTrainingDto) {
        personLookupService.findByHunterNumberIn(multiTrainingDto.getHunterNumbers(), FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION)
                .forEach(person -> {
                    final JHTTrainingDTO dto = new JHTTrainingDTO();
                    dto.setPerson(JHTTrainingDTO.PersonDTO.create(person));
                    dto.setTrainingType(multiTrainingDto.getTrainingType());
                    dto.setOccupationType(multiTrainingDto.getOccupationType());
                    dto.setTrainingDate(multiTrainingDto.getTrainingDate());
                    dto.setTrainingLocation(multiTrainingDto.getTrainingLocation());

                    create(dto);
                });
    }
}
