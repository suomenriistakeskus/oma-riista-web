package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;

public enum GameFitnessClass implements HasMooseDataCardEncoding<GameFitnessClass> {

    ERINOMAINEN(1),
    NORMAALI(2),
    LAIHA(3),
    NAANTYNYT(4);

    private final String mooseDataCardEncoding;

    GameFitnessClass(final int mooseDataCardEncoding) {
        this.mooseDataCardEncoding = String.valueOf(mooseDataCardEncoding);
    }

    @Override
    public String getMooseDataCardEncoding() {
        return mooseDataCardEncoding;
    }

}
