package fi.riista.feature.gamediary.observation.metadata;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ObservationContext {

    private final ObservationSpecVersion specVersion;
    private final int gameSpeciesCode;
    private final boolean observedWithinMooseHunting;
    private final ObservationType observationType;

    public ObservationContext(@Nonnull final ObservationSpecVersion specVersion,
                              final int gameSpeciesCode,
                              final boolean observedWithinMooseHunting,
                              @Nonnull final ObservationType observationType) {

        Preconditions.checkArgument(gameSpeciesCode > 0, "gameSpeciesCode not set");

        this.specVersion = Objects.requireNonNull(specVersion, "specVersion is null");
        this.gameSpeciesCode = gameSpeciesCode;
        this.observedWithinMooseHunting = observedWithinMooseHunting;
        this.observationType = Objects.requireNonNull(observationType, "observationType is null");
    }

    public int getMetadataVersion() {
        return specVersion.getMetadataVersion();
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public boolean isObservedWithinMooseHunting() {
        return observedWithinMooseHunting;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    @Override
    public String toString() {
        return "ObservationContext{" +
                "specVersion=" + specVersion +
                ", gameSpeciesCode=" + gameSpeciesCode +
                ", observedWithinMooseHunting=" + observedWithinMooseHunting +
                ", observationType=" + observationType +
                '}';
    }
}
