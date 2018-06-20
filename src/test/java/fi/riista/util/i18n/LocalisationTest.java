package fi.riista.util.i18n;

import fi.riista.ClassInventory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.util.LocalisedEnum;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.stream.Stream;

import static fi.riista.test.Asserts.assertEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalisationTest {

    @Test
    public void testThatFinnishLocalisationMapIsNotEmptyInFrontend() throws Exception {
        final LinkedHashMap<String, String> map = JsonToMap.readFileToMap(JsonToMap.FI);
        assertTrue(!map.isEmpty());
    }

    @Test
    public void testThatEachFinnishLocalisationKeyHasSwedishCounterpartInFrontend() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.SV));
    }

    @Test
    public void testThatEachFinnishLocalisationKeyHasEnglishCounterpartInFrontend() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.EN));
    }

    @Test
    public void testThatFinnishLocalisationsExistForEachLocalisedEnumValue() throws IOException {
        testThatLocalisationsExistForEachLocalisedEnumValue("src/main/resources/i18n/messages.properties");
    }

    @Test
    public void testThatSwedishLocalisationsExistForEachLocalisedEnumValue() throws IOException {
        testThatLocalisationsExistForEachLocalisedEnumValue("src/main/resources/i18n/messages_sv.properties");
    }

    public void testThatLocalisationsExistForEachLocalisedEnumValue(final String propertiesFilePath) throws IOException {
        final Properties properties = new Properties();

        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFilePath))) {

            properties.load(bis);

            final Stream<String> failingEnumValues = streamLocalisedEnumValues()
                    .map(EnumLocaliser::resourceKey)
                    .filter(localisationKey -> !properties.containsKey(localisationKey));

            assertEmpty(failingEnumValues,
                    "The following Enum values are missing from file '" + propertiesFilePath + "'");
        }
    }

    @SuppressWarnings("unchecked")
    private static Stream<Enum<?>> streamLocalisedEnumValues() {
        return ClassInventory.getEnumClasses()
                .stream()
                .map(clazz -> (Class<? extends Enum<?>>) clazz)
                .filter(LocalisedEnum.class::isAssignableFrom)
                .flatMap(clazz -> Arrays.stream(clazz.getEnumConstants()));
    }

    private static void compare(final LinkedHashMap<String, String> mapA, final LinkedHashMap<String, String> mapB) {
        assertEquals(mapA.size(), mapB.size());

        mapA.forEach((key, value) -> {
            // key in A should be present in B
            assertThat(mapB, hasKey(key));

            // if value in A is not empty, then it shouldn't be empty in B either
            final String bValue = mapB.get(key);
            assertEquals(value.isEmpty(), bValue.isEmpty());
        });
    }
}
