package fi.riista.integration.mml.support;

import com.google.common.collect.Lists;
import fi.riista.config.web.BasicAuthenticationClientInterceptor;
import fi.riista.integration.mml.MMLProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MMLWebFeatureServiceRequestTemplate {

    private final MMLProperties mmlProperties;
    private final RestTemplate restTemplate;

    public MMLWebFeatureServiceRequestTemplate(MMLProperties mmlProperties, ClientHttpRequestFactory clientHttpRequestFactory) {
        this.mmlProperties = mmlProperties;
        this.restTemplate = new RestTemplate(clientHttpRequestFactory);

        final List<MediaType> xmlMediaTypes = Lists.newArrayList(
                MediaType.APPLICATION_XML, MediaType.TEXT_XML,
                new MediaType("application", "*+xml"),
                new MediaType("application", "vnd.ogc.se_xml"));

        final SourceHttpMessageConverter<Source> xmlConverter = new SourceHttpMessageConverter<>();
        xmlConverter.setSupportedMediaTypes(xmlMediaTypes);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(xmlConverter);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new BasicAuthenticationClientInterceptor(
                mmlProperties.getWfsUsername(), mmlProperties.getWfsPassword()));

        this.restTemplate.setMessageConverters(messageConverters);
        this.restTemplate.setInterceptors(interceptors);
    }

    public Document makeXMLGetRequest(final Map<String, ?> uriVariables) {
        final HttpEntity<?> requestEntity = HttpEntity.EMPTY;
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mmlProperties.getEndpointUrl());

        uriVariables.forEach(builder::queryParam);

        final URI uri = builder.build().toUri();
        final ResponseEntity<DOMSource> responseEntity = restTemplate.exchange(
                uri, HttpMethod.GET, requestEntity, DOMSource.class);

        return documentOrNull(responseEntity);
    }

    private static Document documentOrNull(ResponseEntity<DOMSource> responseEntity) {
        if (responseEntity.getBody().getNode().getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) responseEntity.getBody().getNode();
        }
        return null;
    }

}
