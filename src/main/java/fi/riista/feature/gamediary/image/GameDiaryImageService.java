package fi.riista.feature.gamediary.image;

import fi.riista.feature.common.ImageResizer;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameDiaryEntry;
import fi.riista.feature.gamediary.GameDiaryEntry_;
import fi.riista.feature.gamediary.GameDiarySpecs;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.Observation_;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEvent_;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.PersistentFileMetadata_;
import fi.riista.util.ContentDispositionUtil;
import fi.riista.util.F;
import fi.riista.util.Filters;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static fi.riista.util.jpa.JpaSpecs.hasRelationWithId;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class GameDiaryImageService {

    private static final int CACHE_HEADER_MAX_AGE = Days.days(365).toStandardSeconds().getSeconds();

    private static final Logger LOG = LoggerFactory.getLogger(GameDiaryImageService.class);

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepository;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ImageResizer imageResizer;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<GameDiaryImage> getImages(@Nonnull final GameDiaryEntry diaryEntry) {
        Objects.requireNonNull(diaryEntry);

        return diaryEntry.getType().apply(
                diaryEntry, gameDiaryImageRepository::findByHarvest, gameDiaryImageRepository::findByObservation);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<UUID> getImageIds(@Nonnull final GameDiaryEntry diaryEntry) {
        return GameDiaryImage.getImageIds(getImages(diaryEntry));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getGameDiaryImageBytes(final UUID imageUuid,
                                                         final boolean disposition) throws IOException {

        final Optional<PersistentFileMetadata> optionalMetadata = findImageMetadata(imageUuid);

        if (!optionalMetadata.isPresent()) {
            return imageNotFoundResponse();
        }

        final PersistentFileMetadata metadata = optionalMetadata.get();

        final String filename = Optional.ofNullable(metadata.getOriginalFilename()).orElse("file");
        final HttpHeaders attachmentDispositionHeader = disposition ?
                ContentDispositionUtil.header(filename)
                : new HttpHeaders();

        return ResponseEntity.ok()
                .headers(attachmentDispositionHeader)
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                // Do not set content-length header, because gameDiaryImages have been resized on disk but database is not updated,
                // resulting content-length to be greater than actual file size.
                // .contentLength(metadata.getContentSize());
                .body(fileStorageService.getBytes(imageUuid));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ResponseEntity<byte[]> getGameDiaryImageBytesResized(final UUID imageUuid,
                                                                final int width,
                                                                final int height,
                                                                final boolean keepProportions) throws IOException {

        final Optional<PersistentFileMetadata> optionalMetadata = findImageMetadata(imageUuid);

        if (!optionalMetadata.isPresent()) {
            return imageNotFoundResponse();
        }

        final byte[] photoData = fileStorageService.getBytes(imageUuid);
        final byte[] thumbnailData = imageResizer.resize(photoData, width, height, keepProportions);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(thumbnailData.length)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + CACHE_HEADER_MAX_AGE)
                .body(thumbnailData);
    }

    private Optional<PersistentFileMetadata> findImageMetadata(final UUID imageUuid) {
        return fileStorageService.getMetadata(imageUuid)
                .filter(metadata -> {
                    final String contentType = metadata.getContentType().toLowerCase();

                    return contentType.startsWith("image") || contentType.startsWith("jpeg");
                });
    }

    private static ResponseEntity<byte[]> imageNotFoundResponse() {
        return ResponseEntity.notFound()
                .lastModified(System.currentTimeMillis())
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate")
                .build();
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDiaryImage getGameDiaryImageForAuthor(final UUID imageId, final Person author) {
        final Specification<GameDiaryImage> spec =
                where(joinFileMetadata(imageId))
                        .and(GameDiarySpecs.equalValueWithHarvestOrObservation(
                                GameDiaryImage_.harvest, GameDiaryImage_.observation, GameDiaryEntry_.author, author));

        return findOne(spec).orElseThrow(() -> new NotFoundException(String.format(
                "Game diary image not found, UUID: %s, authorId: %d", imageId, author.getId())));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDiaryImage getSrvaEventImageForAuthor(final UUID imageId, final Person author) {
        final Specification<GameDiaryImage> spec =
                where(joinFileMetadata(imageId)).and(equal(GameDiaryImage_.srvaEvent, SrvaEvent_.author, author));

        return findOne(spec).orElseThrow(() -> new NotFoundException(String.format(
                "SRVA event image not found, UUID: %s, authorId: %d", imageId, author.getId())));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public GameDiaryImage getGameDiaryImageForDiaryEntry(final UUID imageId, final GameDiaryEntry diaryEntry) {
        final Specification<GameDiaryImage> joinDiaryEntry = diaryEntry.getType().supply(
                () -> hasRelationWithId(GameDiaryImage_.harvest, Harvest_.id, diaryEntry.getId()),
                () -> hasRelationWithId(GameDiaryImage_.observation, Observation_.id, diaryEntry.getId()));

        return findOne(where(joinDiaryEntry).and(joinFileMetadata(imageId)))
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Game diary image not found, UUID: %s, diaryEntryId: %d", imageId, diaryEntry.getId())));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateImages(final GameDiaryEntry diaryEntry, final List<UUID> imageIds) {
        final List<GameDiaryImage> existingImages = getImages(diaryEntry);
        final Set<UUID> existingImageIds = GameDiaryImage.getUniqueImageIds(existingImages);

        // Remove images and associations with GameDiaryEntry for images not found in list of UUIDs.
        existingImages.stream()
                .filter(img -> !imageIds.contains(img.getFileMetadata().getId()))
                .forEach(this::deleteGameDiaryImage);

        // Add images which are new (GameDiaryEntry isn't already associated with them).
        imageIds.stream()
                .filter(Filters.notIn(existingImageIds))
                .forEach(uuid -> associateGameDiaryEntryWithImage(diaryEntry, uuid));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean updateImages(@Nonnull final SrvaEvent entity, final List<UUID> imageIds) {
        final List<GameDiaryImage> existingImages = gameDiaryImageRepository.findBySrvaEvent(entity);
        final Set<UUID> existingImageIds = GameDiaryImage.getUniqueImageIds(existingImages);

        // Remove images and associations not found in list of UUIDs.
        final List<GameDiaryImage> imagesToBeRemoved = existingImages.stream()
                .filter(img -> !imageIds.contains(img.getFileMetadata().getId()))
                .collect(toList());
        imagesToBeRemoved.forEach(this::deleteGameDiaryImage);

        // Associate new images
        final List<UUID> newImageIds = F.filterToList(imageIds, Filters.notIn(existingImageIds));
        newImageIds.forEach(uuid -> associateSrvaEventWithImage(entity, uuid));

        return !imagesToBeRemoved.isEmpty() || !newImageIds.isEmpty();
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addGameDiaryImage(final GameDiaryEntry diaryEntry, final UUID imageId, final MultipartFile file)
            throws IOException {

        if (checkImageWithIdDoesNotAlreadyExist(imageId)) {
            final PersistentFileMetadata metadata = storeImageFile(imageId, file);

            final GameDiaryImage img = diaryEntry.getType().apply(
                    diaryEntry,
                    harvest -> new GameDiaryImage(harvest, metadata),
                    observation -> new GameDiaryImage(observation, metadata));
            gameDiaryImageRepository.save(img);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addGameDiaryImageWithoutDiaryEntryAssociation(final UUID imageId,
                                                              final MultipartFile file) throws IOException {

        gameDiaryImageRepository.save(new GameDiaryImage(storeImageFile(imageId, file)));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends GameDiaryEntry> void associateGameDiaryEntryWithImage(final T diaryEntry, final UUID uuid) {
        final GameDiaryImage image = findGameDiaryImageByUuid(uuid);

        diaryEntry.getType().consume(diaryEntry, image::setHarvest, image::setObservation);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void associateSrvaEventWithImage(final SrvaEvent srvaEvent, final UUID uuid) {
        findGameDiaryImageByUuid(uuid).setSrvaEvent(srvaEvent);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDiaryImage(final UUID imageUuid, final Person author) {
        deleteGameDiaryImage(getGameDiaryImageForAuthor(imageUuid, author));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDiaryImage(@Nonnull final GameDiaryImage image) {
        Objects.requireNonNull(image, "image must not be null");

        image.setHarvest(null);
        image.setObservation(null);
        image.setSrvaEvent(null);

        final UUID imageUuid = image.getFileMetadata().getId();
        gameDiaryImageRepository.delete(image);
        fileStorageService.remove(imageUuid);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDiaryImages(@Nonnull final Harvest harvest) {
        gameDiaryImageRepository.findByHarvest(harvest).forEach(this::deleteGameDiaryImage);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDiaryImages(@Nonnull final Observation observation) {
        gameDiaryImageRepository.findByObservation(observation).forEach(this::deleteGameDiaryImage);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDiaryImages(@Nonnull final SrvaEvent srvaEvent) {
        gameDiaryImageRepository.findBySrvaEvent(srvaEvent).forEach(this::deleteGameDiaryImage);
    }

    private static Specification<GameDiaryImage> joinFileMetadata(final UUID imageId) {
        return hasRelationWithId(GameDiaryImage_.fileMetadata, PersistentFileMetadata_.id, imageId);
    }

    private PersistentFileMetadata storeImageFile(final UUID imageId, final MultipartFile file) throws IOException {
        final byte[] resized = imageResizer.toJpgDownscaleToSize(file.getInputStream());

        return fileStorageService.storeFile(
                imageId,
                resized,
                FileType.IMAGE_UPLOAD,
                file.getContentType(),
                file.getOriginalFilename());
    }

    private GameDiaryImage findGameDiaryImageByUuid(final UUID uuid) {
        return gameDiaryImageRepository.findOne(joinFileMetadata(uuid));
    }

    private Optional<GameDiaryImage> findOne(final Specification<GameDiaryImage> spec) {
        return Optional.ofNullable(gameDiaryImageRepository.findOne(spec));
    }

    private boolean checkImageWithIdDoesNotAlreadyExist(final UUID imageId) {
        if (fileStorageService.exists(imageId)) {
            LOG.debug("Game diary image already exists, UUID: " + imageId);
            return false;
        }
        return true;
    }

}
