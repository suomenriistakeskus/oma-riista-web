package fi.riista.config.quartz;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.UUID;

public abstract class RunAsAdminJob implements Job {

    @Resource
    private ActiveUserService activeUserService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            activeUserService.loginWithoutCheck(createUser());
            executeAsAdmin();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private SystemUser createUser() {
        final SystemUser u = new SystemUser() {
            @Override
            public String getHashedPassword() {
                return UUID.randomUUID().toString();
            }
        };
        u.setId(ActiveUserService.SCHEDULED_TASK_USER_ID);
        u.setUsername(getClass().getSimpleName());
        u.setRole(SystemUser.Role.ROLE_ADMIN);
        return u;
    }

    protected abstract void executeAsAdmin();
}
