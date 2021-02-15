package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class MobileGameDiaryFeature {

    private static final Logger LOG = LoggerFactory.getLogger(MobileGameDiaryFeature.class);

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public void deleteGameDiaryImage(final UUID imageUuid) {
        try {
            gameDiaryImageService.deleteGameDiaryImage(imageUuid, activeUserService.requireActivePerson());
        } catch (final NotFoundException nfe) {
            LOG.info("deleteGameDiaryImage failed, image not found uuid:" + imageUuid);
            // If image is not found there is nothing that mobile client can do so let's not report this
        }
    }
}
