package fi.riista.feature.mail.bounce;

import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import fi.riista.config.jackson.CustomJacksonObjectMapper;
import fi.riista.config.properties.AWSConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class MailMessageBounceAndComplaintListener {
    private static final Logger LOG = LoggerFactory.getLogger(MailMessageBounceAndComplaintListener.class);

    @Resource
    private MailMessageBounceService mailMessageBounceService;

    @Resource
    private MailMessageComplaintService mailMessageComplaintService;

    @Resource
    private AmazonSQSBufferedAsyncClient sqsClient;

    @Resource
    private AWSConfigProperties awsConfigProperties;

    @Resource
    private CustomJacksonObjectMapper objectMapper;

    public void pollForBounces() throws IOException {
        receiveAndHandleMessages(awsConfigProperties.getSesBounceQueue(),
                AmazonSesBounceNotification.class,
                bounceNotification -> mailMessageBounceService.storeBounce(bounceNotification));
    }

    public void pollForComplaints() throws IOException {
        receiveAndHandleMessages(awsConfigProperties.getSesComplaintQueue(),
                AmazonSesComplaintNotification.class,
                complaintNotification -> mailMessageComplaintService.storeComplaint(complaintNotification));
    }

    private <T extends HasReceiptHandle> void receiveAndHandleMessages(final String queueUri,
                                                                       final Class<T> messageTypeClass,
                                                                       final Consumer<T> processor) throws IOException {
        if (!StringUtils.hasText(queueUri)) {
            LOG.warn("Queue URI not set for " + messageTypeClass.getSimpleName());
            return;
        }

        // Limit maximum iterations
        for (int i = 0; i < 100; i++) {
            final ReceiveMessageResult receiveMessageResult = receiveBounce(queueUri);

            final List<T> messageList = parseSnsNotifications(receiveMessageResult, messageTypeClass);

            if (messageList.isEmpty()) {
                LOG.info("Received empty response");
                break;
            }

            LOG.info("Received {} messages", messageList.size());

            final List<String> receiptHandles = new LinkedList<>();

            try {
                for (final T message : messageList) {
                    processor.accept(message);
                    receiptHandles.add(message.getReceiptHandle());
                }

            } finally {
                LOG.info("Acknowledging {} messages", receiptHandles.size());
                acknowledgeMessagesFromSqs(queueUri, receiptHandles);
            }
        }
    }

    private ReceiveMessageResult receiveBounce(final String queueUrl) {
        final ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
        request.setMaxNumberOfMessages(10);
        request.setVisibilityTimeout(120);

        return sqsClient.receiveMessage(request);
    }

    private <T extends HasReceiptHandle> List<T> parseSnsNotifications(final ReceiveMessageResult receiveMessageResult,
                                                                       final Class<T> messageTypeClass) throws IOException {
        final List<T> bounceList = new LinkedList<>();

        for (final Message sqsMessage : receiveMessageResult.getMessages()) {
            final AmazonSnsNotification snsNotification = objectMapper.readValue(sqsMessage.getBody(), AmazonSnsNotification.class);
            final T bounceNotification = objectMapper.readValue(snsNotification.getMessage(), messageTypeClass);
            bounceNotification.setReceiptHandle(sqsMessage.getReceiptHandle());
            bounceList.add(bounceNotification);
        }

        return bounceList;
    }

    private void acknowledgeMessagesFromSqs(final String queueUrl,
                                            final List<String> receiptHandleList) {
        if (receiptHandleList.isEmpty()) {
            return;
        }

        final List<DeleteMessageBatchRequestEntry> entries = new LinkedList<>();

        for (final String receiptHandle : receiptHandleList) {
            final DeleteMessageBatchRequestEntry deleteMessageBatchRequestEntry = new DeleteMessageBatchRequestEntry();
            deleteMessageBatchRequestEntry.setId(UUID.randomUUID().toString());
            deleteMessageBatchRequestEntry.setReceiptHandle(receiptHandle);
            entries.add(deleteMessageBatchRequestEntry);
        }

        sqsClient.deleteMessageBatch(queueUrl, entries);
    }

}
