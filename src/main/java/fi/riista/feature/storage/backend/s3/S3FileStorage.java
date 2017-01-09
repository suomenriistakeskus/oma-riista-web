package fi.riista.feature.storage.backend.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.Base64;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import fi.riista.config.properties.AWSConfigProperties;
import fi.riista.feature.storage.backend.FileStorageSpi;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@Component
public class S3FileStorage implements FileStorageSpi {
    private static final Logger LOG = LoggerFactory.getLogger(S3FileStorage.class);

    @Resource
    private AWSConfigProperties awsConfigProperties;

    @Resource
    private AmazonS3 amazonS3;

    @Resource
    private TransferManager transferManager;

    @Override
    public StorageType getType() {
        return StorageType.AWS_S3_BUCKET;
    }

    public S3FileStorage() {
        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
    }

    @PreDestroy
    public void stopTransferManager() {
        this.transferManager.shutdownNow();
    }

    @Override
    public void storeFile(final FileType fileType,
                          final PersistentFileMetadata metadata,
                          final InputStream inputStream) throws IOException {
        final String bucketName = fileType.resolveAwsBucketName(awsConfigProperties);
        final String objectKey = fileType.resolveAwsBucketKey(metadata);

        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("Bucket name is not configured for fileType=" + fileType);
        }

        // Store S3 bucket and key embedded as resource URL
        metadata.setResourceUrl(S3Util.createResourceURL(bucketName, objectKey));

        final ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(Objects.requireNonNull(metadata.getContentSize()));
        objectMetadata.setContentType(Objects.requireNonNull(metadata.getContentType()));

        if (metadata.getMd5Hash() != null) {
            // Use MD5 to verify uploaded file
            objectMetadata.setContentMD5(Base64.encodeAsString(metadata.getMd5Hash().asBytes()));
        }

        try {
            final PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream, objectMetadata);
            request.setCannedAcl(CannedAccessControlList.Private);

            final Upload upload = this.transferManager.upload(request);
            upload.waitForUploadResult();

        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(final int status) {
                // Remove file if transaction is rolled back
                if (status == STATUS_ROLLED_BACK) {
                    removeInternal(new S3Util.BucketObjectPair(bucketName, objectKey));
                }
            }
        });
    }

    @Override
    public void retrieveFile(final PersistentFileMetadata metadata,
                             final OutputStream outputStream) {
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(metadata.getResourceUrl());

        try {
            final GetObjectRequest getObjectRequest = new GetObjectRequest(s3Object.getBucketName(), s3Object.getKey());

            try (final InputStream is = this.amazonS3.getObject(getObjectRequest).getObjectContent()) {
                ByteStreams.copy(is, outputStream);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void removeFromStorage(final PersistentFileMetadata metadata) {
        // Invoked before session closed
        final S3Util.BucketObjectPair s3Object = S3Util.parseResourceURL(metadata.getResourceUrl());

        // Delay S3 object delete until transaction is complete and successful
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                removeInternal(s3Object);
            }
        });
    }

    private void removeInternal(final S3Util.BucketObjectPair s3Object) {
        try {
            amazonS3.deleteObject(s3Object.getBucketName(), s3Object.getKey());

        } catch (AmazonClientException ex) {
            LOG.warn("Could not delete S3 resource", ex);
        }
    }

    @Override
    public boolean isConfigured() {
        return awsConfigProperties.isConfigured();
    }
}
