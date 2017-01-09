package fi.riista.feature.gamediary.mobile;

import javax.annotation.Resource;

public class MobileGameDiaryFeatureV2Test extends MobileGameDiaryFeatureTest {

    @Resource
    private MobileGameDiaryV2Feature feature;

    @Override
    protected MobileGameDiaryFeature feature() {
        return feature;
    }

}
