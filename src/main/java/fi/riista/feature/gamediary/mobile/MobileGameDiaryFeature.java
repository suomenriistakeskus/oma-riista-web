package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestService;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.ObservationService;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static fi.riista.feature.gamediary.GameDiarySpecs.harvestsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observationsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class MobileGameDiaryFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryFeature.class);

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private ObservationRepository observationRepository;

    @Resource
    private HarvestService harvestService;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private ObservationService observationService;

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestSpecimenService harvestSpecimenService;

    @Resource
    private ObservationSpecimenService observationSpecimenService;

    @Resource
    private ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private MobileHarvestDTOTransformer harvestDtoTransformer;

    @Resource
    private MobileObservationDTOTransformer observationDtoTransformer;

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getMobileObservationFieldMetadata(@Nonnull final ObservationSpecVersion specVersion) {
        final ObservationMetadataDTO dto = observationFieldsMetadataService.getObservationFieldsMetadata(specVersion);
        dto.setMobileApiObservationSpecVersion(specVersion);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MobileHarvestDTO> getHarvests(final int firstCalendarYearOfHuntingYear,
                                              @Nonnull final HarvestSpecVersion specVersion) {

        Objects.requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();

        // Harvest-specific authorization built into query
        final List<Harvest> harvests = harvestRepository.findAll(
                where(shooter(person))
                        .and(harvestsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return harvestDtoTransformer.apply(harvests, specVersion);
    }

    @Transactional(readOnly = true)
    public MobileHarvestDTO getExistingByMobileClientRefId(final MobileHarvestDTO dto, final int apiVersion) {
        assertHarvestDTOIsValid(dto, apiVersion);

        final Person authenticatedPerson = activeUserService.requireActivePerson();

        if (dto.getMobileClientRefId() != null) {
            final Harvest harvest =
                    harvestRepository.findByAuthorAndMobileClientRefId(authenticatedPerson, dto.getMobileClientRefId());

            if (harvest != null) {
                return harvestDtoTransformer.apply(harvest, dto.getHarvestSpecVersion());
            }
        }

        return null;
    }

    @Transactional
    public MobileHarvestDTO createHarvest(final MobileHarvestDTO dto, final int apiVersion) {
        assertHarvestDTOIsValid(dto, apiVersion);

        // Duplicate prevention check
        final MobileHarvestDTO existing = getExistingByMobileClientRefId(dto, apiVersion);

        if (existing != null) {
            return existing;
        }

        // Not duplicate, create new one
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = Objects.requireNonNull(activeUser.getPerson());

        final Harvest harvest = new Harvest();
        harvest.setAuthor(currentPerson);
        harvest.setActualShooter(currentPerson);
        harvest.setFromMobile(true);
        harvest.setMobileClientRefId(dto.getMobileClientRefId());

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, true);

        harvestRepository.saveAndFlush(harvest);

        if (dto.getSpecimens() != null) {
            harvestSpecimenService.addSpecimens(
                    harvest, dto.getAmount(), dto.getSpecimens(), dto.getHarvestSpecVersion());
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        return harvestDtoTransformer.apply(harvest, dto.getHarvestSpecVersion());
    }

    @Transactional
    public MobileHarvestDTO updateHarvest(final MobileHarvestDTO dto, final int apiVersion) {
        assertHarvestDTOIsValid(dto, apiVersion);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Person currentPerson = Objects.requireNonNull(activeUser.getPerson());

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final boolean businessFieldsCanBeUpdated =
                harvestService.canBusinessFieldsBeUpdated(currentPerson, harvest, specVersion);

        final HarvestChangeHistory historyEvent =
                harvestService.updateMutableFields(harvest, dto, activeUser, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            if (dto.getSpecimens() != null) {
                final boolean anyChangesDetected = harvestSpecimenService
                        .setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), specVersion)
                        .apply((specimens, changesDetected) -> changesDetected);

                if (anyChangesDetected) {
                    harvest.forceRevisionUpdate();
                }
            } else if (dto.getAmount() != null) {
                harvestSpecimenService.limitSpecimens(harvest, dto.getAmount());
            }
        }

        if (historyEvent != null) {
            harvestChangeHistoryRepository.save(historyEvent);
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return harvestDtoTransformer.apply(harvestRepository.saveAndFlush(harvest), specVersion);
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);
        harvestSpecimenService.deleteAllSpecimens(harvest);
        gameDiaryImageService.deleteGameDiaryImages(harvest);
        harvestService.deleteHarvest(harvest, activeUser);
    }

    @Transactional(readOnly = true)
    public List<MobileObservationDTO> getObservations(final int firstCalendarYearOfHuntingYear,
                                                      @Nonnull final ObservationSpecVersion specVersion) {

        Objects.requireNonNull(specVersion);

        final Person person = activeUserService.requireActivePerson();

        // Observation-specific authorization built into query
        final List<Observation> observations = observationRepository.findAll(
                where(observer(person))
                        .and(observationsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return observationDtoTransformer.apply(observations, specVersion);
    }

    @Transactional
    public MobileObservationDTO createObservation(@Nonnull final MobileObservationDTO dto) {
        Objects.requireNonNull(dto);

        final Person authenticatedPerson = activeUserService.requireActivePerson();
        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();

        // Duplicate prevention check
        if (dto.getMobileClientRefId() != null) {
            final MobileObservationDTO existing = observationRepository
                    .findByAuthorAndMobileClientRefId(authenticatedPerson, dto.getMobileClientRefId())
                    .map(observation -> observationDtoTransformer.apply(observation, specVersion))
                    .orElse(null);

            if (existing != null) {
                return existing;
            }
        } else {
            throw new MessageExposableValidationException("mobileClientRefId is missing");
        }

        final boolean carnivoreAuthority =
                userAuthorizationHelper.isCarnivoreContactPersonAnywhere(dto.getPointOfTime().toLocalDate());

        ObservationFieldValidator validator = observationFieldsMetadataService.getObservationFieldValidator(dto.getObservationContext(), carnivoreAuthority);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        // Not duplicate, create a new one

        final Observation observation = new Observation();
        observation.setFromMobile(true);
        observation.setMobileClientRefId(dto.getMobileClientRefId());
        observation.setAuthor(authenticatedPerson);
        observation.setObserver(authenticatedPerson);
        observation.setDescription(dto.getDescription());

        updateMutableFields(observation, dto, carnivoreAuthority);

        if (!specVersion.isMostRecent()) {
            validator = observationFieldsMetadataService.getObservationFieldValidator(observation.getObservationContext(), carnivoreAuthority);

            fixMooseCalfAmountIfNeeded(observation, specVersion, validator);
        }

        observationRepository.saveAndFlush(observation);

        final List<ObservationSpecimen> specimens = F.isNullOrEmpty(dto.getSpecimens())
                ? null
                : observationSpecimenService.addSpecimens(
                observation, dto.getAmount(), dto.getSpecimens(), specVersion);

        // Validate entity graph after specimens are persisted. Amount field is excluded
        // because in within-moose-hunting cases the field is computed automatically based on
        // the separate moose amount fields. Therefore the field is prohibited in the REST API.
        validator.validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());

        return observationDtoTransformer.apply(observation, specVersion);
    }

    @Transactional
    public MobileObservationDTO updateObservation(@Nonnull final MobileObservationDTO dto) {
        Objects.requireNonNull(dto);

        activeUserService.requireActivePerson();

        final Observation observation = requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(observation, dto);

        final boolean carnivoreAuthority =
                userAuthorizationHelper.isCarnivoreContactPersonAnywhere(dto.getPointOfTime().toLocalDate());

        ObservationFieldValidator validator = observationFieldsMetadataService.getObservationFieldValidator(dto.getObservationContext(), carnivoreAuthority);
        validator.validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();
        final boolean observationLockedByClubHunting = clubHuntingStatusService.isDiaryEntryLocked(observation);

        if (!observationLockedByClubHunting) {

            // Cannot mutate most of the state if user's carnivore authority has expired.
            if (carnivoreAuthority || !observation.isAnyLargeCarnivoreFieldPresent()) {

                updateMutableFields(observation, dto, carnivoreAuthority);

                final List<ObservationSpecimen> specimens;

                if (dto.getAmount() != null) {
                    specimens = observationSpecimenService
                            .setSpecimens(observation, dto.getAmount(), dto.getSpecimens(), specVersion)
                            .apply((specimenEntities, anyChangesDetected) -> {
                                if (anyChangesDetected) {
                                    observation.forceRevisionUpdate();
                                }

                                return specimenEntities;
                            });
                } else {
                    specimens = null;
                    observationSpecimenService.deleteAllSpecimens(observation);
                }

                if (!specVersion.isMostRecent()) {
                    validator = observationFieldsMetadataService.getObservationFieldValidator(observation.getObservationContext(), carnivoreAuthority);

                    fixMooseCalfAmountIfNeeded(observation, specVersion, validator);
                }

                final Optional<List<?>> specimensOpt = Optional.ofNullable(specimens);

                // While supporting old spec-versions it is easier to nullify illegal fields in an
                // "after hook" manner than try to bake the logic directly into
                // game-diary-feature/specimen-service ().
                validator.nullifyIllegalFields(observation, specimensOpt, singleton("amount"), emptySet());

                // Validate entity graph after specimens are persisted. Amount field is excluded
                // because in within-moose-hunting cases the field is computed automatically based on
                // the separate moose amount fields. Therefore the field is prohibited in the REST API.
                validator.validate(observation, specimensOpt, singleton("amount"), emptySet());
            }
        }

        // Description updated separately b/c it is independent of club-hunting status.
        observation.setDescription(dto.getDescription());

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return observationDtoTransformer.apply(observationRepository.saveAndFlush(observation), specVersion);
    }

    private void updateMutableFields(final Observation observation,
                                     final MobileObservationDTO dto,
                                     final boolean carnivoreAuthorityGranted) {

        final ObservationType existingType = observation.getObservationType();

        observationService.updateMutableFields(observation, dto, carnivoreAuthorityGranted);

        if (dto.requiresBeaverObservationTypeTranslation() && dto.getObservationType() == PESA) {
            observation.setObservationType(
                    existingType == PESA_PENKKA || existingType == PESA_SEKA ? existingType : PESA_KEKO);
        }
    }

    private static void fixMooseCalfAmountIfNeeded(final Observation observation,
                                                   final ObservationSpecVersion dtoSpecVersion,
                                                   final ObservationFieldValidator validator) {

        if (!dtoSpecVersion.supportsMooselikeCalfAmount()
                && validator.getContextSensitiveFields().getMooselikeCalfAmount().nonNullValueRequired()
                && observation.getMooselikeCalfAmount() == null) {

            observation.setMooselikeCalfAmount(0);
        }
    }

    @Transactional
    public void deleteObservation(final long observationId) {
        final Observation observation = requireEntityService.requireObservation(observationId, EntityPermission.DELETE);
        observationSpecimenService.deleteAllSpecimens(observation);
        gameDiaryImageService.deleteGameDiaryImages(observation);
        observationService.deleteObservation(observation);
    }

    @Transactional
    public void deleteGameDiaryImage(final UUID imageUuid) {
        try {
            gameDiaryImageService.deleteGameDiaryImage(imageUuid, activeUserService.requireActivePerson());
        } catch (final NotFoundException nfe) {
            LOG.info("deleteGameDiaryImage failed, image not found uuid:" + imageUuid);
            // If image is not found there is nothing that mobile client can do so let's not report this
        }
    }

    protected void assertHarvestDTOIsValid(final MobileHarvestDTO dto, final int apiVersion) {
        if (dto.getHarvestSpecVersion() == null) {
            throw new MessageExposableValidationException("harvestSpecVersion is null");
        }

        if (dto.getHarvestSpecVersion().requiresGeolocationSource() && dto.getGeoLocation().getSource() == null) {
            throw new MessageExposableValidationException("geoLocation.source is null");
        }

        if (dto.getHarvestSpecVersion().requiresAmount() && dto.getAmount() == null) {
            throw new MessageExposableValidationException("amount is null");
        }

        if (apiVersion >= 2) {
            if (dto.getId() == null && dto.getMobileClientRefId() == null) {
                throw new MessageExposableValidationException("mobileClientRefId must not be null");
            }

            // Specimens are allowed to be null on creation.
            if (F.hasId(dto) && dto.getSpecimens() == null) {
                throw new MessageExposableValidationException("specimens must not be null");
            }
        }
    }
}
