package fi.riista.feature.account.registration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.onelogin.saml2.Auth;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SamlUserAttributesParser {
    public static SamlUserAttributesParser create(final Auth auth) {
        Preconditions.checkArgument(auth.isAuthenticated(), "not authenticated");
        Preconditions.checkArgument(auth.getErrors().isEmpty(), "response contains errors");

        return new SamlUserAttributesParser(auth.getAttributes());
    }

    // @see https://esuomi.fi/palveluntarjoajille/tunnistaminen/tekninen-aineisto/tunnistetusta-kayttajasta-valitettavat-attribuutit/
    static final String KEY_SSN = "urn:oid:1.2.246.21";
    static final String KEY_LAST_NAME = "urn:oid:2.5.4.4";
    static final String KEY_BY_NAME = "urn:oid:2.5.4.42";
    static final String KEY_FIRST_NAMES = "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName";

    private final Map<String, List<String>> attributes;

    // For unit-testing
    SamlUserAttributesParser(final Map<String, List<String>> attributes) {
        this.attributes = Objects.requireNonNull(attributes, "attributes is null");
    }

    public SamlUserAttributes parse() {
        return SamlUserAttributes.builder()
                .withSsn(parseSsn())
                .withLastName(parseLastName())
                .withFirstNames(parseFirstNames())
                .withByName(parseByName())
                .build();
    }

    private String parseSsn() {
        return requireKey(KEY_SSN, "ssn");
    }

    private String parseFirstNames() {
        return requireKey(KEY_FIRST_NAMES, "firstNames");
    }

    private String parseLastName() {
        return requireKey(KEY_LAST_NAME, "lastName");
    }

    private String parseByName() {
        return onlyValueOrEmpty(KEY_BY_NAME).orElse(null);
    }

    private String requireKey(final String key, final String attrName) {
        return onlyValueOrEmpty(key).orElseThrow(
                () -> new IllegalArgumentException("Attribute missing: " + attrName));
    }

    @Nonnull
    private Optional<String> onlyValueOrEmpty(final String key) {
        final List<String> stringList = attributes.get(key);

        return stringList == null || stringList.isEmpty()
                ? Optional.empty()
                : Optional.of(Iterables.getOnlyElement(stringList));
    }
}
