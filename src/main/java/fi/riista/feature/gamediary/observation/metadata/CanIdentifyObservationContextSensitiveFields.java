package fi.riista.feature.gamediary.observation.metadata;

import fi.riista.feature.gamediary.observation.ObservationType;

public interface CanIdentifyObservationContextSensitiveFields {

    boolean observedWithinMooseHunting();

    ObservationType getObservationType();

}
