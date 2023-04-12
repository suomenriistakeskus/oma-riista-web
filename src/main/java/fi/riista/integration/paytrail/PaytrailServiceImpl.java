package fi.riista.integration.paytrail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.riista.api.external.PaytrailController;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.integration.paytrail.auth.PaytrailAccount;
import fi.riista.integration.paytrail.auth.PaytrailAuthService;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.checkout.PaytrailClientRequestInterceptor;
import fi.riista.integration.paytrail.checkout.model.CallbackUrl;
import fi.riista.integration.paytrail.checkout.model.Payment;
import fi.riista.integration.paytrail.checkout.model.PaytrailPaymentInitResponse;
import fi.riista.integration.paytrail.event.PaytrailPaymentEvent;
import fi.riista.integration.paytrail.event.PaytrailPaymentEventRepository;
import fi.riista.integration.paytrail.util.CheckoutAlgorithm;
import fi.riista.integration.paytrail.util.CheckoutConstants;
import fi.riista.integration.paytrail.util.PaytrailMessageSignatureUtil;
import fi.riista.util.DateUtil;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ACCOUNT;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_ALGORITHM;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_METHOD;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_NONCE;
import static fi.riista.integration.paytrail.util.CheckoutConstants.CHECKOUT_TIMESTAMP;
import static fi.riista.integration.paytrail.util.CheckoutConstants.PAYTRAIL_SERVICE_URI;
import static fi.riista.util.DateUtil.TIMESTAMP_FORMAT_WITH_OFFSET_ZONE;
import static fi.riista.util.MediaTypeExtras.APPLICATION_JSON_UTF8;
import static java.util.Collections.singletonList;

@Component
public class PaytrailServiceImpl implements PaytrailService {

    private static final Logger LOG = LoggerFactory.getLogger(PaytrailServiceImpl.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormat.forPattern(TIMESTAMP_FORMAT_WITH_OFFSET_ZONE);

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Resource
    private PaytrailAuthService authService;

    @Resource
    private ClientHttpRequestFactory requestFactory;

    @Resource
    private PaytrailMessageSignatureUtil authUtil;

    @Resource
    private PaytrailPaymentEventRepository paytrailPaymentEventRepository;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        this.objectMapper = objectMapper;
        final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setObjectMapper(objectMapper);

        final RestTemplate restTemplate = new RestTemplate(singletonList(jsonMessageConverter));
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(requestFactory);

        restTemplate.setRequestFactory(factory);
        restTemplate.setInterceptors(Arrays.asList(new PaytrailClientRequestInterceptor(authUtil)));

        this.restTemplate = restTemplate;
    }

    @Override
    public PaytrailPaymentInitResponse initiatePayment(final Payment payment, final PaytrailAccount account) {

        try {
            final URI uri = UriComponentsBuilder.fromUriString(PAYTRAIL_SERVICE_URI).build().toUri();

            final HttpHeaders requestHeaders = new HttpHeaders();

            requestHeaders.setAccept(singletonList(APPLICATION_JSON_UTF8));
            requestHeaders.setContentType(APPLICATION_JSON_UTF8);
            requestHeaders.set(CHECKOUT_ACCOUNT, authService.resolveCredentials(account).getMerchantId());
            requestHeaders.set(CHECKOUT_ALGORITHM, CheckoutAlgorithm.HMAC_SHA512.getValue());
            requestHeaders.set(CHECKOUT_METHOD, "POST");
            requestHeaders.set(CHECKOUT_NONCE, payment.getStamp());
            requestHeaders.set(CHECKOUT_TIMESTAMP, DateUtil.now().toString(DATE_TIME_FORMATTER));

            final HttpEntity<Payment> requestEntity = new HttpEntity<>(payment, requestHeaders);

            final ResponseEntity<PaytrailPaymentInitResponse> responseEntity = restTemplate
                    .exchange(uri, HttpMethod.POST, requestEntity, PaytrailPaymentInitResponse.class);

            return responseEntity.getBody();

        } catch (final Exception e) {
            LOG.warn("Exception during payment.", e);
            return new PaytrailPaymentInitResponse();
        }
    }

    @Override
    public CallbackUrl createCallbacks(MultiValueMap<String, String> queryParameters) {
        final CallbackUrl callbacks = new CallbackUrl();
        callbacks.setSuccess(getUri(PaytrailController.CALLBACK_SUCCESS_PATH, queryParameters));
        callbacks.setCancel(getUri(PaytrailController.CALLBACK_CANCEL_PATH, queryParameters));

        return callbacks;
    }

    @Override
    public CallbackUrl createRedirects(MultiValueMap<String, String> queryParameters) {
        final CallbackUrl callbacks = new CallbackUrl();
        callbacks.setSuccess(getUri(PaytrailController.SUCCESS_PATH, queryParameters));
        callbacks.setCancel(getUri(PaytrailController.CANCEL_PATH, queryParameters));

        return callbacks;
    }

    @Override
    @Transactional
    public void storePaytrailPaymentEvent(final PaytrailCallbackParameters paytrailParams) {
        LOG.info(paytrailParams.formatToString());

        paytrailPaymentEventRepository.save(new PaytrailPaymentEvent(paytrailParams));
    }

    @Override
    public Optional<Integer> getCallbackDelay() {
        return CheckoutConstants.CHECKOUT_CALLBACK_DELAY_SECONDS;
    }

    private URI getUri(final String path, final MultiValueMap<String, String> queryParameters) {
        final URI baseUri = runtimeEnvironmentUtil.getBackendBaseUri();

        return UriComponentsBuilder.fromUri(baseUri)
                .replacePath(path)
                .replaceQueryParams(queryParameters)
                .build(false)
                .encode()
                .toUri();
    }
}
