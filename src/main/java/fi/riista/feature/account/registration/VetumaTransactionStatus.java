package fi.riista.feature.account.registration;

import fi.riista.feature.common.entity.PersistableEnum;

public enum VetumaTransactionStatus implements PersistableEnum {
    INIT("I"),
    MAC_ERROR("M"),
    ERROR("E"),
    CANCELLED("C"),
    SUCCESS("S"),
    TIMEOUT("T"),
    FINISHED("F");

    private final String databaseValue;

    VetumaTransactionStatus(final String databaseValue) {
        this.databaseValue = databaseValue;
    }

    @Override
    public String getDatabaseValue() {
        return databaseValue;
    }
}
