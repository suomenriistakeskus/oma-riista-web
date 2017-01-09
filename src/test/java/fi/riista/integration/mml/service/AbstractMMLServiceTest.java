package fi.riista.integration.mml.service;

import fi.riista.integration.mml.MMLProperties;
import fi.riista.integration.mml.support.MMLWebFeatureServiceRequestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;

abstract class AbstractMMLServiceTest {
    protected static MMLWebFeatureServiceRequestTemplate createRequestTemplate() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("configuration/application.properties");
        ResourcePropertySource propertySource = new ResourcePropertySource(classPathResource);
        String uri = propertySource.getProperty("wfs.mml.uri").toString();
        String username = propertySource.getProperty("wfs.mml.username").toString();
        String password = propertySource.getProperty("wfs.mml.password").toString();

        MMLProperties mmlProperties = new MMLProperties(uri, username, password);
        ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        return new MMLWebFeatureServiceRequestTemplate(mmlProperties, requestFactory);
    }
}
