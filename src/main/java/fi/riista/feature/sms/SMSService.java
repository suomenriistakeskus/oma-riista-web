package fi.riista.feature.sms;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.sms.delivery.SMSGatewayProperties;
import fi.riista.feature.sms.delivery.SMSGatewayProvider;
import fi.riista.feature.sms.delivery.TokenQuotaLimiter;
import fi.riista.feature.sms.storage.SMSMessageRepository;
import fi.riista.feature.sms.storage.SMSMessageStatus;
import fi.riista.feature.sms.storage.SMSPersistentMessage;
import fi.riista.util.F;
import fi.riista.validation.PhoneNumberValidator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SMSService {
    private static final Logger LOG = LoggerFactory.getLogger(SMSService.class);

    @Resource
    private SMSGatewayProvider smsGatewayProvider;

    @Resource
    private SMSMessageRepository smsMessageRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private SMSGatewayProperties properties;

    // Expire quota after one hour
    private TokenQuotaLimiter tokenQuotaLimiter;

    @PostConstruct
    public void init() {
        this.tokenQuotaLimiter =  new TokenQuotaLimiter(
                properties.getQuotaPeriodMinutes(), TimeUnit.MINUTES, properties.getQuotaSize());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = NumberParseException.class)
    public SMSSentMessage sendMessage(final String toNumber, final String message) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(toNumber, PhoneNumberValidator.DEFAULT_REGION);

        if (!phoneUtil.isValidNumber(phoneNumber)) {
            final SMSPersistentMessage failedMessage = createFailedMessage(
                    phoneNumber, message, "Not a valid phone number");

            LOG.warn("Invalid phoneNumber {}", phoneNumber);

            return new SMSSentMessage(smsMessageRepository.save(failedMessage));
        }

        if (checkQuotaExceeded(phoneNumber)) {
            final SMSPersistentMessage failedMessage = createFailedMessage(
                    phoneNumber, message, "Send quota for phoneNumber has been exceeded");

            LOG.warn("Send quota has exceeded for number {}", phoneNumber);

            return new SMSSentMessage(smsMessageRepository.save(failedMessage));
        }

        final SMSPersistentMessage persistentMessage = smsGatewayProvider.sendMessage(phoneNumber, message);

        persistentMessage.setSystemUser(getSendingUser());

        return new SMSSentMessage(smsMessageRepository.save(persistentMessage));
    }

    private SMSPersistentMessage createFailedMessage(final Phonenumber.PhoneNumber phoneNumber,
                                                     final String message, final String errorMessage) {
        final SMSPersistentMessage persistentMessage = new SMSPersistentMessage();

        persistentMessage.setStatus(SMSMessageStatus.ERROR, errorMessage);
        persistentMessage.setDirection(SMSPersistentMessage.Direction.OUT);
        persistentMessage.setMessage(message);
        persistentMessage.setSystemUser(getSendingUser());
        persistentMessage.setNumberTo(PhoneNumberUtil.getInstance().format(
                phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164));

        return persistentMessage;
    }

    private SystemUser getSendingUser() {
        return activeUserService.findActiveUser().orElse(null);
    }

    private boolean checkQuotaExceeded(final Phonenumber.PhoneNumber phoneNumber) {
        final String quotaToken = PhoneNumberUtil.getInstance().format(
                phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);

        return !tokenQuotaLimiter.acquire(quotaToken);
    }

    @Transactional(readOnly = true)
    public List<SMSReceivedMessage> getIncomingMessages(int limit) {
        final PageRequest pageRequest = new PageRequest(0, limit, Sort.Direction.DESC, "id");
        final List<SMSPersistentMessage> receivedSmsList = smsMessageRepository.findByDirectionAndStatus(
                SMSPersistentMessage.Direction.IN, SMSMessageStatus.PENDING, pageRequest);

        return F.mapNonNullsToList(receivedSmsList, SMSReceivedMessage::new);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeIncomingMessages() {
        final List<SMSPersistentMessage> incomingMessages = smsGatewayProvider.pollForIncomingMessages();

        if (incomingMessages != null && !incomingMessages.isEmpty()) {
            smsMessageRepository.save(incomingMessages);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acknowledgeMessages(final Set<Long> successful, final Map<Long, String> rejected) {
        for (final Long messageId : successful) {
            final SMSPersistentMessage message = smsMessageRepository.findOne(messageId);

            if (message != null) {
                message.setStatus(SMSMessageStatus.SUCCESSFUL, null);
            }
        }

        rejected.forEach((key, value) -> Optional.ofNullable(smsMessageRepository.findOne(key)).ifPresent(message -> {
            message.setStatus(SMSMessageStatus.ERROR, value);
        }));
    }

    @Transactional
    public void purgeOldMessages(int purgeAfterDays) {
        // Delete anything which has been processed and older than one week
        final DateTime before = DateTime.now().minusDays(purgeAfterDays);

        smsMessageRepository.deleteByStatusAndTimestamp(SMSMessageStatus.SUCCESSFUL, before.toDate());
    }
}
