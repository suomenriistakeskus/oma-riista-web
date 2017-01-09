package fi.riista.feature.mail.token;

import com.google.common.io.BaseEncoding;
import fi.riista.feature.common.entity.PersistableEnum;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;

import javax.annotation.Nonnull;

public enum EmailTokenType implements PersistableEnum {
    VERIFY_EMAIL("E", Period.hours(12)),
    PASSWORD_RESET("P", Period.hours(2));

    private final String databaseValue;
    private final Period validityPeriod;

    EmailTokenType(final String databaseValue,
                   final Period validityPeriod) {
        this.databaseValue = databaseValue;
        this.validityPeriod = validityPeriod;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }

    public DateTime calculateValidUntil(final @Nonnull DateTime now) {
        return now.plus(validityPeriod);
    }

    public boolean isUserRequired() {
        return this != VERIFY_EMAIL;
    }

    public static String generateSecureToken(final BytesKeyGenerator pseudoRandomGenerator) {
        final String randomString = BaseEncoding.base64Url().encode(pseudoRandomGenerator.generateKey());
        return cut(randomString, 255);
    }

    private static String cut(final String s, final int maxLength) {
        return s.length() > maxLength ? s.substring(0, maxLength) : s;
    }
}
