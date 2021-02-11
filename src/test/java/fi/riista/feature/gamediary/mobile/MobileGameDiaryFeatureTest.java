package fi.riista.feature.gamediary.mobile;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.UUID;

public class MobileGameDiaryFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileGameDiaryFeature feature;

    @Test
    public void testDeleteImage_notFound() {
        persistAndAuthenticateWithNewUser(true);
        feature.deleteGameDiaryImage(UUID.randomUUID());
    }

}
