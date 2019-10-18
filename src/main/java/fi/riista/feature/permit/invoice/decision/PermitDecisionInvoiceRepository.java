package fi.riista.feature.permit.invoice.decision;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PermitDecisionInvoiceRepository
        extends BaseRepository<PermitDecisionInvoice, Long>, PermitDecisionInvoiceRepositoryCustom {

    Optional<PermitDecisionInvoice> findByDecision(PermitDecision decision);

    Optional<PermitDecisionInvoice> findByInvoice(Invoice invoice);

    @Query("SELECT count(p.id) FROM PermitDecisionInvoice p WHERE p.decision = ?1 and p.invoice = ?2")
    long countByInvoiceAndDecision(PermitDecision decision, Invoice invoice);

    default PermitDecisionInvoice getByDecision(final PermitDecision decision) {
        return findByDecision(decision).orElseThrow(NotFoundException::new);
    }
}
