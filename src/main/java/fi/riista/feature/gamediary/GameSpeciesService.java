package fi.riista.feature.gamediary;

import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.observation.ObservationCategory;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.ObservationContextSensitiveFields_;
import fi.riista.util.LocalisedString;
import fi.riista.util.jpa.JpaSubQuery;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class GameSpeciesService {

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Transactional(readOnly = true)
    public GameSpecies requireByOfficialCode(final int officialCode) {
        return gameSpeciesRepository
                .findByOfficialCode(officialCode)
                .orElseThrow(() -> new GameSpeciesNotFoundException(officialCode));
    }

    @Transactional(readOnly = true)
    public List<GameSpecies> requireByOfficialCodes(List<Integer> officialCodes) {
        requireNonNull(officialCodes);
        checkArgument(!officialCodes.isEmpty());

        return gameSpeciesRepository
                .findByOfficialCodeIn(officialCodes);
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> listAll() {
        return gameSpeciesRepository.findAll().stream()
                .map(GameSpeciesDTO::create)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> listSpeciesForObservationCategory(final ObservationCategory category) {
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findAll(JpaSubQuery
                .of(GameSpecies_.observationContextSensitiveFields)
                .exists((root, cb) -> cb.and(
                        cb.equal(root.get(ObservationContextSensitiveFields_.observationCategory), category),
                        cb.equal(
                                root.get(ObservationContextSensitiveFields_.metadataVersion),
                                ObservationSpecVersion.MOST_RECENT.toIntValue())))));
    }

    @Cacheable(value = "gameSpeciesNameIndex")
    @Transactional(readOnly = true)
    public Map<Integer, LocalisedString> getNameIndex() {
        return gameSpeciesRepository.findAll().stream()
                .collect(toMap(GameSpecies::getOfficialCode, GameSpecies::getNameLocalisation));
    }

    public List<GameCategoryDTO> getCategories() {
        return Arrays.stream(GameCategory.values()).map(enumValue -> new GameCategoryDTO(
                enumValue.getOfficialCode(), enumLocaliser.getLocalisedString(enumValue)))
                .collect(toList());
    }
}
