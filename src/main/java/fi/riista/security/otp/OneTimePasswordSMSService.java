package fi.riista.security.otp;

import com.google.i18n.phonenumbers.NumberParseException;
import fi.riista.feature.sms.SMSSentMessage;
import fi.riista.feature.sms.SMSService;
import fi.riista.feature.sms.storage.SMSMessageStatus;
import fi.riista.security.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OneTimePasswordSMSService {
    private static final Logger LOG = LoggerFactory.getLogger(OneTimePasswordSMSService.class);

    @Resource
    private SMSService smsService;

    public boolean sendCodeUsingSMS(final OneTimePasswordRequiredException otpException) {
        if (otpException.getUserDetails() instanceof UserInfo) {
            try {
                return sendCodeUsingSMS((UserInfo) otpException.getUserDetails(), otpException.getExpectedCode());

            } catch (Exception ex) {
                LOG.error("Could not send one time password using SMS", ex);
            }
        }

        return false;
    }

    /**
     * Try to send expected OTP to user phoneNumber if available.
     * @param userInfo      target user details
     * @param expectedCode  correct OTP for current time and user
     * @return true, if phoneNumber was found and message was sent successfully
     * @throws com.google.i18n.phonenumbers.NumberParseException if number stored in user details is not valid
     */
    private boolean sendCodeUsingSMS(UserInfo userInfo, String expectedCode) throws NumberParseException {
        if (StringUtils.isNotBlank(userInfo.getPhoneNumber())) {
            final SMSSentMessage smsSentMessage = smsService.sendMessage(userInfo.getPhoneNumber(), expectedCode);

            return smsSentMessage.getStatus() == SMSMessageStatus.SUCCESSFUL;
        }

        LOG.warn("User does not have phoneNumber available to send SMS: {}", userInfo.getUsername());

        return false;
    }
}
