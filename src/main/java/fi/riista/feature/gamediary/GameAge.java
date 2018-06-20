package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.util.LocalisedEnum;

public enum GameAge implements HasOfficialCode, LocalisedEnum {

    ADULT(1),
    YOUNG(2),
    UNKNOWN(3);

    private final int officialCode;

    private GameAge(int code) {
        this.officialCode = code;
    }

    @Override
    public int getOfficialCode() {
        return officialCode;
    }
}
