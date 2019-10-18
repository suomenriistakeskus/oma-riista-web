package fi.riista.feature.account;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@Service
public class AccountSessionService {

    @Resource
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteSessions(final List<String> usernameList) {
        usernameList.forEach(this::deleteSessions);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteSessions(final String username) {
        findSessionIdsByUsername(username).forEach(sessionRepository::delete);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteOtherActiveSessions(final String username, final HttpServletRequest request) {
        final String activeSessionId = request.getRequestedSessionId();

        findSessionIdsByUsername(username)
                .filter(id -> !Objects.equals(id, activeSessionId))
                .forEach(sessionRepository::delete);
    }

    private Stream<String> findSessionIdsByUsername(final String username) {
        return sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, username).keySet().stream();
    }

}
