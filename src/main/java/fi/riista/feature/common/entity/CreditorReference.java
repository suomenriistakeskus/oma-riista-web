package fi.riista.feature.common.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import fi.riista.validation.FinnishCreditorReference;
import fi.riista.validation.FinnishCreditorReferenceValidator;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Access(AccessType.FIELD)
public class CreditorReference implements Serializable {

    public static String getDelimitedValue(final String str) {
        if (str == null) {
            return null;
        }

        final String trimmed = StringUtils.replace(str, " ", "");

        if (trimmed.length() == 0) {
            return trimmed;
        }

        // Etunollia ei tulosteta.
        final String zerosRemoved = StringUtils.stripStart(trimmed, "0");

        if (zerosRemoved.length() == 0) {
            return "0";
        }

        // Viitenumero tulostetaan sille varattuun kenttään oikealta vasemmalle
        // viiden numeron ryhmiin, joiden välissä on tyhjä merkkipaikka.
        final String reversed = StringUtils.reverse(zerosRemoved);
        final Iterable<String> parts = Splitter.fixedLength(5).split(reversed);

        return StringUtils.reverse(Joiner.on(' ').join(parts));
    }

    public static CreditorReference fromNullable(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        final CreditorReference result = new CreditorReference();
        result.setValue(value);
        return result;
    }

    @FinnishCreditorReference
    @Convert(converter = CreditorReferenceConverter.class)
    @Column(length = 20)
    private String creditorReference;

    public boolean isValid() {
        return creditorReference != null && FinnishCreditorReferenceValidator.validate(creditorReference, true);
    }

    public Long parseLong() {
        if (creditorReference == null) {
            return null;
        }
        return Long.parseLong(StringUtils.replace(creditorReference, " ", ""));
    }

    @Override
    public String toString() {
        return creditorReference == null ? "<null>" : creditorReference;
    }

    // Accessors -->

    public String getValue() {
        return creditorReference;
    }

    public void setValue(final String value) {
        this.creditorReference = getDelimitedValue(value);
    }
}
