package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import fi.riista.config.Constants;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.observation.specimen.GameMarking;
import fi.riista.feature.gamediary.observation.specimen.ObservedGameState;
import fi.riista.util.jpa.JpaGroupingUtils;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class ObservationFieldsMetadataService {

    private static GameSpeciesObservationMetadataDTO constructMetadata(
            final GameSpecies species,
            final ObservationBaseFields fields,
            final List<ObservationContextSensitiveFields> listOfContextSensitiveFields,
            final boolean doNotIncludeDenyingFieldRequirements) {

        final GameSpeciesObservationMetadataDTO dto =
                new GameSpeciesObservationMetadataDTO(species.getId(), species.getOfficialCode());

        populateDTO(dto, fields, listOfContextSensitiveFields);

        if (doNotIncludeDenyingFieldRequirements) {
            dto.removeDenyingFieldRequirements();
        }

        dto.setLastModified(getMaxLastModifiedTimestamp(fields, listOfContextSensitiveFields)
                .orElseThrow(() -> new IllegalStateException("maxLastModifiedTimestamp not found")));

        return dto;
    }

    private static void populateDTO(
            final GameSpeciesObservationFieldRequirementsDTO dto,
            final ObservationBaseFields fields,
            final List<ObservationContextSensitiveFields> listOfContextSensitiveFields) {

        dto.setWithinMooseHunting(fields.getWithinMooseHunting());

        listOfContextSensitiveFields.forEach(ctxSensitiveFields -> {
            final GameSpeciesObservationFieldRequirementsDTO.ContextSensitiveFieldSetDTO fieldSetDto = dto.new ContextSensitiveFieldSetDTO(
                    ctxSensitiveFields.isWithinMooseHunting(), ctxSensitiveFields.getObservationType());

            fillRequirementsToFieldSetDTO(fieldSetDto, ctxSensitiveFields);

            dto.getContextSensitiveFieldSets().add(fieldSetDto);
        });

        dto.getContextSensitiveFieldSets().sort((o1, o2) -> {
            // Order withinMooseHunting-enabled first, secondarily by ordinal
            // number of ObservationType
            return o1.isWithinMooseHunting() != o2.isWithinMooseHunting()
                    ? o1.isWithinMooseHunting() ? -1 : 1
                    : o1.getType().ordinal() - o2.getType().ordinal();
        });
    }

    private static void fillRequirementsToFieldSetDTO(
            final GameSpeciesObservationFieldRequirementsDTO.ContextSensitiveFieldSetDTO dto, final ObservationContextSensitiveFields entity) {

        dto.setAmount(entity.getAmount());
        dto.setMooselikeMaleAmount(entity.getMooselikeMaleAmount());
        dto.setMooselikeFemaleAmount(entity.getMooselikeFemaleAmount());
        dto.setMooselikeFemale1CalfAmount(entity.getMooselikeFemale1CalfAmount());
        dto.setMooselikeFemale2CalfsAmount(entity.getMooselikeFemale2CalfsAmount());
        dto.setMooselikeFemale3CalfsAmount(entity.getMooselikeFemale3CalfsAmount());
        dto.setMooselikeFemale4CalfsAmount(entity.getMooselikeFemale4CalfsAmount());
        dto.setMooselikeUnknownSpecimenAmount(entity.getMooselikeUnknownSpecimenAmount());

        dto.setGender(entity.getGender());
        dto.setAge(entity.getAge());
        dto.setAllowedAges(entity.getAllowedGameAges());

        final Map<ObservedGameState, Required> validGameStates = entity.getValidGameStateRequirements();
        final boolean noValidGameStates = validGameStates.isEmpty();
        dto.setAllowedStates(
                noValidGameStates ? EnumSet.noneOf(ObservedGameState.class) : EnumSet.copyOf(validGameStates.keySet()));
        dto.setState(noValidGameStates
                ? Required.NO
                : validGameStates.containsValue(Required.YES) ? Required.YES : Required.VOLUNTARY);

        final Map<GameMarking, Required> validGameMarkings = entity.getValidGameMarkingRequirements();
        final boolean noValidGameMarkings = validGameMarkings.isEmpty();
        dto.setAllowedMarkings(
                noValidGameMarkings ? EnumSet.noneOf(GameMarking.class) : EnumSet.copyOf(validGameMarkings.keySet()));
        dto.setMarking(noValidGameMarkings
                ? Required.NO
                : validGameMarkings.containsValue(Required.YES) ? Required.YES : Required.VOLUNTARY);
    }

    private static void checkRequestedMetadataVersion(final int metadataVersion) {
        Preconditions.checkArgument(metadataVersion >= 1, "Metadata version must be positive integer");
    }

    private static Optional<DateTime> getMaxLastModifiedTimestamp(
            final ObservationBaseFields baseFields, final List<ObservationContextSensitiveFields> ctxSensitiveFields) {

        return Stream
                .concat(
                        Stream.of(baseFields.getModificationTime()),
                        ctxSensitiveFields.stream().map(ObservationContextSensitiveFields::getModificationTime))
                .max(Ordering.natural().nullsFirst())
                .map(d -> d.withZone(Constants.DEFAULT_TIMEZONE));
    }

    @Resource
    private GameSpeciesRepository speciesRepo;

    @Resource
    private ObservationBaseFieldsRepository baseFieldsRepo;

    @Resource
    private ObservationContextSensitiveFieldsRepository ctxSensitiveFieldsRepo;

    @Resource
    private GameDiaryService diaryService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ObservationMetadataDTO getObservationFieldsMetadata(final int metadataVersion) {
        checkRequestedMetadataVersion(metadataVersion);

        final List<GameSpecies> allSpecies = speciesRepo.findAll();

        final Function<GameSpecies, List<ObservationBaseFields>> baseFieldsFn =
                JpaGroupingUtils.createInverseMappingFunction(
                        allSpecies,
                        ObservationBaseFields_.species,
                        baseFieldsRepo,
                        JpaSpecs.equal(ObservationBaseFields_.metadataVersion, metadataVersion),
                        true);

        final Function<GameSpecies, List<ObservationContextSensitiveFields>> ctxSensitiveFieldsFn =
                JpaGroupingUtils.createInverseMappingFunction(
                        allSpecies,
                        ObservationContextSensitiveFields_.species,
                        ctxSensitiveFieldsRepo,
                        JpaSpecs.equal(ObservationContextSensitiveFields_.metadataVersion, metadataVersion),
                        true);

        final ObservationMetadataDTO dto = new ObservationMetadataDTO();
        final AtomicReference<DateTime> lastModifiedTS = new AtomicReference<>(null);

        allSpecies.stream()
                .map(species -> {
                    final List<ObservationBaseFields> baseFieldsList = baseFieldsFn.apply(species);

                    if (baseFieldsList == null || baseFieldsList.size() != 1) {
                        throw new IllegalStateException(String.format(
                                "Exactly one %s entry expected for game species with code %d, but found %d",
                                ObservationBaseFields.class.getName(),
                                species.getOfficialCode(),
                                baseFieldsList == null ? 0 : baseFieldsList.size()));
                    }

                    final ObservationBaseFields baseFields = baseFieldsList.get(0);
                    final List<ObservationContextSensitiveFields> ctxSensitiveFields =
                            ctxSensitiveFieldsFn.apply(species);

                    final GameSpeciesObservationFieldRequirementsDTO fieldsDTO =
                            new GameSpeciesObservationFieldRequirementsDTO(species.getOfficialCode());
                    populateDTO(fieldsDTO, baseFields, ctxSensitiveFields);
                    fieldsDTO.removeDenyingFieldRequirements();

                    // Update last-modified timestamp as side-effect.
                    final DateTime maxTS = getMaxLastModifiedTimestamp(baseFields, ctxSensitiveFields)
                            .orElseThrow(() -> new IllegalStateException("maxLastModifiedTimestamp not found"));
                    final DateTime currentMaxTS = lastModifiedTS.get();
                    if (currentMaxTS == null || maxTS.isAfter(currentMaxTS)) {
                        lastModifiedTS.set(maxTS);
                    }

                    return fieldsDTO;
                })
                .forEach(dto.getSpeciesList()::add);

        dto.setLastModified(lastModifiedTS.get());

        return dto;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSingleSpecies(
            @Nonnull final GameSpecies species,
            final int metadataVersion,
            final boolean doNotIncludeDenyingFieldRequirements) {

        Objects.requireNonNull(species);
        checkRequestedMetadataVersion(metadataVersion);

        final ObservationBaseFields baseFields = getObservationBaseFields(species, metadataVersion);

        return constructMetadata(
                species,
                baseFields,
                ctxSensitiveFieldsRepo.findBySpeciesAndMetadataVersion(species, metadataVersion),
                doNotIncludeDenyingFieldRequirements);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ObservationFieldPresenceValidator getObservationFieldsValidator(
            final int gameSpeciesCode, final int metadataVersion) {

        final GameSpecies species = diaryService.getGameSpeciesByOfficialCode(gameSpeciesCode);
        return getObservationFieldsValidator(species, metadataVersion);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ObservationFieldPresenceValidator getObservationFieldsValidator(
            @Nonnull final GameSpecies species, final int metadataVersion) {

        Objects.requireNonNull(species);

        final GameSpeciesObservationMetadataDTO metadata =
                getObservationFieldMetadataForSingleSpecies(species, metadataVersion, false);

        return new ObservationFieldPresenceValidator() {
            @Override
            public void validate(
                    final CanIdentifyObservationContextSensitiveFields observation, final Optional<List<?>> specimens) {

                metadata.assertAllFieldRequirements(observation, specimens);
            }

            @Override
            public void validate(
                    final CanIdentifyObservationContextSensitiveFields observation,
                    final Optional<List<?>> specimens,
                    final Set<String> namesOfExcludedObservationFields,
                    final Set<String> namesOfExcludedSpecimenFields) {

                metadata.assertAllFieldRequirements(
                        observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);
            }

            @Override
            public void nullifyProhibitedFields(
                    final CanIdentifyObservationContextSensitiveFields observation,
                    final Optional<List<?>> specimens,
                    final Set<String> namesOfExcludedObservationFields,
                    final Set<String> namesOfExcludedSpecimenFields) {

                metadata.nullifyProhibitedFields(
                        observation, specimens, namesOfExcludedObservationFields, namesOfExcludedSpecimenFields);
            }
        };
    }

    private ObservationBaseFields getObservationBaseFields(final GameSpecies species, final int metadataVersion) {
        return baseFieldsRepo
                .findBySpeciesAndMetadataVersion(species, metadataVersion)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Observation base metadata (v%d) for species with code %d not found",
                        metadataVersion, species.getOfficialCode())));
    }

}
