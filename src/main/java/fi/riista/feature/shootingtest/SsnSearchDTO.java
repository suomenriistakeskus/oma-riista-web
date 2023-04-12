package fi.riista.feature.shootingtest;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.SafeHtml;

public class SsnSearchDTO {
    @NotNull
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String ssn;

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
}
