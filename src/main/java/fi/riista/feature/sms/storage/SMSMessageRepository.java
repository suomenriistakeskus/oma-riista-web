package fi.riista.feature.sms.storage;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface SMSMessageRepository extends BaseRepository<SMSPersistentMessage, Long> {

    List<SMSPersistentMessage> findByDirectionAndStatus(
            SMSPersistentMessage.Direction direction, SMSMessageStatus status, Pageable page);

    @Modifying
    @Query("DELETE FROM SMSPersistentMessage m WHERE m.status = ?1 AND m.statusTimestamp < ?2")
    void deleteByStatusAndTimestamp(SMSMessageStatus status, Date before);
}
