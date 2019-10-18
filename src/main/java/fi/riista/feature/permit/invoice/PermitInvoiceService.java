package fi.riista.feature.permit.invoice;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.decision.PermitDecisionInvoiceRepository;
import fi.riista.feature.permit.invoice.harvest.PermitHarvestInvoiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

@Component
public class PermitInvoiceService {

    @Resource
    private PermitDecisionInvoiceRepository permitDecisionInvoiceRepository;

    @Resource
    private PermitHarvestInvoiceRepository permitHarvestInvoiceRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertInvoiceAttachedToPermit(final HarvestPermit harvestPermit, final Invoice invoice) {
        final PermitDecision permitDecision = harvestPermit.getPermitDecision();

        if (!isInvoiceForPermitDecision(invoice, permitDecision) && !isInvoiceForHarvestPermit(invoice, harvestPermit)) {
            throw new IllegalArgumentException(String.format("Invoice id=%d is not attached to permit id=%d",
                    invoice.getId(), harvestPermit.getId()));
        }
    }

    private boolean isInvoiceForPermitDecision(final @Nonnull Invoice invoice, final PermitDecision decision) {
        return decision != null && permitDecisionInvoiceRepository.countByInvoiceAndDecision(decision, invoice) > 0;
    }

    private boolean isInvoiceForHarvestPermit(final @Nonnull Invoice invoice, final @Nonnull HarvestPermit harvestPermit) {
        return permitHarvestInvoiceRepository.countByInvoiceAndHarvestPermit(harvestPermit, invoice) > 0;
    }
}
