package fi.riista.feature.storage.metadata;

import fi.riista.config.properties.AWSConfigProperties;

// File storage can be customized using file content type
public enum FileType {

    TEST_FOLDER(StorageType.LOCAL_FOLDER),
    TEST_DB(StorageType.LOCAL_DATABASE),
    TEST_S3(StorageType.AWS_S3_BUCKET),
    METSASTAJAREKISTERI(StorageType.AWS_S3_BUCKET),
    MOOSE_PERMIT_FINISHED_RECEIPT(StorageType.AWS_S3_BUCKET),
    PERMIT_APPLICATION_ARCHIVE(StorageType.AWS_S3_BUCKET),
    PERMIT_APPLICATION_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    DECISION_PDF(StorageType.AWS_S3_BUCKET),
    DECISION_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    DECISION_ACTION_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    INVOICE_PDF(StorageType.AWS_S3_BUCKET),
    FIVALDI_INVOICE_BATCH(StorageType.AWS_S3_BUCKET),
    IMAGE_UPLOAD(StorageType.AWS_S3_BUCKET),
    MOOSE_DATA_CARD(StorageType.AWS_S3_BUCKET),
    SHOOTING_TEST_EXPORT(StorageType.AWS_S3_BUCKET),
    HUNTING_CONTROL_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    OTHERWISE_DECEASED_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    TAXATION_REPORT_ATTACHMENT(StorageType.AWS_S3_BUCKET),
    DEER_CENSUS_ATTACHMENT(StorageType.AWS_S3_BUCKET),;

    private final StorageType storageType;

    FileType(final StorageType storageType) {
        this.storageType = storageType;
    }

    public StorageType storageType() {
        return storageType;
    }

    public String formatFilename(final PersistentFileMetadata metadata) {
        switch (this) {
            case MOOSE_DATA_CARD:
                return metadata.getOriginalFilename() + "_" + metadata.getCreationTime().getMillis();

            default:
                // If there are concurrent transactions saving same file, then first transactions will commit, and all
                // other transactions will rollback and delete the file they saved.
                // If all transactions save their file with same filename,
                // then the successful transaction will refer to a deleted file.
                // To create separate file for each transaction append timestamp to filename.
                return metadata.getId().toString() + "_" + System.currentTimeMillis();
        }
    }

    public String resolveAwsBucketName(final AWSConfigProperties awsConfigProperties) {
        return this == FileType.IMAGE_UPLOAD
                ? awsConfigProperties.getBucketDiaryImages()
                : awsConfigProperties.getDefaultBucket();
    }

    public String resolveAwsBucketKey(final PersistentFileMetadata metadata) {
        switch (this) {
            case METSASTAJAREKISTERI:
                return "metsastajarekisteri/" + metadata.getOriginalFilename();

            case SHOOTING_TEST_EXPORT:
                return "shootingtestexport/" + metadata.getOriginalFilename();

            case MOOSE_PERMIT_FINISHED_RECEIPT:
                return "moosepermitreceipt/" + formatFilename(metadata);

            case MOOSE_DATA_CARD:
                return "moosedatacard/" + formatFilename(metadata);

            case PERMIT_APPLICATION_ARCHIVE:
                return "permitapplication/" + formatFilename(metadata);

            case PERMIT_APPLICATION_ATTACHMENT:
                return "permitapplicationattachment/" + formatFilename(metadata);

            case DECISION_PDF:
                return "permitdecision/" + formatFilename(metadata);

            case DECISION_ATTACHMENT:
                return "permitdecisionattachment/" + formatFilename(metadata);

            case DECISION_ACTION_ATTACHMENT:
                return "permitdecisionactionattachment/" + formatFilename(metadata);

            case INVOICE_PDF:
                return "invoice/" + formatFilename(metadata);

            case FIVALDI_INVOICE_BATCH:
                return "fivaldibatch/" + formatFilename(metadata);

            case HUNTING_CONTROL_ATTACHMENT:
                return "huntingcontrolattachment/" + formatFilename(metadata);

            case OTHERWISE_DECEASED_ATTACHMENT:
                return "otherwisedeceasedattachment/" + formatFilename(metadata);

            case TAXATION_REPORT_ATTACHMENT:
                return "taxationreportattachment/" + formatFilename(metadata);

            case DEER_CENSUS_ATTACHMENT:
                return "deercensusattachment/" + formatFilename(metadata);

            default:
                return formatFilename(metadata);
        }
    }
}
