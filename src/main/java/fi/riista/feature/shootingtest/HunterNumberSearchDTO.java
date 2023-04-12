package fi.riista.feature.shootingtest;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.SafeHtml;

public class HunterNumberSearchDTO {
    @NotNull
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String hunterNumber;

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }
}
