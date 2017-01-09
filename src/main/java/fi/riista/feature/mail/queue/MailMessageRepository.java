package fi.riista.feature.mail.queue;

import fi.riista.feature.common.repository.BaseRepository;
import org.joda.time.DateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MailMessageRepository extends BaseRepository<MailMessage, Long> {
    @Query("SELECT m FROM MailMessage m WHERE m.delivered IS FALSE AND m.failureCounter < ?1 AND m.scheduledTime < ?2")
    List<MailMessage> findUnsentMessages(int maxFailures, DateTime sentBefore, Pageable page);
}
