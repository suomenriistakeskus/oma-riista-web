package fi.riista.integration.vtj;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;

@Configuration
@PropertySource("classpath:configuration/vtj.properties")
public class VtjConfig {

    private static final String TRUSTSTORE_PATH = "certificate/truststore.jks";

    // Trust-store contains only public certificates
    // and requires password only for integrity checking.
    private static final String TRUSTORE_PASSWORD = "changeit";

    @Value("${integration.vtj.endpoint}")
    private String apiEndpoint;

    @Value("${integration.vtj.username}")
    private String apiUsername;

    @Value("${integration.vtj.password}")
    private String apiPassword;

    @Value("${integration.vtj.x509:false}")
    private boolean clientCertificateEnabled;

    @Value("${integration.vtj.keystore:}")
    private String keyStoreLocation;

    @Value("${integration.vtj.keystore.password:}")
    private String keyStorePassword;

    public String getEndpointAddress() {
        return apiEndpoint;
    }

    public String getUsername() {
        return apiUsername;
    }

    public String getPassword() {
        return apiPassword;
    }

    // Only production API requires use of X509 client certificate
    public boolean useClientX509Certificate() {
        return clientCertificateEnabled;
    }

    public AbstractResource getTrustStoreResource() {
        return new ClassPathResource(TRUSTSTORE_PATH);
    }

    public char[] getTrustStorePassword() {
        return TRUSTORE_PASSWORD.toCharArray();
    }

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword.toCharArray();
    }
}
