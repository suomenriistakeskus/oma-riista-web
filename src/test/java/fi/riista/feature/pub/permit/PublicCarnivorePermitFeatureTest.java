package fi.riista.feature.pub.permit;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Resource;

import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class PublicCarnivorePermitFeatureTest extends EmbeddedDatabaseTest {

    @DataPoints
    public static final int[] GAME_SPECIES_CODES = GameSpecies.ALL_GAME_SPECIES_CODES;

    @Resource
    private PublicCarnivorePermitFeature feature;

    @Theory
    public void testSpeciesCode_largeCarnivore(final int speciesCode) {
        assumeTrue(GameSpecies.isLargeCarnivore(speciesCode));

        feature.getPageNoAuthorization(
                null, speciesCode, null, null, pr());
    }

    @Theory
    public void testSpeciesCode_otherSpecies(final int speciesCode) {
        assumeFalse(GameSpecies.isLargeCarnivore(speciesCode));

        assertThrows(IllegalArgumentException.class,
                () -> feature.getPageNoAuthorization(null, speciesCode, null, null, pr()));
    }

    @Test
    public void testPermitNumber() {
        feature.getPageNoAuthorization(model().permitNumber(), null, null, null, pr());
    }

    @Test
    public void testPermitNumber_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> feature.getPageNoAuthorization(
                        "2020-1-000-10000-1", null, null, null, pr()));
    }

    @Test
    public void testPaging_invalidPageSize(){
        assertThrows(IllegalArgumentException.class,
                () -> feature.getPageNoAuthorization(
                        null, null, null, null, PageRequest.of(0, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> feature.getPageNoAuthorization(
                        null, null, null, null, PageRequest.of(0, -1)));
    }

    @Test
    public void testPaging_negativePageNumber(){
        assertThrows(IllegalArgumentException.class,
                () -> feature.getPageNoAuthorization(
                        null, null, null, null, PageRequest.of(-1, 10)));
    }

    private static Pageable pr() {
        return PageRequest.of(0, 10);
    }

}
