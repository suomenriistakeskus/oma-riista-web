package fi.riista.feature.storage.metadata;

import fi.riista.config.properties.AWSConfigProperties;

import java.nio.file.Path;

// File storage can be customized using file content type
public enum FileType {
    TEST_FOLDER(StorageType.LOCAL_FOLDER),
    TEST_DB(StorageType.LOCAL_DATABASE),
    TEST_S3(StorageType.AWS_S3_BUCKET),
    MAP_BACKUP(StorageType.AWS_S3_BUCKET),
    METSASTAJAREKISTERI(StorageType.AWS_S3_BUCKET),
    MOOSE_PERMIT_FINISHED_RECEIPT(StorageType.AWS_S3_BUCKET),
    IMAGE_UPLOAD(StorageType.AWS_S3_BUCKET),
    MOOSE_DATA_CARD(StorageType.AWS_S3_BUCKET);

    private final StorageType storageType;

    FileType(final StorageType storageType) {
        this.storageType = storageType;
    }

    public StorageType storageType() {
        return storageType;
    }

    public String formatFilename(final PersistentFileMetadata metadata) {
        if (this == MOOSE_DATA_CARD) {
            return metadata.getOriginalFilename() + "_" + metadata.getCreationTime().getTime();
        }

        // If there are concurrent transactions saving same file, then first transactions will commit, and all
        // other transactions will rollback and delete the file they saved.
        // If all transactions save their file with same filename,
        // then the successful transaction will refer to a deleted file.
        // To create separate file for each transaction append timestamp to filename.
        return metadata.getId().toString() + "_" + System.currentTimeMillis();
    }

    public Path resolveLocalStorageFolder(final Path storageFolder) {
        return this == FileType.MAP_BACKUP ? storageFolder.resolve(this.name()) : storageFolder;
    }

    public String resolveAwsBucketName(final AWSConfigProperties awsConfigProperties) {
        return this == FileType.IMAGE_UPLOAD
                ? awsConfigProperties.getBucketDiaryImages()
                : awsConfigProperties.getDefaultBucket();
    }

    public String resolveAwsBucketKey(final PersistentFileMetadata metadata) {
        if (this == METSASTAJAREKISTERI) {
            return "metsastajarekisteri/" + metadata.getOriginalFilename();
        }

        if (this == MAP_BACKUP) {
            return "map/backup/" + metadata.getOriginalFilename();
        }

        if (this == MOOSE_PERMIT_FINISHED_RECEIPT) {
            return "moosepermitreceipt/" + formatFilename(metadata);
        }

        if (this == MOOSE_DATA_CARD) {
            return "moosedatacard/" + formatFilename(metadata);
        }

        return formatFilename(metadata);
    }
}
