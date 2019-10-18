package fi.riista.feature.permit.invoice.harvest;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.permit.invoice.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PermitHarvestInvoiceRepository
        extends JpaRepository<PermitHarvestInvoice, Long>, PermitHarvestInvoiceRepositoryCustom {

    Optional<PermitHarvestInvoice> findBySpeciesAmount(HarvestPermitSpeciesAmount speciesAmount);

    Optional<PermitHarvestInvoice> findByInvoice(Invoice invoice);

    @Query("SELECT count(spa.id) " +
            "FROM PermitHarvestInvoice phi " +
            "JOIN phi.speciesAmount spa " +
            "WHERE spa.harvestPermit = ?1 and phi.invoice = ?2")
    long countByInvoiceAndHarvestPermit(HarvestPermit harvestPermit, Invoice invoice);

}
