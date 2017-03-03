package fi.riista.feature.account.audit;

import com.google.common.collect.ImmutableMap;

import fi.riista.feature.account.user.ActiveUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.Map;

@Component
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    @Resource
    private ActiveUserService activeUserService;

    public ImmutableMap.Builder<String, Object> extra(String k, Object v) {
        return new ImmutableMap.Builder<String, Object>().put(k, v);
    }

    public void log(String event, Object target) {
        log(event, target, Collections.<String, Object> emptyMap());
    }

    public void log(String event, Object target, final ImmutableMap.Builder<String, Object> extraInfo) {
        log(event, target, extraInfo.build());
    }

    public void log(String event, Object target, final Map<String, Object> extraInfo) {
        String msg = createLogMessage(event, target, extraInfo);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            doLogAfterTransaction(msg);
        } else {
            doLog(msg);
        }
    }

    private String createLogMessage(String event, Object target, Map<String, Object> extraInfo) {
        Long userId = getActiveUserId();
        StringBuilder sb = new StringBuilder();
        add(sb, "userId", userId);
        add(sb, "event", event);
        add(sb, "target", target);

        extraInfo.forEach((key, value) -> add(sb, key, value));
        return sb.toString();
    }

    private Long getActiveUserId() {
        if (activeUserService.isAuthenticated()) {
            return activeUserService.getActiveUserId();
        }
        return null;
    }

    private static void add(StringBuilder sb, String k, Object v) {
        sb.append(k).append(':').append(v).append(' ');
    }

    private static void doLogAfterTransaction(final String msg) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                doLog(msg);
            }
        });
    }

    private static void doLog(String msg) {
        LOG.info(msg);
    }
}
