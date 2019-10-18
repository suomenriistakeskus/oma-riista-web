package fi.riista.util;

import org.junit.Test;

import java.util.Optional;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.EmailSanitizer.getSanitizedOrNull;
import static fi.riista.util.EmailSanitizer.sanitize;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EmailSanitizerTest {

    @Test
    public void testGetSanitizedOrNull() {
        assertNull(getSanitizedOrNull(null));
        assertNull(getSanitizedOrNull("abcde"));
        assertEquals("@", getSanitizedOrNull("@"));
        assertEquals("abc@de", getSanitizedOrNull("abc@de"));
        assertEquals("abc@de", getSanitizedOrNull(" abc@de  "));
        assertEquals("abc@de", getSanitizedOrNull("ABC@DE"));
    }

    @Test
    public void testSanitize_withStringParameter() {
        assertEquals(Optional.empty(), sanitize((String) null));
        assertEquals(Optional.empty(), sanitize("abcde"));
        assertEquals(Optional.of("@"), sanitize("@"));
        assertEquals(Optional.of("abc@de"), sanitize("abc@de"));
        assertEquals(Optional.of("abc@de"), sanitize(" abc@de  "));
        assertEquals(Optional.of("abc@de"), sanitize("ABC@DE"));
    }

    @Test
    public void testSanitize_withIterableParameter() {
        assertEmpty(sanitize((Iterable<String>) null));
        assertEmpty(sanitize(emptyList()));
        assertEmpty(sanitize(singleton("")));
        assertEmpty(sanitize(singleton("abcde")));
        assertEmpty(sanitize(asList("", "abcde")));

        assertEquals(F.newLinkedHashSet("@"), sanitize(singleton("@")));
        assertEquals(F.newLinkedHashSet("abc@de"), sanitize(singleton("abc@de")));
        assertEquals(F.newLinkedHashSet("abc@de"), sanitize(singleton(" abc@de  ")));
        assertEquals(F.newLinkedHashSet("abc@de"), sanitize(singleton("ABC@DE")));

        assertEquals(F.newLinkedHashSet("@", "abc@de"), sanitize(asList("@", "  abc@DE  ")));
        assertEquals(F.newLinkedHashSet("@", "abc@de"), sanitize(asList("", "@", "abcde", "ABC@DE")));
    }
}
