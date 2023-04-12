package fi.riista.feature.mail.bounce;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(Theories.class)
public class MailMessageBounceServiceTest extends EmbeddedDatabaseTest {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT = ISODateTimeFormat.dateTime();

    @Resource
    private MailMessageBounceService service;

    @Resource
    private MailMessageBounceRepository bounceRepository;

    @Test
    public void testHandleBounce_smoke() {
        final String emailAddress = "test@riista.fi";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            assertThat(bounces, hasSize(1));
            final MailMessageBounce mailMessageBounce = bounces.get(0);
            assertThat(mailMessageBounce.getBounceFeedbackId(), equalTo(notification.getBounce().getFeedbackId()));

            assertThat(notification.getBounce().getBouncedRecipients(), hasSize(1));

            final AmazonSesBouncedRecipient bouncedRecipient = notification.getBounce().getBouncedRecipients().get(0);
            assertThat(bouncedRecipient.getEmailAddress(), equalTo(emailAddress));
        });
    }

    @Test
    public void testHandleBounce_invalidMailAddress() {
        final String emailAddress = "this is invalid email address";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            assertThat(bounces, hasSize(0));
        });
    }


    @Test
    public void testHandleBounce_unknownBounceType() {
        final String emailAddress = "test@riista.fi";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        notification.getBounce().setBounceType("SomeUnknownValue");
        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            final MailMessageBounce mailMessageBounce = bounces.get(0);
            assertThat(mailMessageBounce.getBounceFeedbackId(), equalTo(notification.getBounce().getFeedbackId()));

            assertThat(notification.getBounce().getBouncedRecipients(), hasSize(1));

            final AmazonSesBouncedRecipient bouncedRecipient = notification.getBounce().getBouncedRecipients().get(0);
            assertThat(bouncedRecipient.getEmailAddress(), equalTo(emailAddress));

            assertThat(mailMessageBounce.getBounceType(), equalTo(MailMessageBounce.BounceType.Undetermined));
        });
    }

    @Theory
    public void testHandleBounce_enumerationBounceTypeValuesArePersistable(
            final MailMessageBounce.BounceType bounceType) {

        final String emailAddress = "test@riista.fi";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        notification.getBounce().setBounceType(bounceType.name());
        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            final MailMessageBounce mailMessageBounce = bounces.get(0);
            assertThat(mailMessageBounce.getBounceFeedbackId(), equalTo(notification.getBounce().getFeedbackId()));

            assertThat(notification.getBounce().getBouncedRecipients(), hasSize(1));

            final AmazonSesBouncedRecipient bouncedRecipient = notification.getBounce().getBouncedRecipients().get(0);
            assertThat(bouncedRecipient.getEmailAddress(), equalTo(emailAddress));

            assertThat(mailMessageBounce.getBounceType(), equalTo(bounceType));
        });
    }

    @Theory
    public void testHandleBounce_enumerationBounceSubTypeValuesArePersistable(
            final MailMessageBounce.BounceSubType subType) {

        final String emailAddress = "test@riista.fi";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        notification.getBounce().setBounceSubType(subType.name());
        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            final MailMessageBounce mailMessageBounce = bounces.get(0);
            assertThat(mailMessageBounce.getBounceFeedbackId(), equalTo(notification.getBounce().getFeedbackId()));

            assertThat(notification.getBounce().getBouncedRecipients(), hasSize(1));

            final AmazonSesBouncedRecipient bouncedRecipient = notification.getBounce().getBouncedRecipients().get(0);
            assertThat(bouncedRecipient.getEmailAddress(), equalTo(emailAddress));

            assertThat(mailMessageBounce.getBounceSubType(), equalTo(subType));
        });
    }

    @Test
    public void testHandleBounce_unknownBounceSubType() {
        final String emailAddress = "test@riista.fi";
        final AmazonSesBounceNotification notification = createNotification(emailAddress);

        notification.getBounce().setBounceSubType("SomeUnknownValue");

        service.storeBounce(notification);

        runInTransaction(() -> {
            final List<MailMessageBounce> bounces = bounceRepository.findAll();
            final MailMessageBounce mailMessageBounce = bounces.get(0);
            assertThat(mailMessageBounce.getBounceFeedbackId(), equalTo(notification.getBounce().getFeedbackId()));

            assertThat(notification.getBounce().getBouncedRecipients(), hasSize(1));

            final AmazonSesBouncedRecipient bouncedRecipient = notification.getBounce().getBouncedRecipients().get(0);
            assertThat(bouncedRecipient.getEmailAddress(), equalTo(emailAddress));

            assertThat(mailMessageBounce.getBounceSubType(), equalTo(MailMessageBounce.BounceSubType.Undetermined));
        });
    }

    private AmazonSesBounceNotification createNotification(final String emailAddress) {
        final AmazonSesBouncedRecipient recipient = new AmazonSesBouncedRecipient();
        recipient.setEmailAddress(emailAddress);
        recipient.setAction("failed");
        recipient.setDiagnosticCode("5.1.1");
        recipient.setStatus("failed");

        final AmazonSesBounce bounce = new AmazonSesBounce();
        bounce.setBounceType("Permanent");
        bounce.setBounceSubType("General");
        bounce.setFeedbackId("feeeback-" + nextPositiveInt());
        bounce.setBouncedRecipients(Collections.singletonList(recipient));
        bounce.setTimestamp(DateUtil.now().toString(ISO_DATETIME_FORMAT));

        final AmazonSesBounceNotification notification = new AmazonSesBounceNotification();
        notification.setBounce(bounce);

        return notification;
    }
}
