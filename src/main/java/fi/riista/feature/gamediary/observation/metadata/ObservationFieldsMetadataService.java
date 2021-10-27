package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.collect.Ordering;
import fi.riista.config.Constants;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.GameSpecies_;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static fi.riista.util.Collect.indexingBy;
import static fi.riista.util.Functions.idOf;
import static fi.riista.util.jpa.JpaGroupingUtils.createInverseMappingFunction;
import static fi.riista.util.jpa.JpaSpecs.equal;

@Component
public class ObservationFieldsMetadataService {

    @Resource
    private GameSpeciesRepository speciesRepo;

    @Resource
    private ObservationBaseFieldsRepository baseFieldsRepo;

    @Resource
    private ObservationContextSensitiveFieldsRepository ctxSensitiveFieldsRepo;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ObservationFieldValidator getObservationFieldValidator(
            @Nonnull final ObservationContext context,
            final boolean isUserActiveCarnivoreContactPerson) {

        Objects.requireNonNull(context);

        final ObservationBaseFields baseFields = baseFieldsRepo.getOne(context.getGameSpeciesCode(), context.getMetadataVersion());

        final ObservationContextSensitiveFields fields = ctxSensitiveFieldsRepo.findOne(
                context.getGameSpeciesCode(),
                context.getObservationCategory(),
                context.getObservationType(),
                context.getMetadataVersion())
                .orElseThrow(() -> new ObservationContextSensitiveFieldsNotFoundException(
                        "Cannot resolve context sensitive fields for observation context: " + context.toString()));

        return new ObservationFieldValidator(baseFields, fields, isUserActiveCarnivoreContactPerson);
    }

    @Transactional(readOnly = true)
    public ObservationMetadataDTO getObservationFieldsMetadata(@Nonnull final ObservationSpecVersion specVersion) {
        Objects.requireNonNull(specVersion);

        final int metadataVersion = specVersion.getMetadataVersion();

        final List<GameSpecies> allSpecies = specVersion.lessThan(ObservationSpecVersion._3)
                ? speciesRepo.findAll()
                : speciesRepo.findAll(JpaSort.of(GameSpecies_.officialCode));

        final Map<Long, ObservationBaseFields> baseFieldsBySpeciesId =
                baseFieldsRepo.findByMetadataVersion(metadataVersion)
                        .stream()
                        .collect(indexingBy(idOf(ObservationBaseFields::getSpecies)));

        final Function<GameSpecies, List<ObservationContextSensitiveFields>> ctxSensitiveFieldsFn =
                createInverseMappingFunction(
                        allSpecies,
                        ObservationContextSensitiveFields_.species,
                        ctxSensitiveFieldsRepo,
                        equal(ObservationContextSensitiveFields_.metadataVersion, metadataVersion),
                        true);

        final ObservationMetadataDTO dto = new ObservationMetadataDTO();
        final AtomicReference<DateTime> lastModifiedTS = new AtomicReference<>(null);

        allSpecies.stream()
                .map(species -> {
                    final long speciesId = species.getId();
                    final ObservationBaseFields baseFields = baseFieldsBySpeciesId.computeIfAbsent(speciesId, s -> {
                        throw new NotFoundException(String.format(
                                "Could not find observation base fields for species code %d with metadataVersion %d",
                                species.getOfficialCode(), metadataVersion));
                    });

                    final List<ObservationContextSensitiveFields> ctxFieldsets = ctxSensitiveFieldsFn.apply(species);
                    final GameSpeciesObservationFieldRequirementsDTO fieldsDTO =
                            new GameSpeciesObservationFieldRequirementsDTO(baseFields, ctxFieldsets, true);

                    // Update last-modified timestamp as side-effect.
                    final DateTime maxTS = getMaxLastModifiedTimestamp(baseFields, ctxFieldsets);
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

    @Transactional(readOnly = true)
    public GameSpeciesObservationMetadataDTO getObservationFieldMetadataForSingleSpecies(
            final int gameSpeciesCode,
            @Nonnull final ObservationSpecVersion specVersion,
            final boolean omitNullValueRequirements) {

        Objects.requireNonNull(specVersion);

        final int metadataVersion = specVersion.getMetadataVersion();
        final ObservationBaseFields baseFields = baseFieldsRepo.getOne(gameSpeciesCode, metadataVersion);
        final List<ObservationContextSensitiveFields> listOfContextSensitiveFields =
                ctxSensitiveFieldsRepo.findAll(gameSpeciesCode, metadataVersion);

        final GameSpeciesObservationMetadataDTO dto = new GameSpeciesObservationMetadataDTO(
                baseFields, listOfContextSensitiveFields, omitNullValueRequirements);

        dto.setLastModified(getMaxLastModifiedTimestamp(baseFields, listOfContextSensitiveFields));

        return dto;
    }

    private static DateTime getMaxLastModifiedTimestamp(final ObservationBaseFields baseFields,
                                                        final List<ObservationContextSensitiveFields> ctxFieldsets) {

        return Stream
                .concat(
                        Stream.of(baseFields.getModificationTime()),
                        ctxFieldsets.stream().map(ObservationContextSensitiveFields::getModificationTime))
                .max(Ordering.natural().nullsFirst())
                .map(d -> d.withZone(Constants.DEFAULT_TIMEZONE))
                .orElseThrow(() -> new IllegalStateException("maxLastModifiedTimestamp could not be resolved"));
    }
}
