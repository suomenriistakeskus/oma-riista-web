package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.common.dto.CodesetEntryDTO;
import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.HarvestDTOBase;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageRepository;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationDTOBase;
import fi.riista.feature.gamediary.observation.ObservationRepository;
import fi.riista.feature.gamediary.observation.metadata.GameSpeciesObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldPresenceValidator;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldsMetadataService;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadataDTO;
import fi.riista.feature.gamediary.observation.specimen.ObservationSpecimenService;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.RhyNotResolvableByGeoLocationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountNotFound;
import fi.riista.feature.harvestpermit.report.HarvestReportRequirementsService;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsSpecs;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Filters;
import fi.riista.util.jpa.JpaSpecs;
import javaslang.control.Option;
import org.joda.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public abstract class AbstractGameDiaryFeature {

    // protected for testing
    protected static void assertSpecimensWithFields(
            final List<HarvestSpecimenDTO> dtoList, final HarvestReportFields fields) {

        final boolean agesValid = allValuesValid(dtoList, fields.getAge(), HarvestSpecimenDTO::getAge);
        final boolean gendersValid = allValuesValid(dtoList, fields.getGender(), GameDiaryEntrySpecimenDTO::getGender);
        final boolean weightsValid = allValuesValid(dtoList, fields.getWeight(), HarvestSpecimenDTO::getWeight);

        HarvestPermitSpecimensIncompleteException.assertTrue(agesValid, "Age is required");
        HarvestPermitSpecimensIncompleteException.assertTrue(gendersValid, "Gender is required");
        HarvestPermitSpecimensIncompleteException.assertTrue(weightsValid, "Weight is required");
    }

    private static <T> boolean allValuesValid(
            final List<HarvestSpecimenDTO> dtoList,
            final Required requirement,
            final Function<HarvestSpecimenDTO, T> value) {

        return Optional.ofNullable(dtoList)
                .map(specimens -> specimens.stream().allMatch(
                        specimen -> specimen != null && requirement.isValidValue(value.apply(specimen))))
                .orElse(true);
    }

    private static void assertPermitValid(final HarvestPermit permit, final int gameSpeciesCode, final LocalDate date) {
        permit.findSpeciesAmount(gameSpeciesCode, date).orElseThrow(() -> {
            final String msg = String.format(
                    "For given permit, species is not valid for given date. permitId:%d gameSpeciesCode:%d date:%s",
                    permit.getId(), gameSpeciesCode, date);

            return new HarvestPermitSpeciesAmountNotFound(msg);
        });
    }

    private static StateAcceptedToHarvestPermit getStateAcceptedToPermit(
            final HarvestPermit permit, final Person currentPerson, final boolean isModerator) {

        if (!permit.isHarvestsAsList()) {
            return null;
        }

        if (!permit.getUndeletedHarvestReports().isEmpty()) {
            return StateAcceptedToHarvestPermit.REJECTED;
        }

        return isModerator || permit.hasContactPerson(currentPerson)
                ? StateAcceptedToHarvestPermit.ACCEPTED
                : StateAcceptedToHarvestPermit.PROPOSED;
    }

    @Resource
    protected HarvestRepository harvestRepository;

    @Resource
    protected ObservationRepository observationRepository;

    @Resource
    protected GameDiaryImageRepository gameDiaryImageRepository;

    @Resource
    protected HarvestPermitRepository harvestPermitRepository;

    @Resource
    protected HarvestReportFieldsRepository harvestReportFieldsRepository;

    @Resource
    protected GameDiaryService gameDiaryService;

    @Resource
    protected GameDiaryImageService gameDiaryImageService;

    @Resource
    protected GISQueryService gisQueryService;

    @Resource
    protected ActiveUserService activeUserService;

    @Resource
    protected RequireEntityService requireEntityService;

    @Resource
    protected HarvestSpecimenService harvestSpecimenService;

    @Resource
    protected ObservationSpecimenService observationSpecimenService;

    @Resource
    private HarvestReportRequirementsService harvestReportRequirementsService;

    @Resource
    protected ObservationFieldsMetadataService observationFieldsMetadataService;

    @Resource
    protected ClubHuntingStatusService clubHuntingStatusService;

    public List<CodesetEntryDTO> getGameCategories() {
        // No authorization by intention
        return gameDiaryService.getGameCategories();
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> getGameSpecies() {
        // No authorization by intention
        return gameDiaryService.getGameSpecies();
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> getGameSpeciesRegistrableAsObservationsWithinMooseHunting() {
        return gameDiaryService.getGameSpeciesRegistrableAsObservationsWithinMooseHunting();
    }

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getObservationFieldMetadata(final int metadataVersion) {
        return observationFieldsMetadataService.getObservationFieldsMetadata(metadataVersion);
    }

    @Transactional(readOnly = true)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSpecies(
            final int officialCode, final int metadataVersion) {

        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(officialCode);
        return observationFieldsMetadataService.getObservationFieldMetadataForSingleSpecies(
                species, metadataVersion, true);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<?> getGameDiaryImageBytes(final UUID imageUuid, final boolean disposition)
            throws IOException {

        // No authorization by intention

        return gameDiaryImageService.getGameDiaryImageBytes(imageUuid, disposition);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<?> getGameDiaryImageBytesResized(
            final UUID imageUuid, final int width, final int height, final boolean keepProportions)
                    throws IOException {

        // No authorization by intention

        return gameDiaryImageService.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @Transactional(rollbackFor = IOException.class)
    public void addGameDiaryImageForDiaryEntry(
            final long diaryEntryId,
            final GameDiaryEntryType diaryEntryType,
            final UUID imageId,
            final MultipartFile file)
                    throws IOException {

        // Authorize person
        activeUserService.requireActivePerson();

        final GameDiaryEntry diaryEntry = diaryEntryType.supply(
                () -> requireEntityService.requireHarvest(diaryEntryId, EntityPermission.UPDATE),
                () -> requireEntityService.requireObservation(diaryEntryId, EntityPermission.UPDATE));

        gameDiaryImageService.addGameDiaryImage(diaryEntry, imageId, file);
    }

    protected void updateImages(final GameDiaryEntry diaryEntry, final List<UUID> imageIds) {
        final List<GameDiaryImage> existingImages = gameDiaryImageService.getImages(diaryEntry);
        final Set<UUID> existingImageIds =
                F.getUniqueIdsAfterTransform(existingImages, GameDiaryImage::getFileMetadata);

        // Remove images and associations with GameDiaryEntry for images not found in list of UUIDs.
        final List<GameDiaryImage> imagesToBeRemoved = existingImages.stream()
                .filter(img -> !imageIds.contains(img.getFileMetadata().getId()))
                .collect(toList());
        gameDiaryImageService.deleteGameDiaryImages(imagesToBeRemoved);

        // Add images which are new (GameDiaryEntry isn't already associated with them).
        imageIds.stream()
                .filter(Filters.notIn(existingImageIds))
                .forEach(uuid -> gameDiaryImageService.associateGameDiaryEntryWithImage(diaryEntry, uuid));
    }

    @Transactional
    public void deleteHarvest(final long harvestId) {
        final Harvest harvest = requireEntityService.requireHarvest(harvestId, EntityPermission.DELETE);

        assertHarvestReportNotDone(harvest);
        assertNotAcceptedToPermit(harvest);
        assertNotAttachedToHuntingDay(harvest);

        harvestSpecimenService.deleteAllSpecimens(harvest);

        gameDiaryImageService.deleteGameDiaryImages(gameDiaryImageRepository.findByHarvest(harvest));

        harvestRepository.delete(harvest);
    }

    @Transactional
    public void deleteObservation(final long observationId) {
        final Observation observation = requireEntityService.requireObservation(observationId, EntityPermission.DELETE);

        assertNotAttachedToHuntingDay(observation);

        observationSpecimenService.deleteAllSpecimens(observation);

        gameDiaryImageService.deleteGameDiaryImages(gameDiaryImageRepository.findByObservation(observation));

        observationRepository.delete(observation);
    }

    private void assertNotAcceptedToPermit(final Harvest harvest) {
        if (harvest.getHarvestPermit() != null
                && harvest.getStateAcceptedToHarvestPermit() == Harvest.StateAcceptedToHarvestPermit.ACCEPTED) {

            final Person person = activeUserService.getActiveUser().getPerson();

            if (person == null || !harvest.getHarvestPermit().hasContactPerson(person)) {
                throw new RuntimeException("Cannot delete harvest which is accepted to permit.");
            }
        }
    }

    private static void assertHarvestReportNotDone(final Harvest harvest) {
        if (harvest.isHarvestReportDone()) {
            throw new RuntimeException("Cannot delete harvest with an associated harvest report.");
        }
    }

    private static void assertNotAttachedToHuntingDay(final GameDiaryEntry entry) {
        if (entry.getHuntingDayOfGroup() != null) {
            throw new RuntimeException("Cannot delete entry with an associated hunting day.");
        }
    }

    protected boolean canBusinessFieldsBeUpdated(final Person person, final Harvest harvest, final boolean moderator) {
        if (harvest.isHarvestReportDone()) {
            return false;
        }

        if (clubHuntingStatusService.isDiaryEntryLocked(harvest)) {
            return false;
        }

        final HarvestPermit permit = harvest.getHarvestPermit();
        return permit == null
                || moderator
                || permit.hasContactPerson(person)
                || harvest.getStateAcceptedToHarvestPermit() != Harvest.StateAcceptedToHarvestPermit.ACCEPTED;
    }

    protected boolean canBusinessFieldsBeUpdated(final Observation observation) {
        return !clubHuntingStatusService.isDiaryEntryLocked(observation);
    }

    protected void updateMutableFields(
            final Observation observation,
            final GameSpecies species,
            final ObservationDTOBase dto,
            final boolean businessFieldsCanBeUpdated) {

        if (businessFieldsCanBeUpdated) {
            observation.setSpecies(species);
            observation.updateGeoLocation(dto.getGeoLocation(), gisQueryService);
            observation.setPointOfTime(DateUtil.toDateNullSafe(dto.getPointOfTime()));
            observation.setObservationType(dto.getObservationType());

            // Invoking setters of mooselike-amount fields will eventually override the value set here.
            observation.setAmount(dto.getAmount());

            if (observation.getHuntingDayOfGroup() != null && !dto.observedWithinMooseHunting()) {
                throw new MessageExposableValidationException(
                        "Observation must be done within moose hunting when linked to hunting group");
            }

            observation
                    .withMooselikeAmounts(
                            dto.getMooselikeMaleAmount(), dto.getMooselikeFemaleAmount(),
                            dto.getMooselikeFemale1CalfAmount(), dto.getMooselikeFemale2CalfsAmount(),
                            dto.getMooselikeFemale3CalfsAmount(), dto.getMooselikeFemale4CalfsAmount(),
                            dto.getMooselikeUnknownSpecimenAmount())
                    .setWithinMooseHunting(dto.getWithinMooseHunting());
        }
    }

    // Reason for introducing this consumer is to do Harvest-related queries early and defer Harvest mutations.
    protected BiConsumer<Harvest, HarvestSpecVersion> prepareMutatorForPermitStateAndReportRequirement(
            final HarvestDTOBase dto,
            final GameSpecies species,
            final GeoLocation location,
            final Person currentPerson,
            final boolean isModerator) {

        final LocalDate harvestDate = dto.getPointOfTime().toLocalDate();

        final HarvestPermit permit = dto.hasPermitNumber()
                ? getValidPermit(dto.getPermitNumber(), species.getOfficialCode(), location, harvestDate)
                : null;

        final boolean harvestReportRequired =
                harvestReportRequirementsService.isHarvestReportRequired(species, harvestDate, location, permit);

        final StateAcceptedToHarvestPermit state =
                permit == null ? null : getStateAcceptedToPermit(permit, currentPerson, isModerator);

        if (permit != null) {
            final HarvestReportFields fields =
                    harvestReportFieldsRepository.findOne(JpaSpecs.and(
                            HarvestReportFieldsSpecs.withGameSpeciesCode(species.getOfficialCode()),
                            HarvestReportFieldsSpecs.withUsedWithPermit(true)));

            if (dto.getSpecimens() != null && fields != null) {
                assertSpecimensWithFields(dto.getSpecimens(), fields);
            }
        }

        return (harvest, specVersion) -> {

            if (specVersion.supportsHarvestPermitState()) {
                harvest.setHarvestPermit(permit);
                harvest.setStateAcceptedToHarvestPermit(state);
            }

            harvest.setHarvestReportRequired(harvestReportRequired);
        };
    }

    protected ObservationFieldPresenceValidator getObservationFieldsValidator(
            final GameSpecies species, final int metadataVersion) {

        return observationFieldsMetadataService.getObservationFieldsValidator(species, metadataVersion);
    }

    private HarvestPermit getValidPermit(
            final String permitNumber, final int gameSpeciesCode, final GeoLocation location, final LocalDate date) {

        RhyNotResolvableByGeoLocationException.assertNotNull(gisQueryService.findRhyByLocation(location));

        return Option.of(harvestPermitRepository.findByPermitNumber(permitNumber))
                .peek(permit -> assertPermitValid(permit, gameSpeciesCode, date))
                .getOrElseThrow(NotFoundException::new);
    }

}
