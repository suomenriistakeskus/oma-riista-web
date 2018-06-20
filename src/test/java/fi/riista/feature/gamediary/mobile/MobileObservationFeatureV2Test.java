package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.observation.ObservationSpecVersion;

import java.util.Arrays;
import java.util.List;

public class MobileObservationFeatureV2Test extends MobileObservationFeatureTest {

    @Override
    public List<ObservationSpecVersion> getTestExecutionVersions() {
        return Arrays.asList(ObservationSpecVersion.values());
    }
}
