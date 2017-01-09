package fi.riista.feature.mail.token;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailTokenRepository extends BaseRepository<EmailToken, String> {
    @Query("SELECT t FROM EmailToken t WHERE t.user = ?1 AND t.revokedAt IS NULL")
    List<EmailToken> findNonRevokedByUser(SystemUser user);

    @Query("SELECT t FROM EmailToken t WHERE t.email LIKE ?1 AND t.revokedAt IS NULL")
    List<EmailToken> findNonRevokedByEmail(String email);
}
