package fi.riista.feature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.URI;

import static org.springframework.util.StringUtils.hasText;

@Component
public class RuntimeEnvironmentUtil {
    private static final long JVM_STARTUP_TIMESTAMP = System.currentTimeMillis();

    @Value("${environment.id}")
    private String environmentId;

    @Value("${git.commit.id.abbrev:}")
    public String gitCommitIdAbbrev;

    @Value("${server.url}")
    private URI serverUri;

    @Value("${map.export.endpoint}")
    private URI mapExportEndpoint;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${file.storage.folder}")
    private String fileStorageFolder;

    @Nonnull
    public String getRevision() {
        // Always force cache bust using changing timestamp
        if (isDevelopmentEnvironment()) {
            return "" + System.currentTimeMillis();
        }

        return hasText(gitCommitIdAbbrev) ? gitCommitIdAbbrev : Long.toHexString(JVM_STARTUP_TIMESTAMP);
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public boolean isDevelopmentEnvironment() {
        return "dev".equalsIgnoreCase(getEnvironmentId());
    }

    public boolean isStagingEnvironment() {
        return "staging".equals(getEnvironmentId());
    }

    public boolean isProductionEnvironment() {
        return "prod".equalsIgnoreCase(getEnvironmentId());
    }

    public boolean isIntegrationTestEnvironment() {
        return "e2e-test".equals(getEnvironmentId());
    }

    public URI getBackendBaseUri() {
        return serverUri;
    }

    public String getFileStorageBasePath() {
        return fileStorageFolder;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public URI getMapExportEndpoint() {
        return mapExportEndpoint;
    }
}
