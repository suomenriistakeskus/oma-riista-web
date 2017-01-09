package fi.riista.feature.gamediary;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.util.TestUtils.createList;
import static org.junit.Assert.assertEquals;

public class GameDiaryServiceTest extends EmbeddedDatabaseTest {

    private static ImmutableMap<String, Object> transformToMap(final GameSpeciesDTO dto) {
        return ImmutableMap.of(
                "code", dto.getCode(),
                "names", dto.getName(),
                "catId", dto.getCategoryId(),
                "multiple", dto.isMultipleSpecimenAllowedOnHarvests());
    }

    @Resource
    private GameDiaryService service;

    @Test
    @Transactional
    public void testGetGameSpecies() {
        final List<GameSpecies> species = createList(10, model()::newGameSpecies);

        persistInCurrentlyOpenTransaction();

        final Set<GameSpeciesDTO> expected = species.stream()
                .map(GameSpeciesDTO::create)
                .collect(Collectors.toSet());

        final List<GameSpeciesDTO> results = service.getGameSpecies();

        assertEquals(
                F.mapNonNullsToSet(expected, GameDiaryServiceTest::transformToMap),
                F.mapNonNullsToSet(results, GameDiaryServiceTest::transformToMap));
    }
}
