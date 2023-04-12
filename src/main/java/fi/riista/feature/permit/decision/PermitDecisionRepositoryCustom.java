package fi.riista.feature.permit.decision;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.invoice.Invoice;
import org.joda.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PermitDecisionRepositoryCustom {
    List<String> findCancelledAndIgnoredPermitNumbersByOriginalPermit(HarvestPermit originalPermit);

    Map<Invoice, PermitDecision> findByInvoiceIn(Collection<Invoice> invoices);

    List<PermitDecision> findByHuntingYearAndSpeciesAndCategory(int huntingYear, GameSpecies species, HarvestPermitCategory category);

    List<PermitDecision> findByTypeCodeAndScheduledPublishingAfter(Collection<String> permitTypeCodes, LocalDateTime dateAfter);
}
