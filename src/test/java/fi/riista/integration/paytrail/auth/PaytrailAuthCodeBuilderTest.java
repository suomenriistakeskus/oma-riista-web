package fi.riista.integration.paytrail.auth;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;

public class PaytrailAuthCodeBuilderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testInvalidDelimiterCharacter() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Field contains delimiter character");

        new PaytrailAuthCodeBuilder("secret", PaytrailAuthCodeDigest.SHA256)
                .withFields(Collections.singletonList("a|b"))
                .getMessage(PaytrailAuthCodeBuilder.SecretAlignment.BEFORE);
    }
}
