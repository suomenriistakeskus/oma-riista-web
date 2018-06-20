package fi.riista.feature.gamediary.harvest;

public class HarvestSpecVersionNotSupportedException extends IllegalArgumentException {
    public static HarvestSpecVersionNotSupportedException groupHuntingNotSupported(final HarvestSpecVersion specVersion) {
        return new HarvestSpecVersionNotSupportedException("Harvest for group hunting not supported for specVersion " + specVersion.toIntValue());
    }

    public static HarvestSpecVersionNotSupportedException permitNotSupported(final HarvestSpecVersion specVersion) {
        return new HarvestSpecVersionNotSupportedException("Harvest for permit not supported for specVersion " + specVersion.toIntValue());
    }

    public static HarvestSpecVersionNotSupportedException seasonNotSupported(final HarvestSpecVersion specVersion) {
        return new HarvestSpecVersionNotSupportedException("Harvest for season not supported for specVersion " + specVersion.toIntValue());
    }

    HarvestSpecVersionNotSupportedException(final String msg) {
        super(msg);
    }
}
