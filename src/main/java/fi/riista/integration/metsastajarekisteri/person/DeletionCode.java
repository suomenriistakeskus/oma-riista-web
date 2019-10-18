package fi.riista.integration.metsastajarekisteri.person;

import org.apache.commons.lang.StringUtils;

public enum DeletionCode {

    // Kuollut
    DECEASED,

    // Muu syy
    OTHER;

    public static DeletionCode parse(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        switch (value.charAt(0)) {
            case 'D':
                return DECEASED;
            case 'K': // Invalid value to be replaced by 'O'
            case 'O':
                return OTHER;
            default:
                throw new IllegalArgumentException("Invalid deletionCode: " + value);
        }
    }
}
