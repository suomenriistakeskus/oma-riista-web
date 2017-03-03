package fi.riista.feature.account.registration;

import com.google.common.collect.ImmutableMap;
import fi.riista.util.NumberGenerator;
import fi.riista.util.NumberSequence;
import fi.riista.util.ValueGeneratorMixin;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class SamlUserAttributesParserTest implements ValueGeneratorMixin {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public NumberGenerator getNumberGenerator() {
        return NumberSequence.INSTANCE;
    }

    @Test
    public void testMinimalAttributes() {
        final String ssn = ssn();
        final String firstNames = "FirstName-" + zeroPaddedNumber(3);
        final String lastName = "LastName-" + zeroPaddedNumber(3);

        final SamlUserAttributes result = new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList(ssn))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList(firstNames))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList(lastName))
                .build()).parse();

        assertEquals(ssn, result.getSsn());
        assertEquals(firstNames, result.getFirstNames());
        assertEquals(lastName, result.getLastName());
    }

    @Test
    public void testAllAttributes() {
        final String ssn = ssn();
        final String firstNames = "FirstName-" + zeroPaddedNumber(3);
        final String byName = "ByName-" + zeroPaddedNumber(3);
        final String lastName = "LastName-" + zeroPaddedNumber(3);
        final String municipalityCode = zeroPaddedNumber(3);

        final SamlUserAttributes result = new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList(ssn))
                .put(SamlUserAttributesParser.KEY_BY_NAME, singletonList(byName))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList(firstNames))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList(lastName))
                .build()).parse();

        assertEquals(ssn, result.getSsn());
        assertEquals(firstNames, result.getFirstNames());
        assertEquals(byName, result.getByName());
        assertEquals(lastName, result.getLastName());
    }

    @Test
    public void testMissingSsn() {
        thrown.expectMessage("Attribute missing: ssn");

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList("firstName"))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList("lastName"))
                .build()).parse();
    }

    @Test
    public void testInvalidSsn() {
        thrown.expectMessage("Invalid SSN");

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList("invalid"))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList("firstName"))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList("lastName"))
                .build()).parse();
    }

    @Test
    public void testMultipleSsn() {
        thrown.expectMessage(Matchers.startsWith("expected one element"));

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, asList(ssn(), ssn()))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList("firstName"))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList("lastName"))
                .build()).parse();
    }

    @Test
    public void testEmptySsnList() {
        thrown.expectMessage("Attribute missing: ssn");

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, emptyList())
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList("firstName"))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList("lastName"))
                .build()).parse();
    }

    @Test
    public void testMissingFirstNames() {
        thrown.expectMessage("Attribute missing: firstNames");

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList(ssn()))
                .put(SamlUserAttributesParser.KEY_LAST_NAME, singletonList("lastName"))
                .build()).parse();
    }

    @Test
    public void testMissingLastName() {
        thrown.expectMessage("Attribute missing: lastName");

        new SamlUserAttributesParser(ImmutableMap.<String, List<String>>builder()
                .put(SamlUserAttributesParser.KEY_SSN, singletonList(ssn()))
                .put(SamlUserAttributesParser.KEY_FIRST_NAMES, singletonList("firstName"))
                .build()).parse();
    }
}
