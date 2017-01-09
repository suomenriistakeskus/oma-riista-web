package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;

public enum GameAntlersType implements HasMooseDataCardEncoding<GameAntlersType> {

    HANKO("H"),
    LAPIO("L"),
    SEKA("S");

    private final String mooseDataCardEncoding;

    GameAntlersType(final String mooseDataCardEncoding) {
        this.mooseDataCardEncoding = mooseDataCardEncoding;
    }

    @Override
    public String getMooseDataCardEncoding() {
        return mooseDataCardEncoding;
    }

}
