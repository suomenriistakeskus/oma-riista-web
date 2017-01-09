package fi.riista.feature.sms.consumer;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import fi.riista.feature.sms.SMSReceivedMessage;
import fi.riista.feature.sms.SMSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SMSConsumerServiceImpl implements SMSConsumerService {
    private static final Logger LOG = LoggerFactory.getLogger(SMSConsumerServiceImpl.class);

    public static final int MAX_PROCESSED_MESSAGES = 5;

    @Resource
    private SMSService smsService;

    @Resource
    private List<SMSMessageConsumer> messageConsumers;

    @Override
    public void invokeMessageConsumers() {
        final Set<Long> successful = new HashSet<>();
        final Map<Long, String> rejected = Maps.newHashMap();

        for (final SMSReceivedMessage sms : smsService.getIncomingMessages(MAX_PROCESSED_MESSAGES)) {
            tryConsume(new MessageWrapper(sms, successful, rejected));
        }

        smsService.acknowledgeMessages(successful, rejected);
    }

    private void tryConsume(final MessageWrapper sms) {
        try {
            if (!sms.processUsing(messageConsumers)) {
                LOG.error("Unknown SMS message: {}", sms);

                sms.reject("No consumer could process message");
            }

        } catch (SMSMessageConsumerException smsEx) {
            LOG.error("SMS consumer failed to process message", smsEx);

            sms.reject(smsEx);

        } catch (Exception ex) {
            LOG.error("Unknown handing exception for incoming SMS", ex);

            sms.reject(ex);
        }
    }

    static class MessageWrapper {
        private final SMSReceivedMessage sms;
        private final Set<Long> successful;
        private final Map<Long, String> rejected;

        private MessageWrapper(final SMSReceivedMessage sms,
                               final Set<Long> successful,
                               final Map<Long, String> rejected) {
            this.sms = sms;
            this.successful = successful;
            this.rejected = rejected;
        }

        public boolean processUsing(List<SMSMessageConsumer> consumers) {
            // Pick first consumer which can process message
            for (final SMSMessageConsumer consumer : consumers) {
                if (processUsing(consumer)) {
                    return true;
                }
            }

            return false;
        }

        public boolean processUsing(SMSMessageConsumer consumer) {
            if (consumer.consume(this.sms)) {
                successful.add(this.sms.getId());

                return true;
            }

            return false;
        }

        public void reject(Exception exception) {
            // Store error response with failed SMS message
            final Throwable rootCause = Throwables.getRootCause(exception);

            rejected.put(sms.getId(), "Exception caught "
                    + rootCause.getClass().getName()
                    + " : " + rootCause.getMessage());
        }

        public void reject(String errorMessage) {
            // Store error response with failed SMS message
            rejected.put(sms.getId(), errorMessage);
        }
    }
}
