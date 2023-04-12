package fi.riista.feature.permit.invoice;

import fi.riista.feature.common.entity.CreditorReference;
import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends BaseRepository<Invoice, Long>, InvoiceRepositoryCustom {

    @Override
    @Query("SELECT invoice FROM #{#entityName} invoice JOIN FETCH invoice.recipientAddress")
    List<Invoice> findAll();

    Optional<Invoice> findByInvoiceNumber(int invoiceNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT o FROM #{#entityName} o where o.invoiceNumber = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumberLocking(@Param("invoiceNumber") int invoiceNumber);

    @Query("SELECT o FROM #{#entityName} o WHERE o.creditorReference IN (:refs) ORDER BY o.invoiceDate, o.id")
    List<Invoice> findByCreditorReferences(@Param("refs") Collection<CreditorReference> creditorReference);



}
