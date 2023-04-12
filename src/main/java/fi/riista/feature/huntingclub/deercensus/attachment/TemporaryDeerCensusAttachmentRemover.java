package fi.riista.feature.huntingclub.deercensus.attachment;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class TemporaryDeerCensusAttachmentRemover {

    private static final Logger LOG = LoggerFactory.getLogger(TemporaryDeerCensusAttachmentRemover.class);

    @Resource
    private DeerCensusAttachmentRepository deerCensusAttachmentRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Transactional
    public void removeExpiredTemporaryImages(final ReadablePeriod expirationTime) {
        final DateTime olderThan = DateTime.now().minus(expirationTime);
        int deletedCount = deerCensusAttachmentRepository.deleteAllWithoutDeerCensusByCreationTime(olderThan);

        if (deletedCount > 0) {
            LOG.debug("Deleted DeerCensusAttachments: " + deletedCount);
        }
    }

}
