package fi.riista.integration.srva.callring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RetryOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SrvaUpdateCallRingFeature {
    private static final Logger LOG = LoggerFactory.getLogger(SrvaUpdateCallRingFeature.class);

    @Resource
    private TempoApiAdapter tempoApiAdapter;

    @Resource
    private SrvaCallRingConfigurationService srvaCallerConfigurationService;

    @Qualifier("tempo")
    @Resource
    private RetryOperations tempoApiRetryPolicy;

    @Transactional(readOnly = true)
    public void configureAll() {
        final int finnishLanguageId = getFinnishLanguageId();

        final List<SrvaCallRingConfiguration> callRingConfigurationList =
                srvaCallerConfigurationService.generateConfigurationForEveryRhy();

        callApi(callRingConfigurationList, (rhyConfiguration, counter, total) -> {
            LOG.info("Configuring SRVA call ring {}/{} for rhyOfficialCode={}",
                    counter, total, rhyConfiguration.getRhyOfficialCode());

            tempoApiAdapter.configureCallRing(rhyConfiguration, finnishLanguageId);
        });

        callApi(callRingConfigurationList, (rhyConfiguration, counter, total) -> {
            LOG.info("Configuring SRVA call tracking {}/{} for rhyOfficialCode={}",
                    counter, total, rhyConfiguration.getRhyOfficialCode());

            tempoApiAdapter.configureCallTrackingRules(rhyConfiguration);
        });
    }

    private int getFinnishLanguageId() {
        final Map<String, Integer> languageCodeMap = tempoApiRetryPolicy.execute(
                retryContext -> tempoApiAdapter.getLanguageCodeMap());

        final Integer languageId = languageCodeMap.get("FI");

        if (languageId == null) {
            throw new IllegalStateException("Could not determine finnish languageId");
        }

        return languageId;
    }

    @FunctionalInterface
    private interface ApiCallback<T> {
        void apply(final T item, final int counter, final int total);
    }

    private <T> void callApi(final List<T> itemList, final ApiCallback<T> delegate) {
        final int total = itemList.size();
        final AtomicInteger completeCounter = new AtomicInteger();

        itemList.forEach(item -> {
            final int counterValue = completeCounter.addAndGet(1);

            tempoApiRetryPolicy.execute(retryContext -> {
                delegate.apply(item, counterValue, total);
                return null;
            });
        });
    }
}
