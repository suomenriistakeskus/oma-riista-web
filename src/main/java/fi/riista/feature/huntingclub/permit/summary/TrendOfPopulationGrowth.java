package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.common.entity.HasMooseDataCardEncoding;
import fi.riista.feature.common.entity.PersistableEnum;

public enum TrendOfPopulationGrowth implements PersistableEnum, HasMooseDataCardEncoding<TrendOfPopulationGrowth> {

    INCREASED("I", "KASVANUT"),
    UNCHANGED("U", "ENNALLAAN"),
    DECREASED("D", "VÄHENTYNYT");

    private final String databaseValue;

    private final String mooseDataCardEncoding;

    TrendOfPopulationGrowth(final String databaseValue, final String mooseDataCardEncoding) {
        this.databaseValue = databaseValue;
        this.mooseDataCardEncoding = mooseDataCardEncoding;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }

    @Override
    public String getMooseDataCardEncoding() {
        return mooseDataCardEncoding;
    }

}
