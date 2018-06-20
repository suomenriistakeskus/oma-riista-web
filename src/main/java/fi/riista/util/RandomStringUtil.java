package fi.riista.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomStringUtil {
    public static final int EXTERNAL_ID_LENGTH = 10;

    private static final int EXTERNAL_ID_RANDOM_BIT_COUNT = 128;

    public static String generateExternalId(final SecureRandom random) {
        final String base36 = new BigInteger(EXTERNAL_ID_RANDOM_BIT_COUNT, random).toString(36).toUpperCase();

        // replace hard to distinguish characters and trim length
        return base36.replaceAll("[0O1I]", "").substring(0, EXTERNAL_ID_LENGTH);
    }

    private RandomStringUtil() {
        throw new AssertionError();
    }
}
