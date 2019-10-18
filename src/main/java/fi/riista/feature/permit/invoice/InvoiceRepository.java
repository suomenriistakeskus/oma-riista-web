package fi.riista.feature.permit.invoice;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends BaseRepository<Invoice, Long>, InvoiceRepositoryCustom {

    @Override
    @Query("SELECT invoice FROM #{#entityName} invoice JOIN FETCH invoice.recipientAddress")
    List<Invoice> findAll();

    Optional<Invoice> findByInvoiceNumber(int invoiceNumber);

    @Query("SELECT o FROM #{#entityName} o WHERE o.creditorReference IN (:refs) ORDER BY o.invoiceDate, o.id")
    List<Invoice> findByCreditorReferences(@Param("refs") Collection<CreditorReference> creditorReference);

}
