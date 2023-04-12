package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.config.quartz.QuartzScheduledJob;
import fi.riista.config.quartz.RunAsAdminJob;
import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@DisallowConcurrentExecution
@QuartzScheduledJob(
        name = "InvalidateOldInformationRequestLinksJob",
        enabledProperty = "permit.decision.informationrequest.link_invalidation.enabled",
        cronExpression = "${permit.decision.informationrequest.link_invalidation.schedule}"
)
public class InvalidateOldInformationRequestLinksJob extends RunAsAdminJob {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidateOldInformationRequestLinksJob.class);

    @Resource
    private InformationRequestLinkRepository informationRequestLinkRepository;

    @Resource
    private PermitDecisionInformationRequestFeature permitDecisionInformationRequestFeature;

    @Override
    public void executeAsAdmin() {
        LOG.info("Seek outdated information request links...");
        informationRequestLinkRepository.findAll().stream().filter(link -> link.getValidUntil().isBefore(DateTime.now())).forEach(outdatedLink -> {
            LOG.info("Remove recipient data from outdated link. Id={}", outdatedLink.getId());
            permitDecisionInformationRequestFeature.clearLinkRecipientData(outdatedLink);
            informationRequestLinkRepository.saveAndFlush(outdatedLink);
        });

        LOG.info("Done information request link invalidation");
    }
}
