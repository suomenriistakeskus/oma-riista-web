package fi.riista.integration.paytrail.rest.client;

import com.google.common.collect.Lists;
import fi.riista.config.web.BasicAuthenticationClientInterceptor;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import fi.riista.integration.paytrail.rest.model.ErrorMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaytrailRestTemplate extends RestTemplate {

    public PaytrailRestTemplate(final ClientHttpRequestFactory requestFactory,
                                final Jaxb2Marshaller xmlMarshaller,
                                final PaytrailCredentials credentials) {
        super(requestFactory);

        final MarshallingHttpMessageConverter xmlConverter = createXmlMessageConverter(xmlMarshaller);
        final ErrorHandler errorHandler = new ErrorHandler(xmlConverter);
        final PaytrailApiVersionHeaderInterceptor apiVersionHeaderInterceptor = new PaytrailApiVersionHeaderInterceptor();
        final BasicAuthenticationClientInterceptor authInterceptor = createAuthInterceptor(credentials);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(xmlConverter);

        final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(apiVersionHeaderInterceptor);

        if (authInterceptor != null) {
            interceptors.add(authInterceptor);
        }

        setMessageConverters(messageConverters);
        setInterceptors(interceptors);
        setErrorHandler(errorHandler);
    }

    private static BasicAuthenticationClientInterceptor createAuthInterceptor(final PaytrailCredentials credentials) {
        return credentials != null ? new BasicAuthenticationClientInterceptor(
                credentials.getMerchantId(), credentials.getMerchantSecret()) : null;
    }

    private static MarshallingHttpMessageConverter createXmlMessageConverter(final Jaxb2Marshaller marshaller) {
        final MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter(marshaller);
        converter.setSupportedMediaTypes(Lists.newArrayList(MediaType.APPLICATION_XML));

        return converter;
    }

    private static class ErrorHandler extends DefaultResponseErrorHandler {
        private final MarshallingHttpMessageConverter xmlConverter;

        ErrorHandler(final MarshallingHttpMessageConverter xmlConverter) {
            this.xmlConverter = xmlConverter;
        }

        @Override
        public void handleError(final ClientHttpResponse response) throws IOException {
            if (xmlConverter.canRead(ErrorMessage.class, response.getHeaders().getContentType())) {
                final ErrorMessage msg = (ErrorMessage) xmlConverter.read(ErrorMessage.class, response);

                if (msg != null) {
                    throw new PaytrailClientException(msg);
                }
            }

            super.handleError(response);
        }
    }
}
