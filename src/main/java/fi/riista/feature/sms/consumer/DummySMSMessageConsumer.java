package fi.riista.feature.sms.consumer;

import fi.riista.feature.sms.SMSReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DummySMSMessageConsumer implements SMSMessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(DummySMSMessageConsumer.class);

    @Override
    public boolean consume(SMSReceivedMessage sms) {
        LOG.error("Cannot consume SMS from={} with body={}", sms.getNumberFrom(), sms.getMessage());

        return false;
    }
}
