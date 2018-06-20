package fi.riista.integration.paytrail.auth;

import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PaytrailAuthCodeBuilder {
    private static final String FIELD_DELIMITER = "|";

    public enum SecretAlignment {
        BEFORE,
        AFTER
    }

    private final String merchantSecret;
    private final PaytrailAuthCodeDigest authCodeDigest;
    private final List<String> fieldValues = new LinkedList<>();

    public PaytrailAuthCodeBuilder(final String merchantSecret,
                                   final PaytrailAuthCodeDigest paytrailAuthCodeDigest) {
        this.merchantSecret = Objects.requireNonNull(merchantSecret);
        this.authCodeDigest = Objects.requireNonNull(paytrailAuthCodeDigest);
    }

    public PaytrailAuthCodeBuilder withFields(final List<String> fields) {
        fieldValues.addAll(fields);
        return this;
    }

    @Nonnull
    public String getAuthCode(final SecretAlignment alignment) {
        return Hex.encodeHexString(authCodeDigest.getMessageDigest(getMessage(alignment))).toUpperCase();
    }

    @Nonnull
    public String getMessage(final SecretAlignment alignment) {
        final List<String> copy = new ArrayList<>(fieldValues.size() + 1);

        if (alignment == SecretAlignment.BEFORE) {
            copy.add(merchantSecret);
        }

        copy.addAll(fieldValues);

        if (alignment == SecretAlignment.AFTER) {
            copy.add(merchantSecret);
        }

        if (copy.stream().anyMatch(f -> f.contains(FIELD_DELIMITER))) {
            throw new IllegalArgumentException("Field contains delimiter character");
        }

        return Joiner.on(FIELD_DELIMITER).join(copy);
    }
}
