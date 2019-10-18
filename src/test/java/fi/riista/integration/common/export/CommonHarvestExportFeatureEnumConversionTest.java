package fi.riista.integration.common.export;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.specimen.GameAntlersType;
import fi.riista.feature.gamediary.harvest.specimen.GameFitnessClass;
import fi.riista.integration.common.export.harvests.CHAR_GameAge;
import fi.riista.integration.common.export.harvests.CHAR_GameAntlersType;
import fi.riista.integration.common.export.harvests.CHAR_GameFitnessClass;
import fi.riista.integration.common.export.harvests.CHAR_GameGender;
import fi.riista.util.EnumUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonHarvestExportFeatureEnumConversionTest {

    private static <A extends Enum<A>, B extends Enum<B>>
    void assertEnumValuesMatch(final Class<A> first, final Class<B> second) {
        assertEquals(first.getEnumConstants().length, second.getEnumConstants().length);

        for (final A a : first.getEnumConstants()) {
            final B b = EnumUtils.convertNullableByEnumName(second, a);
            assertEquals(a.name(), b.name());
        }
    }

    @Test
    public void testGameGender() {
        assertEnumValuesMatch(GameGender.class, CHAR_GameGender.class);
    }

    @Test
    public void testGameAge() {
        assertEnumValuesMatch(GameAge.class, CHAR_GameAge.class);
    }

    @Test
    public void testGameFitnessClass() {
        assertEnumValuesMatch(GameFitnessClass.class, CHAR_GameFitnessClass.class);
    }

    @Test
    public void testGameAntlersType() {
        assertEnumValuesMatch(GameAntlersType.class, CHAR_GameAntlersType.class);
    }

}
