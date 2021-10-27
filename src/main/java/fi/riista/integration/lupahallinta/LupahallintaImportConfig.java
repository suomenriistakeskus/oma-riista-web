package fi.riista.integration.lupahallinta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.URI;

@Configuration
@PropertySource("classpath:configuration/lh.properties")
public class LupahallintaImportConfig {

    @Value("${lh.username}")
    private String username;

    @Value("${lh.password}")
    private String password;

    @Value("${lh.permit.import.types}")
    private String permitTypes;

    @Value("${lh.permit.import.url}")
    private URI permitUrl;

    public String getPermitTypes() {
        return permitTypes;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public URI getPermitUri() {
        return permitUrl;
    }

}
