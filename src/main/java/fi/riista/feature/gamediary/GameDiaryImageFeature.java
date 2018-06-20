package fi.riista.feature.gamediary;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.image.GameDiaryImageService;
import fi.riista.security.EntityPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

@Service
public class GameDiaryImageFeature {

    @Resource
    private GameDiaryImageService gameDiaryImageService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<?> getGameDiaryImageBytes(final UUID imageUuid,
                                                    final boolean disposition) throws IOException {

        // No authorization by intention

        return gameDiaryImageService.getGameDiaryImageBytes(imageUuid, disposition);
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<?> getGameDiaryImageBytesResized(
            final UUID imageUuid, final int width, final int height, final boolean keepProportions)
            throws IOException {

        // No authorization by intention

        return gameDiaryImageService.getGameDiaryImageBytesResized(imageUuid, width, height, keepProportions);
    }

    @Transactional(rollbackFor = IOException.class)
    public void addGameDiaryImageWithoutDiaryEntryAssociation(final UUID imageId,
                                                              final MultipartFile file) throws IOException {

        // Authorize
        activeUserService.requireActivePerson();

        gameDiaryImageService.addGameDiaryImageWithoutDiaryEntryAssociation(imageId, file);
    }

    @Transactional(rollbackFor = IOException.class)
    public void addGameDiaryImageForDiaryEntry(final long diaryEntryId,
                                               final GameDiaryEntryType diaryEntryType,
                                               final UUID imageId,
                                               final MultipartFile file) throws IOException {

        // Authorize person
        activeUserService.requireActivePerson();

        final GameDiaryEntry diaryEntry = diaryEntryType.supply(
                () -> requireEntityService.requireHarvest(diaryEntryId, EntityPermission.UPDATE),
                () -> requireEntityService.requireObservation(diaryEntryId, EntityPermission.UPDATE));

        gameDiaryImageService.addGameDiaryImage(diaryEntry, imageId, file);
    }

    @Transactional(rollbackFor = IOException.class)
    public void replaceImageForDiaryEntry(final long diaryEntryId,
                                          final GameDiaryEntryType diaryEntryType,
                                          final UUID replacedUuid,
                                          final UUID uuid,
                                          final MultipartFile file) throws IOException {

        final GameDiaryEntry diaryEntry = diaryEntryType.supply(
                () -> requireEntityService.requireHarvest(diaryEntryId, EntityPermission.UPDATE),
                () -> requireEntityService.requireObservation(diaryEntryId, EntityPermission.UPDATE));

        // Remove image which will be replaced.

        final GameDiaryImage toBeReplaced =
                gameDiaryImageService.getGameDiaryImageForDiaryEntry(replacedUuid, diaryEntry);

        gameDiaryImageService.deleteGameDiaryImage(toBeReplaced);

        // Add new image
        gameDiaryImageService.addGameDiaryImage(diaryEntry, uuid, file);
    }
}
