package fi.riista.feature.gamediary.mobile;

import javax.annotation.Resource;

public class MobileGameDiaryFeatureV1Test extends MobileGameDiaryFeatureTest {

    @Resource
    private MobileGameDiaryV1Feature feature;

    @Override
    protected MobileGameDiaryFeature feature() {
        return feature;
    }

}
