package fi.riista.integration.paytrail;

import fi.riista.api.external.PaytrailController;
import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.integration.paytrail.auth.PaytrailAccount;
import fi.riista.integration.paytrail.auth.PaytrailAuthService;
import fi.riista.integration.paytrail.callback.PaytrailCallbackParameters;
import fi.riista.integration.paytrail.e2.PaytrailFormBuilder;
import fi.riista.integration.paytrail.e2.model.CallbackUrlSet;
import fi.riista.integration.paytrail.e2.model.Payment;
import fi.riista.integration.paytrail.event.PaytrailPaymentEvent;
import fi.riista.integration.paytrail.event.PaytrailPaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Map;

@Service
public class PaytrailServiceImpl implements PaytrailService {
    private static final Logger LOG = LoggerFactory.getLogger(PaytrailServiceImpl.class);

    @Resource
    private PaytrailPaymentEventRepository paytrailPaymentEventRepository;

    @Resource
    private PaytrailAuthService paytrailAuthService;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Override
    public Map<String, String> getPaymentForm(final Payment payment, final PaytrailAccount account) {
        return PaytrailFormBuilder.createForm(paytrailAuthService.resolveCredentials(account), payment);
    }

    @Override
    public CallbackUrlSet createCallbacks(MultiValueMap<String, String> queryParameters) {
        final CallbackUrlSet callbacks = new CallbackUrlSet();
        callbacks.setSuccessUri(getUri(PaytrailController.SUCCESS_PATH, queryParameters));
        callbacks.setCancelUri(getUri(PaytrailController.CANCEL_PATH, queryParameters));
        callbacks.setNotifyUri(getUri(PaytrailController.NOTIFY_PATH, null));

        return callbacks;
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

    @Override
    @Transactional
    public void storePaytrailPaymentEvent(final PaytrailCallbackParameters paytrailParams) {
        LOG.info(paytrailParams.formatToString());

        paytrailPaymentEventRepository.save(new PaytrailPaymentEvent(paytrailParams));
    }
}
