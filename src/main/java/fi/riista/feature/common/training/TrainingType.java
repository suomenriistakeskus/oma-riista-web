package fi.riista.feature.common.training;

import fi.riista.feature.common.entity.PersistableEnum;
import fi.riista.util.LocalisedEnum;

public enum TrainingType implements LocalisedEnum, PersistableEnum {
    LAHI("L"),
    SAHKOINEN("S");

    private final String databaseValue;

    TrainingType(final String databaseValue) {
        this.databaseValue = databaseValue;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }
}
