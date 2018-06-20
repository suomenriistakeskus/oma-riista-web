package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.common.entity.HasOfficialCode;
import fi.riista.util.LocalisedEnum;

import javax.annotation.Nullable;

public enum GameGender implements HasOfficialCode, HasMooseDataCardEncoding<GameGender>, LocalisedEnum {

    FEMALE(1, "N"),
    MALE(2, "U"),
    UNKNOWN(3, null);

    private final int officialCode;
    private final String mooseDataCardEncoding;

    GameGender(final int code, @Nullable final String mooseDataCardEncoding) {
        this.officialCode = code;
        this.mooseDataCardEncoding = mooseDataCardEncoding;
    }

    @Override
    public int getOfficialCode() {
        return officialCode;
    }

    @Override
    public String getMooseDataCardEncoding() {
        return mooseDataCardEncoding;
    }
}
