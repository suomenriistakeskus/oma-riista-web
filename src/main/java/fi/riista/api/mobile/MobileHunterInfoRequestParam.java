package fi.riista.api.mobile;

import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import java.io.Serializable;
import javax.validation.constraints.AssertTrue;

public class MobileHunterInfoRequestParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @FinnishHunterNumber
    private String hunterNumber;

    @FinnishSocialSecurityNumber
    private String ssn;

    @AssertTrue(message = "Either hunterNumber or ssn needs to be present, but not both.")
    public boolean isValid() {
        if (getHunterNumber() == null) {
            return getSsn() != null;
        }
        if (getSsn() == null) {
            return getHunterNumber() != null;
        }
        return false;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(final String ssn) {
        this.ssn = ssn;
    }
}
