package fi.riista.feature.sms.delivery;

import com.google.common.base.Charsets;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.StandardDatabase;
import fi.riista.feature.sms.storage.SMSMessageStatus;
import fi.riista.feature.sms.storage.SMSPersistentMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

@Component
@AmazonDatabase
@StandardDatabase
public class LabyrinttiSMSGatewayProvider implements SMSGatewayProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LabyrinttiSMSGatewayProvider.class);

    private static final Charset SMS_URL_CHARSET = Charsets.ISO_8859_1;

    private static RestTemplate createRestTemplate(ClientHttpRequestFactory requestFactory) {
        // Request converter
        final FormHttpMessageConverter requestConverter = new FormHttpMessageConverter();
        requestConverter.setCharset(SMS_URL_CHARSET);

        // Response converter
        final StringHttpMessageConverter responseConverter = new StringHttpMessageConverter(SMS_URL_CHARSET);

        final RestTemplate restTemplate = new RestTemplate(Arrays.asList(requestConverter, responseConverter));
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }

    @Resource
    private SMSGatewayProperties properties;

    @Resource
    private ClientHttpRequestFactory requestFactory;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = createRestTemplate(requestFactory);
    }

    @Override
    public List<SMSPersistentMessage> pollForIncomingMessages() {
        return Collections.emptyList();
    }

    @Override
    public SMSPersistentMessage sendMessage(Phonenumber.PhoneNumber phoneNumber, String messageText) {
        final SMSPersistentMessage message = new SMSPersistentMessage();

        message.setStatus(SMSMessageStatus.PENDING, null);
        message.setDirection(SMSPersistentMessage.Direction.OUT);
        message.setNumberTo(PhoneNumberUtil.getInstance().format(
                phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164));
        message.setMessage(messageText);

        try {
            sendMessage(message);

        } catch (Exception ex) {
            LOG.error("SMS send to number " + message.getNumberTo() + " has failed", ex);
            message.setErrorStatus(ex);
        }

        return message;
    }

    private void sendMessage(SMSPersistentMessage message) {
        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add("user", properties.getGatewayUsername());
        requestBody.add("password", properties.getGatewayPassword());

        // One or more destination phone numbers, separated with commas (,). Numbers can be in national or
        // international format and include separators like spaces, hyphens and parentheses.
        requestBody.add("dests", message.getNumberTo());

        // Message in plain text. \n is replaced with a line feed, \r with a carriage return, and \\ with a backslash,
        // although they can be specified using URL encoding also. Normally, \n is enough for SMS newline.
        requestBody.add("text", message.getMessage());

        // Name or phone number displayed instead of service number in receivers’ mobile phones.
        // Default is set in user account configuration (initially unspecified).
        if (StringUtils.isNotBlank(properties.getGatewaySourceName())) {
            requestBody.add("source-name", properties.getGatewaySourceName());
        }

        // Message validity period. Either a minute value or absolute time in format yyyy-mm-dd hh:mm.
        // If a message has not been received by mobile phone when its validity expires,
        // SMS center deletes the message. Default is set in user account configuration (initially 1440 minutes).
        requestBody.add("validity", "5");

        final String response = this.restTemplate.postForObject(properties.getGatewayUri(), requestBody, String.class);

        // result has one line per destination, e.g. "+358401234567 OK 1 message accepted for sending"
        parseResponseLine(message, new StringTokenizer(response));
    }

    private static void parseResponseLine(final SMSPersistentMessage message, final StringTokenizer st) {
        final String number = st.nextToken();
        final String status = st.nextToken();
//        final String code = st.nextToken();

        final StringBuilder statusMessage = new StringBuilder();
        while (st.hasMoreTokens()) {
            statusMessage.append(" ");
            statusMessage.append(st.nextToken());
        }

        message.setNumberTo(number);

        if (status.equalsIgnoreCase("OK")) {
            // For messages which have been accepted for sending, the response format is:
            // <phone-number> OK <message-count> <description>
            // +358401234567 OK 1 message accepted for sending

            message.setStatus(SMSMessageStatus.SUCCESSFUL, statusMessage.toString());

        } else {
            // For messages which have been denied, the response format is:
            // <phone-number> ERROR <error-code> <message-count> <description>
            // +358401234567 ERROR 3 1 message failed: Duplicate destination phone number

            message.setStatus(SMSMessageStatus.ERROR, status + " " + statusMessage.toString());

            // Current error codes:
            // If the HTTP request parameters are invalid, username or password wrong, client IP address unallowed
            // for user account, protocol (HTTP or HTTPS) unallowed, or SMS sending unallowed,
            // SMS Gateway responds with a text/ plain error message and HTTP error status code.

            // 1 Unknown error:        Error whose reason is not known or specified.
            // 2 Invalid recipient:    Recipient phone number syntax is invalid.
            //                         For example, too short or long.
            // 3 Duplicate recipient:  The same recipient phone number has been specified multiple times.
            //                         Only one message will be sent to each phone number.
            // 4 Un-allowed recipient: Recipient phone number is not allowed in user account configuration.
            //                         For example, non-mobile or foreign recipient phone numbers can be denied.
        }
    }
}
