package fi.riista.feature.gamediary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.harvest.HarvestDTO;
import fi.riista.feature.gamediary.observation.ObservationDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.ObservationDTOTransformer;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldPresenceValidator;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDayService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import javaslang.Tuple2;
import org.joda.time.Interval;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static fi.riista.feature.gamediary.GameDiarySpecs.acceptedToPermit;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorAndRejectedForPermit;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorButNotObserver;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorButNotShooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorOrShooterAndHarvestReportRequiredAndMissing;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.permitContactPersonAndProposedToPermit;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@Service
public class GameDiaryFeature extends AbstractGameDiaryFeature {

    private static final HarvestSpecVersion HARVEST_SPEC_VERSION = HarvestSpecVersion.MOST_RECENT;
    private static final int OBSERVATION_FIELDS_METADATA_VERSION = ObservationSpecVersion.MOST_RECENT.toIntValue();

    @Resource
    private GroupHuntingDayService clubHuntingDayService;

    @Resource
    private HarvestDTOTransformer harvestDtoTransformer;

    @Resource
    private ObservationDTOTransformer observationDtoTransformer;

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getObservationFieldMetadata() {
        return getObservationFieldMetadata(OBSERVATION_FIELDS_METADATA_VERSION);
    }

    @Transactional(readOnly = true)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSpecies(final int officialCode) {
        return getObservationFieldMetadataForSpecies(officialCode, OBSERVATION_FIELDS_METADATA_VERSION);
    }

    @Transactional(readOnly = true)
    public List<GameDiaryEntryDTO> listDiaryEntriesForActiveUser(final Interval interval,
                                                                 final boolean reportedForOthers) {
        final Person person = activeUserService.requireActivePerson();

        final Specification<Harvest> harvestPersonSpec =
                reportedForOthers ? authorButNotShooter(person) : shooter(person);

        final Specification<Observation> observationPersonSpec =
                reportedForOthers ? authorButNotObserver(person) : observer(person);

        final List<Harvest> harvests = harvestRepository.findAll(JpaSpecs.and(
                harvestPersonSpec,
                JpaSpecs.withinInterval(GameDiaryEntry_.pointOfTime, interval)));

        final List<Observation> observations = observationRepository.findAll(JpaSpecs.and(
                observationPersonSpec,
                JpaSpecs.withinInterval(GameDiaryEntry_.pointOfTime, interval)));

        return F.concat(harvestDtoTransformer.apply(harvests), observationDtoTransformer.apply(observations));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public long countAllHarvestsRequiringAction() {
        final Person person = activeUserService.requireActivePerson();

        return harvestRepository.count(JpaSpecs.or(
                authorOrShooterAndHarvestReportRequiredAndMissing(person),
                authorAndRejectedForPermit(person),
                permitContactPersonAndProposedToPermit(person)));
    }

    @Transactional(readOnly = true)
    public HarvestDTO getHarvest(final Long id) {
        return harvestDtoTransformer.apply(requireEntityService.requireHarvest(id, EntityPermission.READ));
    }

    @Transactional
    public HarvestDTO createHarvest(@Nonnull final HarvestDTO dto) {
        Objects.requireNonNull(dto);

        final Harvest harvest = new Harvest();
        harvest.setFromMobile(false);

        final SystemUser activeUser = activeUserService.getActiveUser();
        final boolean moderator = activeUser.isModeratorOrAdmin();

        updateMutableFields(harvest, dto, activeUser, true, moderator, true);

        // Default to manual if geolocation source not explicitly given
        if (harvest.getGeoLocation().getSource() == null) {
            harvest.getGeoLocation().setSource(GeoLocation.Source.MANUAL);
        }

        activeUserService.assertHasPermission(harvest, EntityPermission.CREATE);

        harvestRepository.saveAndFlush(harvest);

        harvestSpecimenService.addSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), HARVEST_SPEC_VERSION);

        dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(harvest, uuid));

        return harvestDtoTransformer.apply(harvest);
    }

    @Transactional
    public HarvestDTO updateHarvest(@Nonnull final HarvestDTO dto) {
        Objects.requireNonNull(dto);

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final SystemUser activeUser = activeUserService.getActiveUser();
        final Person currentPerson = activeUser.getPerson();
        final boolean moderator = activeUser.isModeratorOrAdmin();

        final boolean authorOrShooter = !moderator && harvest.isAuthorOrActor(currentPerson);
        final boolean businessFieldsCanBeUpdated = canBusinessFieldsBeUpdated(currentPerson, harvest, moderator);

        updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated, moderator, authorOrShooter);

        if (businessFieldsCanBeUpdated) {
            final Tuple2<?, Boolean> specimenResult =
                    harvestSpecimenService.setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), HARVEST_SPEC_VERSION);

            if (specimenResult._2) {
                harvest.forceRevisionUpdate();
            }
        }

        if (authorOrShooter) {
            updateImages(harvest, dto.getImageIds());
        }

        return harvestDtoTransformer.apply(harvestRepository.saveAndFlush(harvest));
    }

    private void updateMutableFields(final Harvest harvest,
                                     final HarvestDTO dto,
                                     final SystemUser activeUser,
                                     final boolean businessFieldsCanBeUpdated,
                                     final boolean moderator,
                                     final boolean authorOrShooter) {
        final Person currentPerson = activeUser.getPerson();

        if (businessFieldsCanBeUpdated) {
            gameDiaryService.setAuthorAndActor(harvest, dto, activeUser);

            final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());
            final Date pointOfTime = DateUtil.toDateNullSafe(dto.getPointOfTime());
            final GeoLocation location = dto.getGeoLocation();

            harvest.setSpecies(species);
            harvest.setPointOfTime(pointOfTime);
            harvest.updateGeoLocation(location, gisQueryService);
            harvest.setAmount(dto.getAmount());
            harvest.setPermittedMethod(dto.getPermittedMethod());

            final HarvestReportFields reportFields = Optional.ofNullable(dto.getFields())
                    .map(HasID::getId)
                    .filter(Objects::nonNull)
                    .map(harvestReportFieldsRepository::getOne)
                    .orElse(null);
            harvest.setHarvestReportFields(reportFields);

            prepareMutatorForPermitStateAndReportRequirement(dto, species, location, currentPerson, moderator)
                    .accept(harvest, HARVEST_SPEC_VERSION);

            if (dto.getHuntingDayId() != null) {
                clubHuntingDayService.linkDiaryEntryToHuntingDay(harvest, dto.getHuntingDayId(), currentPerson);
            }
        }

        if (authorOrShooter) {
            harvest.setDescription(dto.getDescription());
        }

        if (moderator) {
            harvest.setModeratorOverride(true);
        }
    }

    @Transactional(readOnly = true)
    public ObservationDTO getObservation(final Long id) {
        return observationDtoTransformer.apply(requireEntityService.requireObservation(id, EntityPermission.READ));
    }

    @Transactional
    public ObservationDTO createObservation(@Nonnull final ObservationDTO dto) {
        Objects.requireNonNull(dto);

        final SystemUser activeUser = activeUserService.getActiveUser();
        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());

        final ObservationFieldPresenceValidator validator = getObservationFieldsValidator(species);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final Observation observation = new Observation();
        observation.setFromMobile(false);

        updateMutableFields(observation, species, activeUser, dto, true, true);

        activeUserService.assertHasPermission(observation, EntityPermission.CREATE);

        observationRepository.saveAndFlush(observation);

        final List<ObservationSpecimen> specimens = F.isNullOrEmpty(dto.getSpecimens())
                ? null
                : observationSpecimenService.addSpecimens(observation, dto.getAmount(), dto.getSpecimens());

        // Validate entity graph after specimens are persisted. Amount field is excluded because in
        // moose/mooselike-within-moose-hunting cases the field is computed automatically even
        // though in the REST-API it is actually prohibited (because of separate moose amount
        // fields are used).
        validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());

        dto.getImageIds().forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(observation, uuid));

        return observationDtoTransformer.apply(observation);
    }

    @Transactional
    public ObservationDTO updateObservation(@Nonnull final ObservationDTO dto) {
        Objects.requireNonNull(dto);

        final Observation observation = requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());

        final ObservationFieldPresenceValidator validator = getObservationFieldsValidator(species);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        DtoUtil.assertNoVersionConflict(observation, dto);

        final SystemUser activeUser = activeUserService.getActiveUser();
        final Person currentPerson = activeUser.getPerson();
        final boolean moderator = activeUser.isModeratorOrAdmin();
        final boolean authorOrObserver = !moderator && observation.isAuthorOrActor(currentPerson);
        final boolean businessFieldsCanBeUpdated = canBusinessFieldsBeUpdated(observation);

        updateMutableFields(observation, species, activeUser, dto, businessFieldsCanBeUpdated, authorOrObserver);

        if (businessFieldsCanBeUpdated) {
            final List<ObservationSpecimen> specimens;

            if (dto.getAmount() != null) {
                final Tuple2<List<ObservationSpecimen>, Boolean> specimenResult =
                        observationSpecimenService.setSpecimens(observation, dto.getAmount(), dto.getSpecimens());
                specimens = specimenResult._1;

                if (specimenResult._2) {
                    observation.forceRevisionUpdate();
                }
            } else {
                specimens = null;
                observationSpecimenService.deleteAllSpecimens(observation);
            }

            // Validate entity graph after specimens are persisted. Amount field is excluded because in
            // moose/mooselike-within-moose-hunting cases the field is computed automatically even
            // though in the REST-API it is actually prohibited (because of separate moose amount
            // fields are used).
            validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());
        }

        if (authorOrObserver) {
            updateImages(observation, dto.getImageIds());
        }

        return observationDtoTransformer.apply(observationRepository.saveAndFlush(observation));
    }

    private void updateMutableFields(
            final Observation observation,
            final GameSpecies species,
            final SystemUser activeUser,
            final ObservationDTO dto,
            final boolean businessFieldsCanBeUpdated,
            final boolean authorOrObserver) {

        super.updateMutableFields(observation, species, dto, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            gameDiaryService.setAuthorAndActor(observation, dto, activeUser);

            if (dto.getHuntingDayId() != null) {
                if (!dto.observedWithinMooseHunting()) {
                    throw new ObservationCanBeLinkedToHuntingDayOnlyWithinMooseHuntingException();
                }
                clubHuntingDayService.linkDiaryEntryToHuntingDay(observation, dto.getHuntingDayId(), activeUser.getPerson());
            }
        }

        if (authorOrObserver) {
            observation.setDescription(dto.getDescription());
        }

        if (activeUser.isModeratorOrAdmin()) {
            observation.setModeratorOverride(true);
        }
    }

    @Transactional(rollbackFor = IOException.class)
    public void addGameDiaryImageWithoutDiaryEntryAssociation(final UUID imageId, final MultipartFile file)
            throws IOException {

        // Authorize
        activeUserService.requireActivePerson();

        gameDiaryImageService.addGameDiaryImageWithoutDiaryEntryAssociation(imageId, file);
    }

    @Transactional(rollbackFor = IOException.class)
    public void replaceImageForDiaryEntry(
            final long diaryEntryId,
            final GameDiaryEntryType diaryEntryType,
            final UUID replacedUuid,
            final UUID uuid,
            final MultipartFile file)
            throws IOException {

        final GameDiaryEntry diaryEntry = diaryEntryType.supply(
                () -> requireEntityService.requireHarvest(diaryEntryId, EntityPermission.UPDATE),
                () -> requireEntityService.requireObservation(diaryEntryId, EntityPermission.UPDATE));

        // Remove image which will be replaced.

        final GameDiaryImage toBeReplaced =
                gameDiaryImageService.getGameDiaryImageForDiaryEntry(replacedUuid, diaryEntry);

        gameDiaryImageService.deleteGameDiaryImage(toBeReplaced);

        // Add new image
        gameDiaryImageService.addGameDiaryImage(diaryEntry, uuid, file);
    }

    @Transactional(readOnly = true)
    public List<HarvestDTO> listHarvestsAcceptedToPermit(final Long permitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);

        return harvestDtoTransformer.apply(harvestRepository.findAll(acceptedToPermit(permit)));
    }

    @Transactional(readOnly = true)
    public PersonRelationshipToGameDiaryEntryDTO getRelationshipToGameDiaryEntry(final GameDiaryEntryType entryType,
                                                                                 final long entryId) {
        Objects.requireNonNull(entryType);

        final SystemUser user = activeUserService.getActiveUser();
        final Person loggedInPerson = user.getPerson();

        if (loggedInPerson == null) {
            return new PersonRelationshipToGameDiaryEntryDTO();
        }

        final GameDiaryEntry diaryEntry = entryType.supply(
                () -> harvestRepository.getOne(entryId),
                () -> observationRepository.getOne(entryId));

        final boolean isAuthor = diaryEntry.isAuthor(loggedInPerson);
        final boolean isActor = diaryEntry.isActor(loggedInPerson);

        return new PersonRelationshipToGameDiaryEntryDTO(isAuthor, isActor);
    }

    private ObservationFieldPresenceValidator getObservationFieldsValidator(final GameSpecies species) {
        return getObservationFieldsValidator(species, OBSERVATION_FIELDS_METADATA_VERSION);
    }

}
