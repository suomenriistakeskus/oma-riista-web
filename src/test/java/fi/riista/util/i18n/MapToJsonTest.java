package fi.riista.util.i18n;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class MapToJsonTest {

    @Test
    public void test() throws IOException {
        Map<String, String> map = ImmutableMap.of(
            "a.a", "aa",
            "a.b.a", "aba",
            "a.b.b.a", "abba",
            "a.b.b.b", "abbb");

        String result = MapToJson.toJson(map, false);
        assertEquals("{\"a\":{\"a\":\"aa\",\"b\":{\"a\":\"aba\",\"b\":{\"a\":\"abba\",\"b\":\"abbb\"}}}}", result);
    }
}
