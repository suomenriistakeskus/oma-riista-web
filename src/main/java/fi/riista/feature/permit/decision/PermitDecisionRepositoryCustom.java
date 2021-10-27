package fi.riista.feature.permit.decision;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.invoice.Invoice;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PermitDecisionRepositoryCustom {
    List<String> findCancelledAndIgnoredPermitNumbersByOriginalPermit(HarvestPermit originalPermit);

    Map<Invoice, PermitDecision> findByInvoiceIn(Collection<Invoice> invoices);
}
