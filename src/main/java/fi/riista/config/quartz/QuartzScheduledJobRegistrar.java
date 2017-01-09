package fi.riista.config.quartz;

import com.google.common.base.Stopwatch;
import fi.riista.config.Constants;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * This class listeners to ContextStartedEvent, and when the context is started
 * gets all bean definitions, looks for the @ScheduledJob annotation,
 * and registers quartz jobs based on that.
 * Note that a new instance of the quartz job class is created on each execution,
 * so the bean has to be of "prototype" scope. Therefore an applicationListener is used
 * rather than a bean postprocessor (unlike singleton beans, prototype beans don't get
 * created on application startup)
 */
public class QuartzScheduledJobRegistrar implements
        EmbeddedValueResolverAware, ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(QuartzScheduledJobRegistrar.class);

    private Scheduler scheduler;

    private StringValueResolver embeddedValueResolver;

    private ApplicationContext applicationContext;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    static class AnnotatedQuartzJob {
        private final QuartzScheduledJob annotation;
        private final Class<? extends Job> jobClass;

        private AnnotatedQuartzJob(QuartzScheduledJob annotation, Class<? extends Job> jobClass) {
            this.annotation = Objects.requireNonNull(annotation);
            this.jobClass = Objects.requireNonNull(jobClass);
        }

        public QuartzScheduledJob getAnnotation() {
            return annotation;
        }

        public Class<? extends Job> getJobClass() {
            return jobClass;
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        LOG.debug("Scanning for Quartz scheduled jobs...");

        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (event.getApplicationContext() != this.applicationContext) {
            return;
        }

        final AtomicInteger enabledJobCount = new AtomicInteger(0);

        findAnnotatedJobs(event.getApplicationContext()).forEach(annotatedJob ->  {
            final JobDetail jobDetail = createJobDetail(annotatedJob);
            final Trigger trigger = createTrigger(annotatedJob, jobDetail);

            try {
                if (isEnabled(annotatedJob.getAnnotation())) {
                    enabledJobCount.incrementAndGet();

                    LOG.info("Scheduling job={} with trigger={}", jobDetail.getKey(), trigger.getKey());
                    scheduleJob(jobDetail, trigger);

                } else if (scheduler.getJobDetail(jobDetail.getKey()) != null) {
                    LOG.info("Removing job={}", jobDetail.getKey());
                    scheduler.deleteJob(jobDetail.getKey());
                }
            } catch (SchedulerException e) {
                throw new IllegalStateException(e);
            }
        });

        LOG.debug("Application Context scan completed, took {} ms. Found {} enabled jobs",
                stopwatch.elapsed(TimeUnit.MILLISECONDS), enabledJobCount.get());
    }

    private static List<AnnotatedQuartzJob> findAnnotatedJobs(final ApplicationContext applicationContext) {
        return applicationContext.getBeansOfType(Job.class, true, false).values().stream()
                .filter(Objects::nonNull)
                .map(bean -> {
                    final QuartzScheduledJob annotation = bean.getClass().getAnnotation(QuartzScheduledJob.class);
                    return annotation == null ? null : new AnnotatedQuartzJob(annotation, bean.getClass());
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private boolean isEnabled(final QuartzScheduledJob annotation) {
        return !StringUtils.hasText(annotation.enabledProperty()) ||
                applicationContext.getEnvironment().getProperty(annotation.enabledProperty(), boolean.class);
    }

    private void scheduleJob(final JobDetail jobDetail, final Trigger trigger) throws SchedulerException {
        final boolean triggerExists = (this.scheduler.getTrigger(trigger.getKey()) != null);

        this.scheduler.addJob(jobDetail, true);

        if (triggerExists) {
            this.scheduler.rescheduleJob(trigger.getKey(), trigger);

        } else {
            try {
                this.scheduler.scheduleJob(trigger);

            } catch (ObjectAlreadyExistsException ex) {
                LOG.debug("Unexpectedly found existing trigger, assumably due to cluster race condition: " +
                        ex.getMessage() + " - can safely be ignored");

                this.scheduler.rescheduleJob(trigger.getKey(), trigger);
            }
        }
    }

    private static JobDetail createJobDetail(final AnnotatedQuartzJob annotatedJob) {
        final QuartzScheduledJob annotation = annotatedJob.getAnnotation();
        final Class<? extends Job> jobClass = annotatedJob.getJobClass();

        return JobBuilder.newJob()
                .ofType(jobClass)
                .withIdentity(
                        annotation.name().isEmpty() ? jobClass.getSimpleName() : annotation.name(),
                        annotation.group().isEmpty() ? "default" : annotation.group())
                .storeDurably(true)
                .requestRecovery(true)
                .build();
    }

    private Trigger createTrigger(final AnnotatedQuartzJob annotatedJob,
                                  final JobDetail jobDetail) {
        final QuartzScheduledJob annotation = annotatedJob.getAnnotation();
        final Class<? extends Job> jobClass = annotatedJob.getJobClass();
        final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

        final String cronExpression = resolveCronExpression(annotation);
        final boolean cronEnabled = cronExpression != null;
        final boolean fixedRateEnabled = annotation.fixedRate() > 0;

        if (cronEnabled == fixedRateEnabled) {
            throw new IllegalStateException("Exactly one of 'cronExpression', 'fixedRate' is required. Offending class "
                    + jobClass.getName());
        }

        if (cronEnabled) {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                    .inTimeZone(Constants.DEFAULT_TIMEZONE.toTimeZone()));

        } else {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMilliseconds(annotation.fixedRate())
                    .repeatForever());
        }

        return triggerBuilder.forJob(jobDetail)
                .startNow()
                .withIdentity(jobDetail.getKey().getName() + "_trigger", jobDetail.getKey().getGroup() + "_triggers")
                .build();
    }

    private String resolveCronExpression(QuartzScheduledJob annotation) {
        if (StringUtils.hasText(annotation.cronExpression())) {
            return embeddedValueResolver.resolveStringValue(annotation.cronExpression());
        }

        return null;
    }
}
