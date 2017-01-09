package fi.riista.util.i18n;

import org.junit.Test;

import java.util.LinkedHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalisationTest {

    @Test
    public void testFinnish() throws Exception {
        LinkedHashMap<String, String> map = JsonToMap.readFileToMap(JsonToMap.FI);
        assertTrue(!map.isEmpty());
    }

    @Test
    public void testSwedish() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.SV));
    }

    @Test
    public void testEnglish() throws Exception {
        compare(JsonToMap.readFileToMap(JsonToMap.FI), JsonToMap.readFileToMap(JsonToMap.EN));
    }

    private static void compare(LinkedHashMap<String, String> mapA, LinkedHashMap<String, String> mapB) {
        assertEquals(mapA.size(), mapB.size());

        mapA.forEach((key, value) -> {
            //key in A should be present in B
            assertThat(mapB, hasKey(key));

            //if value in A is not empty, then it shouldn't be empty in B either
            String bValue = mapB.get(key);
            assertEquals(value.isEmpty(), bValue.isEmpty());
        });
    }
}
