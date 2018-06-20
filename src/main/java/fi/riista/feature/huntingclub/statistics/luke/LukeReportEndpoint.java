package fi.riista.feature.huntingclub.statistics.luke;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;

@Component
public class LukeReportEndpoint {
    @Value("${luke.moose.reports.url.prefix}")
    private URI lukeReportPrefix;

    @Value("${luke.moose.reports.user}")
    private String lukeMooseReportUsername;

    @Value("${luke.moose.reports.pass}")
    private String lukeMooseReportPassword;

    private UsernamePasswordCredentials credentials;

    @PostConstruct
    public void initCredentials() {
        this.credentials = new UsernamePasswordCredentials(lukeMooseReportUsername, lukeMooseReportPassword);
    }

    public URI getBaseUri() {
        return lukeReportPrefix;
    }

    public UsernamePasswordCredentials getHttpClientCredentials() {
        return credentials;
    }
}
