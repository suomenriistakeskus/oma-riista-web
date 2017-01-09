package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.observation.ObservationSpecVersion;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MobileObservationFeatureV2Test extends MobileObservationFeatureTest {

    @Resource
    private MobileGameDiaryV2Feature feature;

    @Override
    protected MobileGameDiaryFeature feature() {
        return feature;
    }

    @Override
    public List<ObservationSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(ObservationSpecVersion.class));
    }

}
