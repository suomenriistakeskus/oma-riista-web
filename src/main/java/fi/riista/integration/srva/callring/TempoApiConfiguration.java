package fi.riista.integration.srva.callring;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nsftele.tempo.api.CallRingApi;
import com.nsftele.tempo.api.CallTrackingApi;
import com.nsftele.tempo.api.LanguageApi;
import com.nsftele.tempo.auth.HttpBasicAuth;
import feign.Feign;
import feign.FeignException;
import feign.RetryableException;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TempoApiConfiguration {
    @Value("${srva.tempo.api.uri}")
    private String baseUri;

    @Value("${srva.tempo.api.username:}")
    private String username;

    @Value("${srva.tempo.api.password:}")
    private String password;

    @Resource
    private HttpClient httpClient;

    @Bean
    public CallRingApi callRingApi() {
        return tempoApiBuilder().target(CallRingApi.class, baseUri);
    }

    @Bean
    public CallTrackingApi callTrackingApi() {
        return tempoApiBuilder().target(CallTrackingApi.class, baseUri);
    }

    @Bean
    public LanguageApi languageApi() {
        return tempoApiBuilder().target(LanguageApi.class, baseUri);
    }

    @Bean
    public Feign.Builder tempoApiBuilder() {
        final ObjectMapper objectMapper = createObjectMapper();

        final JacksonDecoder decoder = new JacksonDecoder(objectMapper);
        final JacksonEncoder encoder = new JacksonEncoder(objectMapper);

        return Feign.builder()
                .requestInterceptor(tempoApiAuthentication())
                .errorDecoder(new TempoApiErrorDecoder(decoder))
                .client(new ApacheHttpClient(httpClient))
                .encoder(encoder)
                .decoder(decoder)
                .logger(new Slf4jLogger());
    }

    private HttpBasicAuth tempoApiAuthentication() {
        final HttpBasicAuth authorization = new HttpBasicAuth();
        authorization.setCredentials(username, password);
        return authorization;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    @Qualifier("tempo")
    public RetryOperations tempoApiRetryPolicy() {
        final RetryPolicy retryPolicy = createRetryPolicy();

        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(3);

        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);

        return retryTemplate;
    }

    private static RetryPolicy createRetryPolicy() {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(5);

        final Map<Class<? extends Throwable>, RetryPolicy> policyMap = new HashMap<>();
        policyMap.put(TempoApiException.class, new NeverRetryPolicy());
        policyMap.put(FeignException.class, new NeverRetryPolicy());
        policyMap.put(IOException.class, simpleRetryPolicy);
        policyMap.put(RetryableException.class, simpleRetryPolicy);

        final ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setPolicyMap(policyMap);

        return simpleRetryPolicy;
    }
}
