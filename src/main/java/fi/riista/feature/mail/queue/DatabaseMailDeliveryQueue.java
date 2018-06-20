package fi.riista.feature.mail.queue;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import fi.riista.config.properties.MailProperties;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailMessageDelivery;
import fi.riista.sql.SQMailMessageRecipient;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DatabaseMailDeliveryQueue implements MailDeliveryQueue {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseMailDeliveryQueue.class);

    @Resource
    private MailProperties mailProperties;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void scheduleForDelivery(final MailMessageDTO dto) {
        final Stopwatch sw = Stopwatch.createStarted();

        final MailMessage mailMessage = new MailMessage();

        mailMessage.setSubmitTime(DateUtil.now());
        mailMessage.setScheduledTime(dto.getScheduledTime());
        mailMessage.setBody(dto.getBody());
        mailMessage.setSubject(dto.getSubject());
        mailMessage.setFromEmail(dto.getFrom());

        entityManager.persist(mailMessage);
        entityManager.flush();

        Iterables.partition(dto.getRecipients(), 1000).forEach(partition -> {
            final SQMailMessageRecipient RECIPIENT = SQMailMessageRecipient.mailMessageRecipient;
            final SQLInsertClause batchInsert = sqlQueryFactory.insert(RECIPIENT);

            for (final String recipientEmail : partition) {
                batchInsert.columns(RECIPIENT.mailMessageId, RECIPIENT.email, RECIPIENT.failureCounter)
                        .values(mailMessage.getId(), recipientEmail, 0)
                        .addBatch();
            }

            batchInsert.setBatchToBulk(true);
            batchInsert.execute();
        });

        if (sw.elapsed(TimeUnit.SECONDS) > 1) {
            LOG.info("Took {} to store all recipients", sw);
        }
    }

    @Override
    @Transactional
    public Optional<MailMessageDelivery> nextDelivery() {
        while (true) {
            final MailMessage mailMessage = getUndeliveredMessage();

            if (mailMessage == null) {
                return Optional.empty();
            }

            final List<MailMessageDelivery.Recipient> recipients = getUndeliveredRecipients(mailMessage);

            if (recipients.isEmpty()) {
                mailMessage.setDelivered(true);
                entityManager.flush();

                LOG.info("All recipients have been processed for messageId={} subject={}",
                        mailMessage.getId(), mailMessage.getSubject());

            } else {
                return Optional.of(new MailMessageDelivery(mailMessage, recipients));
            }
        }
    }

    private MailMessage getUndeliveredMessage() {
        final QMailMessage MSG = QMailMessage.mailMessage;

        return jpqlQueryFactory
                .selectFrom(MSG).where(MSG.delivered.isFalse(), MSG.scheduledTime.before(DateUtil.now()))
                .orderBy(MSG.scheduledTime.asc())
                .fetchFirst();
    }

    private List<MailMessageDelivery.Recipient> getUndeliveredRecipients(final MailMessage mailMessage) {
        final QMailMessageRecipient RECIPIENT = QMailMessageRecipient.mailMessageRecipient;

        return jpqlQueryFactory
                .select(Projections.constructor(MailMessageDelivery.Recipient.class, RECIPIENT.id, RECIPIENT.email))
                .from(RECIPIENT)
                .where(RECIPIENT.mailMessage.eq(mailMessage),
                        RECIPIENT.deliveryTime.isNull(),
                        RECIPIENT.failureCounter.lt(mailProperties.getMaxFailuresForRecipient()))
                .limit(mailProperties.getBatchSize())
                .orderBy(RECIPIENT.failureCounter.asc())
                .fetch();
    }

    @Override
    @Transactional
    public void storeDeliveryStatus(final MailMessageDelivery delivery) {
        final DateTime now = DateUtil.now();
        final QMailMessageRecipient RECIPIENT = QMailMessageRecipient.mailMessageRecipient;

        delivery.consumeDeliveredRecipientIds(deliveredIds -> {
            jpqlQueryFactory.update(RECIPIENT)
                    .set(RECIPIENT.deliveryTime, now)
                    .where(RECIPIENT.id.in(deliveredIds))
                    .execute();
        });

        delivery.consumeFailedRecipientIds(failedIds -> {
            jpqlQueryFactory.update(RECIPIENT)
                    .set(RECIPIENT.failureCounter, RECIPIENT.failureCounter.add(1))
                    .where(RECIPIENT.id.in(failedIds))
                    .execute();
        });
    }
}
