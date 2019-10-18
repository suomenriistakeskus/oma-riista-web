package fi.riista.integration.common.export;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.srva.SrvaEventNameEnum;
import fi.riista.feature.gamediary.srva.SrvaEventTypeEnum;
import fi.riista.feature.gamediary.srva.SrvaResultEnum;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.integration.common.export.srva.CEV_GameAge;
import fi.riista.integration.common.export.srva.CEV_GameGender;
import fi.riista.integration.common.export.srva.CEV_SRVAEventName;
import fi.riista.integration.common.export.srva.CEV_SRVAEventResult;
import fi.riista.integration.common.export.srva.CEV_SRVAEventType;
import fi.riista.integration.common.export.srva.CEV_SRVAMethod;
import fi.riista.util.EnumUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommonSrvaEventExportFeatureEnumConversionTest {

    private static <A extends Enum<A>, B extends Enum<B>>
    void assertEnumValuesMatch(final Class<A> first, final Class<B> second) {
        assertEquals(first.getEnumConstants().length, second.getEnumConstants().length);

        for (final A a : first.getEnumConstants()) {
            final B b = EnumUtils.convertNullableByEnumName(second, a);
            assertEquals(a.name(), b.name());
        }
    }

    @Test
    public void testSrvaEventNameEnum() {
        assertEnumValuesMatch(SrvaEventNameEnum.class, CEV_SRVAEventName.class);
    }

    @Test
    public void testSrvaEventTypeEnum() {
        assertEnumValuesMatch(SrvaEventTypeEnum.class, CEV_SRVAEventType.class);
    }

    @Test
    public void testSrvaMethodEnum() {
        assertEnumValuesMatch(SrvaMethodEnum.class, CEV_SRVAMethod.class);
    }

    @Test
    public void testSrvaResultEnum() {
        assertEnumValuesMatch(SrvaResultEnum.class, CEV_SRVAEventResult.class);
    }

    @Test
    public void testGameGender() {
        assertEnumValuesMatch(GameGender.class, CEV_GameGender.class);
    }

    @Test
    public void testGameAge() {
        assertEnumValuesMatch(GameAge.class, CEV_GameAge.class);
    }

}

