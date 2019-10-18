package fi.riista.cli;

import fi.riista.config.Constants;
import fi.riista.config.HttpClientConfig;
import fi.riista.config.JaxbConfig;
import fi.riista.integration.paytrail.PaytrailConfig;
import fi.riista.integration.paytrail.auth.PaytrailCredentials;
import fi.riista.integration.paytrail.rest.PaytrailRestAdapter;
import fi.riista.integration.paytrail.rest.client.PaytrailRestTemplate;
import fi.riista.integration.paytrail.rest.model.CreatePaymentRequest;
import fi.riista.integration.paytrail.rest.model.CreatePaymentResponse;
import fi.riista.integration.paytrail.rest.model.UrlSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.math.BigDecimal;
import java.net.URI;

public class PaytrailCli {
    private static final Logger LOG = LoggerFactory.getLogger(TempoApiCli.class);

    @ComponentScan(basePackageClasses = PaytrailConfig.class)
    @Import({HttpClientConfig.class, JaxbConfig.class})
    static class Context {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public PaytrailRestAdapter paytrailApi(final @Qualifier("paytrailMarshaller") Jaxb2Marshaller marshaller,
                                               final @Qualifier("rk") PaytrailCredentials rkCredentials,
                                               final ClientHttpRequestFactory clientHttpRequestFactory) {
            return new PaytrailRestAdapter(new PaytrailRestTemplate(
                    clientHttpRequestFactory, marshaller, rkCredentials));
        }
    }

    public static void main(final String[] cmdArgs) {
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().addActiveProfile(Constants.STANDARD_DATABASE);
            ctx.register(PaytrailCli.Context.class);
            ctx.refresh();
            ctx.start();

            try {
                final PaytrailRestAdapter api = ctx.getBean(PaytrailRestAdapter.class);
                final CreatePaymentRequest request = new CreatePaymentRequest();
                request.setOrderNumber("12345678");
                request.setCurrency("EUR");
                request.setLocale("fi_FI");
                request.setPrice(new BigDecimal("99"));

                final UrlSet urlSet = new UrlSet();
                request.setUrlSet(urlSet);
                urlSet.setSuccess(URI.create("https://oma.riista.fi/paytrail/success"));
                urlSet.setFailure(URI.create("https://oma.riista.fi/paytrail/cancel"));
                urlSet.setNotification(URI.create("https://oma.riista.fi/paytrail/notify"));

                final CreatePaymentResponse payment = api.createPayment(request);

                LOG.info("orderNumber: {}", payment.getOrderNumber());
                LOG.info("token: {}", payment.getToken());
                LOG.info("uri: {}", payment.getUrl());

            } catch (Exception e) {
                LOG.error("Job execution has failed with error", e);
            }
        }
    }

}
