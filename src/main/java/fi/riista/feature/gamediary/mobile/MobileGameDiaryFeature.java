package fi.riista.feature.gamediary.mobile;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.AbstractGameDiaryFeature;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimen;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsSpecs;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermit_;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.jpa.JpaSpecs;
import javaslang.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.observation.ObservationSpecVersion.MOST_RECENT;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_KEKO;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_PENKKA;
import static fi.riista.feature.gamediary.observation.ObservationType.PESA_SEKA;
import static fi.riista.feature.gamediary.GameDiarySpecs.harvestsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observationsByHuntingYear;
import static fi.riista.feature.gamediary.GameDiarySpecs.observer;
import static fi.riista.feature.gamediary.GameDiarySpecs.shooter;
import static fi.riista.feature.gamediary.GameDiarySpecs.temporalSort;
import static fi.riista.feature.harvestpermit.HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT;
import static fi.riista.feature.harvestpermit.HarvestPermitSpecs.harvestReportNotDone;
import static fi.riista.feature.harvestpermit.HarvestPermitSpecs.isPermitContactPerson;
import static fi.riista.feature.harvestpermit.HarvestPermitSpecs.withHarvestAuthor;
import static fi.riista.feature.harvestpermit.HarvestPermitSpecs.withHarvestShooter;
import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.fetch;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.jpa.domain.Specifications.where;

public abstract class MobileGameDiaryFeature extends AbstractGameDiaryFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryFeature.class);

    // Contains mappings from observation-specification-versions to observation-field-metadata-versions.
    private static final Map<ObservationSpecVersion, Integer> OBSERVATION_SPEC_VERSION_TO_METADATA_VERSION =
            ImmutableMap.of(ObservationSpecVersion._1, 1, ObservationSpecVersion._2, 2);

    protected static int getObservationMetadataVersion(final ObservationSpecVersion observationSpecVersion) {
        return OBSERVATION_SPEC_VERSION_TO_METADATA_VERSION.get(observationSpecVersion);
    }

    @Resource
    protected MobileHarvestDTOTransformer harvestDtoTransformer;

    @Resource
    protected MobileObservationDTOTransformer observationDtoTransformer;

    @Resource
    protected MobileOccupationDTOFactory mobileOccupationDTOFactory;

    @Resource
    protected OccupationRepository occupationRepository;

    public abstract EnumSet<HarvestSpecVersion> getSupportedSpecVersions();

    protected abstract MobileAccountDTO getMobileAccount();

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getMobileObservationFieldMetadata(
            @Nonnull final ObservationSpecVersion observationSpecVersion) {

        Objects.requireNonNull(observationSpecVersion);

        final ObservationMetadataDTO dto =
                getObservationFieldMetadata(getObservationMetadataVersion(observationSpecVersion));
        dto.setMobileApiObservationSpecVersion(observationSpecVersion);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<MobileHarvestDTO> getHarvests(
            final int firstCalendarYearOfHuntingYear, @Nonnull final HarvestSpecVersion harvestSpecVersion) {

        Objects.requireNonNull(harvestSpecVersion, "harvestSpecVersion must not be null");

        final Person person = activeUserService.requireActivePerson();

        // Harvest-specific authorization built into query
        final List<Harvest> harvests = harvestRepository.findAll(
                where(shooter(person))
                        .and(harvestsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return harvestDtoTransformer.apply(harvests, harvestSpecVersion);
    }

    @Transactional(readOnly = true)
    public MobileHarvestDTO getExistingByMobileClientRefId(final MobileHarvestDTO dto) {
        assertHarvestDTOIsValid(dto);

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
    public MobileHarvestDTO createHarvest(final MobileHarvestDTO dto) {
        assertHarvestDTOIsValid(dto);

        // Duplicate prevention check
        final MobileHarvestDTO existing = getExistingByMobileClientRefId(dto);
        if (existing != null) {
            return existing;
        }

        // Not duplicate, create new one
        final Person authenticatedPerson = activeUserService.requireActivePerson();

        final Harvest harvest = new Harvest();
        harvest.setAuthor(authenticatedPerson);
        harvest.setActualShooter(authenticatedPerson);
        harvest.setFromMobile(true);
        harvest.setMobileClientRefId(dto.getMobileClientRefId());

        // Use 1 as default specimen amount
        harvest.setAmount(Optional.ofNullable(dto.getAmount()).orElse(1));

        // Default to GPS if GeoLocation.Source not explicitly given.
        if (dto.getGeoLocation().getSource() == null) {
            dto.getGeoLocation().setSource(GeoLocation.Source.GPS_DEVICE);
        }

        updateMutableFields(harvest, dto, authenticatedPerson, true);

        harvestRepository.saveAndFlush(harvest);

        if (dto.getSpecimens() != null) {
            harvestSpecimenService.addSpecimens(
                    harvest, dto.getAmount(), dto.getSpecimens(), dto.getHarvestSpecVersion());
        }

        return harvestDtoTransformer.apply(harvest, dto.getHarvestSpecVersion());
    }

    @Transactional
    public MobileHarvestDTO updateHarvest(final MobileHarvestDTO dto) {
        assertHarvestDTOIsValid(dto);

        final HarvestSpecVersion specVersion = dto.getHarvestSpecVersion();
        final Person currentPerson = activeUserService.requireActivePerson();

        final Harvest harvest = requireEntityService.requireHarvest(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(harvest, dto);

        final boolean businessFieldsCanBeUpdated = canBusinessFieldsBeUpdated(currentPerson, harvest, false);
        updateMutableFields(harvest, dto, currentPerson, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            if (dto.getSpecimens() != null) {
                final Tuple2<?, Boolean> specimenResult =
                        harvestSpecimenService.setSpecimens(harvest, dto.getAmount(), dto.getSpecimens(), specVersion);

                if (specimenResult._2) {
                    harvest.forceRevisionUpdate();
                }
            } else if (dto.getAmount() != null) {
                harvestSpecimenService.limitSpecimens(harvest, dto.getAmount());
            }
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return harvestDtoTransformer.apply(harvestRepository.saveAndFlush(harvest), specVersion);
    }

    private void updateMutableFields(
            final Harvest harvest,
            final MobileHarvestDTO dto,
            final Person currentPerson,
            final boolean businessFieldsCanBeUpdated) {

        if (businessFieldsCanBeUpdated) {
            // Try to do queries first (as far as possible) in order to prevent
            // harvest revision bumping many integer steps.

            final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());
            final Date pointOfTime = DateUtil.toDateNullSafe(dto.getPointOfTime());

            // Null-checking of geolocation source done for backwards-compatibility.
            final boolean geoLocationSourceMissingFromDto = dto.getGeoLocation().getSource() == null;
            GeoLocation location = null;

            if (!dto.getHarvestSpecVersion().requiresGeolocationSource()) {
                location = harvest.getGeoLocation();

                // Keep original location within updates if source is missing from DTO.
                if (!geoLocationSourceMissingFromDto) {
                    location = dto.getGeoLocation();
                }
            } else {
                location = dto.getGeoLocation();
            }

            final BiConsumer<Harvest, HarvestSpecVersion> permitStateAndReportRequirementSetter =
                    prepareMutatorForPermitStateAndReportRequirement(
                            dto, species, location, currentPerson, false);

            if (!geoLocationSourceMissingFromDto) {
                harvest.updateGeoLocation(location, gisQueryService);
            }

            harvest.setSpecies(species);
            harvest.setPointOfTime(pointOfTime);
            Optional.ofNullable(dto.getAmount()).ifPresent(harvest::setAmount);

            permitStateAndReportRequirementSetter.accept(harvest, dto.getHarvestSpecVersion());
        }

        harvest.setDescription(dto.getDescription());
    }

    @Transactional(readOnly = true)
    public List<MobileObservationDTO> getObservations(
            final int firstCalendarYearOfHuntingYear, @Nonnull final ObservationSpecVersion observationSpecVersion) {

        Objects.requireNonNull(observationSpecVersion, "observationSpecVersion must not be null");

        final Person person = activeUserService.requireActivePerson();

        // Observation-specific authorization built into query
        final List<Observation> observations = observationRepository.findAll(
                where(observer(person))
                        .and(observationsByHuntingYear(firstCalendarYearOfHuntingYear)),
                temporalSort(Direction.ASC));

        return observationDtoTransformer.apply(observations, observationSpecVersion);
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

        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());

        getObservationFieldsValidator(species, getObservationMetadataVersion(specVersion))
                .validate(dto, Optional.ofNullable(dto.getSpecimens()));

        // Not duplicate, create new one

        final Observation observation = new Observation();
        observation.setFromMobile(true);
        observation.setMobileClientRefId(dto.getMobileClientRefId());
        observation.setAuthor(authenticatedPerson);
        observation.setObserver(authenticatedPerson);

        updateMutableFields(observation, species, dto, true);

        observationRepository.saveAndFlush(observation);

        final List<ObservationSpecimen> specimens = F.isNullOrEmpty(dto.getSpecimens())
                ? null
                : observationSpecimenService.addSpecimens(
                        observation, dto.getAmount(), dto.getSpecimens(), specVersion);

        // Validate entity graph after specimens are persisted. Amount field is excluded because in
        // moose/mooselike-within-moose-hunting cases the field is computed automatically even
        // though in the REST-API it is actually prohibited (because of separate moose amount
        // fields are used).
        getObservationFieldsValidator(species, getObservationMetadataVersion(MOST_RECENT))
                .validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());

        return observationDtoTransformer.apply(observation, dto.getObservationSpecVersion());
    }

    @Transactional
    public MobileObservationDTO updateObservation(@Nonnull final MobileObservationDTO dto) {
        Objects.requireNonNull(dto);

        final ObservationSpecVersion specVersion = dto.getObservationSpecVersion();
        activeUserService.requireActivePerson();

        final Observation observation =
                requireEntityService.requireObservation(dto.getId(), EntityPermission.UPDATE);
        DtoUtil.assertNoVersionConflict(observation, dto);

        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(dto.getGameSpeciesCode());

        getObservationFieldsValidator(species, getObservationMetadataVersion(specVersion))
                .validate(dto, Optional.ofNullable(dto.getSpecimens()));

        final boolean businessFieldsCanBeUpdated = canBusinessFieldsBeUpdated(observation);
        updateMutableFields(observation, species, dto, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            final List<ObservationSpecimen> specimens;

            if (dto.getAmount() != null) {
                final Tuple2<List<ObservationSpecimen>, Boolean> specimenResult =
                        observationSpecimenService.setSpecimens(
                                observation, dto.getAmount(), dto.getSpecimens(), specVersion);
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
            getObservationFieldsValidator(species, getObservationMetadataVersion(MOST_RECENT))
                    .validate(observation, Optional.ofNullable(specimens), singleton("amount"), emptySet());
        }

        // flush is mandatory! because mobile will use the returned revision, and revision is updated on save
        return observationDtoTransformer.apply(
                observationRepository.saveAndFlush(observation), dto.getObservationSpecVersion());
    }

    private void updateMutableFields(final Observation observation,
                                     final GameSpecies species,
                                     final MobileObservationDTO dto,
                                     final boolean businessFieldsCanBeUpdated) {

        final ObservationType existingType = observation.getObservationType();

        super.updateMutableFields(observation, species, dto, businessFieldsCanBeUpdated);

        if (businessFieldsCanBeUpdated) {
            if (dto.requiresBeaverObservationTypeTranslation() && dto.getObservationType() == PESA) {
                observation.setObservationType(
                        existingType == PESA_PENKKA || existingType == PESA_SEKA ? existingType : PESA_KEKO);
            }
        }

        observation.setDescription(dto.getDescription());
    }

    @Transactional(rollbackFor = IOException.class)
    public void deleteGameDiaryImage(final UUID imageUuid) {
        try {
            gameDiaryImageService.deleteGameDiaryImage(imageUuid, activeUserService.requireActivePerson());
        } catch (final NotFoundException nfe) {
            LOG.info("deleteGameDiaryImage failed, image not found uuid:" + imageUuid);
            // If image is not found there is nothing that mobile client can do so let's not report this
        }
    }

    protected SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingHarvests(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(harvestRepository.findByActualShooter(person));
    }

    protected SortedSet<Integer> getBeginningCalendarYearsOfHuntingYearsContainingObservations(final Person person) {
        return getBeginningCalendarYearsOfHuntingYears(observationRepository.findByObserver(person));
    }

    private static <T extends GameDiaryEntry> SortedSet<Integer> getBeginningCalendarYearsOfHuntingYears(
            final Iterable<T> diaryEntries) {

        return F.stream(diaryEntries)
                .map(GameDiaryEntry::getPointOfTime)
                .map(DateUtil::toLocalDateNullSafe)
                .filter(Objects::nonNull)
                .map(DateUtil::getFirstCalendarYearOfHuntingYearContaining)
                .collect(toCollection(TreeSet::new));
    }

    @Transactional(readOnly = true)
    public MobileHarvestPermitExistsDTO findPermitNumber(final String permitNumber) {
        return Optional.ofNullable(harvestPermitRepository.findByPermitNumber(permitNumber))
                .map(permit -> MobileHarvestPermitExistsDTO.create(permit, gameSpeciesIdToFields(singleton(permit))))
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<MobileHarvestPermitExistsDTO> preloadPermits() {
        final List<HarvestPermit> permits = preloadPermitEntities();
        return MobileHarvestPermitExistsDTO.create(permits, gameSpeciesIdToFields(permits));
    }

    private List<HarvestPermit> preloadPermitEntities() {
        final Person person = activeUserService.getActiveUser().getPerson();

        if (person == null) {
            return emptyList();
        }

        final Specifications<HarvestPermit> harvestsAsListAndReportNotDone = Specifications
                .where(equal(HarvestPermit_.harvestsAsList, Boolean.TRUE))
                .and(harvestReportNotDone());

        final Specification<HarvestPermit> harvestsNotAsList = equal(HarvestPermit_.harvestsAsList, Boolean.FALSE);

        final Specification<HarvestPermit> contactPersonAndPermitUsable = JpaSpecs.and(
                isPermitContactPerson(person),
                JpaSpecs.or(harvestsAsListAndReportNotDone, harvestsNotAsList));

        return harvestPermitRepository.findAll(JpaSpecs.and(
                JpaSpecs.or(
                        contactPersonAndPermitUsable,
                        withHarvestAuthor(person),
                        withHarvestShooter(person)),
                IS_NOT_ANY_MOOSELIKE_PERMIT));
    }

    private Map<Long, HarvestReportFields> gameSpeciesIdToFields(final Collection<HarvestPermit> permits) {

        final Set<Long> gameSpeciesIds = permits.stream()
                .map(HarvestPermit::getSpeciesAmounts)
                .flatMap(List::stream)
                .map(HarvestPermitSpeciesAmount::getGameSpecies)
                .map(GameSpecies::getId)
                .collect(toSet());

        return harvestReportFieldsRepository.findAll(
                where(HarvestReportFieldsSpecs.withUsedWithPermit(true))
                        .and(HarvestReportFieldsSpecs.withGameSpeciesCodes(gameSpeciesIds))
                        .and(fetch(HarvestReportFields_.species)))
                .stream()
                .collect(toMap(Functions.idOf(HarvestReportFields::getSpecies), identity()));
    }

    protected void assertHarvestDTOIsValid(final MobileHarvestDTO dto) {
        if (dto.getHarvestSpecVersion() == null) {
            throw new MessageExposableValidationException("harvestSpecVersion must not be null");
        }

        if (dto.getHarvestSpecVersion().requiresGeolocationSource() && dto.getGeoLocation().getSource() == null) {
            throw new MessageExposableValidationException("geoLocation.source must not be null");
        }

        if (dto.getHarvestSpecVersion().requiresAmount() && dto.getAmount() == null) {
            throw new MessageExposableValidationException("amount must not be null");
        }
    }

}
