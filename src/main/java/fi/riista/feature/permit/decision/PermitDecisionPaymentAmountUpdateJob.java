package fi.riista.feature.permit.decision;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Set;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "PermitDecisionPaymentAmountUpdateJob",
        enabledProperty = "permitdecision.paymentamount.update.enabled",
        cronExpression = "${permitdecision.paymentamount.update.schedule}")
public class PermitDecisionPaymentAmountUpdateJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(PermitDecisionPaymentAmountUpdateJob.class);

    @Resource
    private PermitDecisionPaymentAmountUpdateFeature feature;

    @Override
    public void executeAsAdmin() {
        LOG.info("Updating permit decision payment amount");

        final Set<Long> decisionIds = feature.updateNewPayments();

        LOG.info("Updated decisions: " + decisionIds);
    }
}
