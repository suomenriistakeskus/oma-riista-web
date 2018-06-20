package fi.riista.integration.mml;

import fi.riista.integration.mml.support.MMLWebFeatureServiceRequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestFactory;

import javax.annotation.Resource;

@Configuration
@PropertySource("classpath:configuration/mml.properties")
public class MMLConfig {

    @Resource
    private ClientHttpRequestFactory requestFactory;

    @Value("${wfs.mml.uri}")
    private String uri;

    @Value("${wfs.mml.username}")
    private String username;

    @Value("${wfs.mml.password}")
    private String password;

    @Value("${wfs.building.uri}")
    private String buildingUri;

    @Bean("propertyWFS")
    public MMLWebFeatureServiceRequestTemplate requestTemplateForProperties() {
        return MMLWebFeatureServiceRequestTemplate.createWithAuthentication(uri, username, password, requestFactory);
    }

    @Bean("buildingWFS")
    public MMLWebFeatureServiceRequestTemplate requestTemplateForBuildings() {
        return MMLWebFeatureServiceRequestTemplate.create(buildingUri, requestFactory);
    }
}
