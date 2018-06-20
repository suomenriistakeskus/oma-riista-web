package fi.riista.feature.permit;

import fi.riista.validation.FinnishHuntingPermitNumberValidator;
import org.apache.commons.lang.StringUtils;

public class PermitNumberUtil {
    public static String createPermitNumber(final int year, final int validYears, final int applicationNumber) {
        final String appNoStr = StringUtils.leftPad(Integer.toString(applicationNumber), 8, '0');
        final String permitNumber = String.format("%d-%d-%s-%s",
                year, validYears, appNoStr.substring(0, 3), appNoStr.substring(3));
        return permitNumber + '-' + FinnishHuntingPermitNumberValidator.calculateChecksum(permitNumber);
    }

    private PermitNumberUtil() {
        throw new AssertionError();
    }
}
