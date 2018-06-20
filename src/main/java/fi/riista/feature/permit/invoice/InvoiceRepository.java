package fi.riista.feature.permit.invoice;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends BaseRepository<Invoice, Long>, InvoiceRepositoryCustom {

    @Override
    @Query("SELECT invoice FROM #{#entityName} invoice JOIN FETCH invoice.recipientAddress")
    List<Invoice> findAll();

    Optional<Invoice> findByInvoiceNumber(int invoiceNumber);

}
