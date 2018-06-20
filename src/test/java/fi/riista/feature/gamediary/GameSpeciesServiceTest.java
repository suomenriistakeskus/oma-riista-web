package fi.riista.feature.gamediary;

import com.google.common.collect.ImmutableMap;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static fi.riista.test.TestUtils.createList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class GameSpeciesServiceTest extends EmbeddedDatabaseTest {

    private static ImmutableMap<String, Object> transformToMap(final GameSpeciesDTO dto) {
        return ImmutableMap.of(
                "code", dto.getCode(),
                "names", dto.getName(),
                "catId", dto.getCategoryId(),
                "multiple", dto.isMultipleSpecimenAllowedOnHarvests());
    }

    @Resource
    private GameSpeciesService gameSpeciesService;

    @Test
    @Transactional
    public void testGetGameSpecies() {
        final List<GameSpecies> species = createList(10, model()::newGameSpecies);

        persistInCurrentlyOpenTransaction();

        final Set<GameSpeciesDTO> expected = species.stream().map(GameSpeciesDTO::create).collect(toSet());

        final List<GameSpeciesDTO> results = gameSpeciesService.listAll();

        assertEquals(
                F.mapNonNullsToSet(expected, GameSpeciesServiceTest::transformToMap),
                F.mapNonNullsToSet(results, GameSpeciesServiceTest::transformToMap));
    }
}
