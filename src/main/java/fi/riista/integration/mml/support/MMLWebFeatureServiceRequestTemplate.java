package fi.riista.integration.mml.support;

import com.google.common.collect.Lists;
import fi.riista.config.web.BasicAuthenticationClientInterceptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
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
    private final RestTemplate restTemplate;
    private final URI endpointUri;

    public static MMLWebFeatureServiceRequestTemplate create(
            final String endpointUri, final ClientHttpRequestFactory clientHttpRequestFactory) {
        return new MMLWebFeatureServiceRequestTemplate(
                URI.create(endpointUri), null, null, clientHttpRequestFactory);
    }

    public static MMLWebFeatureServiceRequestTemplate createWithAuthentication(
            final String endpointUri, final String username, final String password,
            final ClientHttpRequestFactory clientHttpRequestFactory) {
        return new MMLWebFeatureServiceRequestTemplate(
                URI.create(endpointUri), username, password, clientHttpRequestFactory);
    }

    public MMLWebFeatureServiceRequestTemplate(
            final URI endpointUri, final String username, final String password,
            final ClientHttpRequestFactory clientHttpRequestFactory) {
        this.restTemplate = new CustomRestTemplate(clientHttpRequestFactory, username, password);
        this.endpointUri = endpointUri;
    }

    public Document makeXMLGetRequest(final Map<String, ?> uriVariables) {
        final HttpEntity<?> requestEntity = HttpEntity.EMPTY;
        final UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.endpointUri);

        uriVariables.forEach(builder::queryParam);

        final URI uri = builder.build().toUri();
        final ResponseEntity<DOMSource> responseEntity = restTemplate.exchange(
                uri, HttpMethod.GET, requestEntity, DOMSource.class);

        return documentOrNull(responseEntity);
    }

    private static Document documentOrNull(ResponseEntity<DOMSource> responseEntity) {
        final DOMSource body = responseEntity.getBody();
        return body != null && body.getNode().getNodeType() == Node.DOCUMENT_NODE ? (Document) body.getNode() : null;
    }

    private static class CustomRestTemplate extends RestTemplate {
        static final MediaType MEDIA_TYPE_ANY_XML = new MediaType("application", "*+xml");
        static final MediaType MEDIA_TYPE_OGC_XML = new MediaType("application", "vnd.ogc.se_xml");

        CustomRestTemplate(final ClientHttpRequestFactory requestFactory,
                           final String username, final String password) {
            super(requestFactory);

            final SourceHttpMessageConverter<Source> xmlConverter = new SourceHttpMessageConverter<>();
            xmlConverter.setSupportedMediaTypes(Lists.newArrayList(
                    MediaType.APPLICATION_XML, MediaType.TEXT_XML, MEDIA_TYPE_ANY_XML, MEDIA_TYPE_OGC_XML));

            final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
            messageConverters.add(xmlConverter);

            final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

            if (username != null && password != null) {
                interceptors.add(new BasicAuthenticationClientInterceptor(username, password));
            }

            setMessageConverters(messageConverters);
            setInterceptors(interceptors);
        }

        @Override
        protected <T> T doExecute(final URI url, final HttpMethod method, final RequestCallback requestCallback,
                                  final ResponseExtractor<T> responseExtractor) throws RestClientException {
            return super.doExecute(url, method, requestCallback, response -> {
                final String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

                // This ugly hack is required to parse invalid WFS content type causing
                // org.springframework.util.InvalidMimeTypeException:
                // Invalid mime type "text/xml; subtype=gml/3.2.1;charset=UTF-8":
                // Invalid token character '/' in token "gml/3.2.1"
                if (contentType != null && contentType.startsWith("text/xml; subtype=gml/")) {
                    response.getHeaders().setContentType(MediaType.valueOf("text/xml; subtype=\"gml/3.2.1\""));
                }
                return responseExtractor.extractData(response);
            });
        }
    }
}
