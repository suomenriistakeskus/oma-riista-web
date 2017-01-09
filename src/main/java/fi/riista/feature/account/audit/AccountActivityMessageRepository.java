package fi.riista.feature.account.audit;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountActivityMessageRepository extends BaseRepository<AccountActivityMessage, Long> {
    Page<AccountActivityMessage> findByUserId(Long userId, Pageable page);
}
