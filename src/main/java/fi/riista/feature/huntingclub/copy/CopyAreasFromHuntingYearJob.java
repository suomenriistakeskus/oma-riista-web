package fi.riista.feature.huntingclub.copy;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.DateUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "CopyAreasFromHuntingYear",
        enabledProperty = "copy.areas.from.hunting.year.enabled",
        cronExpression = "${copy.areas.from.hunting.year.schedule}"
)
public class CopyAreasFromHuntingYearJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(CopyAreasFromHuntingYearJob.class);

    @Resource
    private CopyAreasFromHuntingYearToAnotherFeature feature;

    @Resource
    private ActiveUserService activeUserService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            LOG.info("Starting ...");
            activeUserService.loginWithoutCheck(createUser());
            final int currentHuntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            feature.copyAreas(currentHuntingYear - 1, currentHuntingYear);
            LOG.info("Done.");
        } catch (Exception e) {
            LOG.error("Processing threw exception", e);
        }
         finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static SystemUser createUser() {
        final SystemUser u = new SystemUser() {
            @Override
            public String getHashedPassword() {
                return this.getClass().getSimpleName();
            }
        };
        u.setId(ActiveUserService.SCHEDULED_TASK_USER_ID);
        u.setUsername(CopyAreasFromHuntingYearJob.class.getSimpleName());
        u.setRole(SystemUser.Role.ROLE_ADMIN);
        return u;
    }
}
