package fi.riista.feature.announcement.show;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import junit.framework.TestCase;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class MobileAnnouncementDTOTest extends TestCase {

    @Test
    public void testIsNotAbbreviated() {
        assertThat(createDTO("1234", "1234").copyAbbreviated(6, 6).isAbbreviated(), is(false));
        assertThat(createDTO("12345", "12345").copyAbbreviated(6, 6).isAbbreviated(), is(false));
        assertThat(createDTO("123456", "123456").copyAbbreviated(6, 6).isAbbreviated(), is(false));
    }

    @Test
    public void testIsAbbreviated() {
        assertThat(createDTO("1234567", "1234567").copyAbbreviated(6, 6).isAbbreviated(), is(true));
        assertThat(createDTO("123", "1234567").copyAbbreviated(6, 6).isAbbreviated(), is(true));
        assertThat(createDTO("1234567", "123").copyAbbreviated(6, 6).isAbbreviated(), is(true));
    }

    @Test
    public void testAbbreviatedEllipsis() {
        assertThat(createDTO("123456", "123").copyAbbreviated(6, 6).getSubject().endsWith("..."), is(false));
        assertThat(createDTO("1234567", "123").copyAbbreviated(6, 6).getSubject().endsWith("..."), is(true));
        assertThat(createDTO("12345678", "123").copyAbbreviated(6, 6).getSubject().endsWith("..."), is(true));
    }

    @Test
    public void testAbbreviatedValueIsCopied() {
        final MobileAnnouncementDTO originallyAbbreviated = createDTO("12345678", "123").copyAbbreviated(6, 6);
        assertThat(originallyAbbreviated.copyAbbreviated(999, 999).isAbbreviated(), is(true));
    }

    private static MobileAnnouncementDTO createDTO(final String subject, final String body) {
        return new MobileAnnouncementDTO(1L, 1, LocalDateTime.now(),
                new MobileAnnouncementSenderDTO(Map.of(), Map.of(), ""), subject, body);
    }
}