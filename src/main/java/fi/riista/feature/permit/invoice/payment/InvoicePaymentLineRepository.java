package fi.riista.feature.permit.invoice.payment;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.util.jpa.JpaGroupingUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface InvoicePaymentLineRepository extends BaseRepository<InvoicePaymentLine, Long> {

    List<InvoicePaymentLine> findByInvoice(Invoice invoice);

    default Map<Invoice, List<InvoicePaymentLine>> findAndGroupByInvoices(final Collection<Invoice> invoices) {
        return JpaGroupingUtils.groupRelations(invoices, InvoicePaymentLine_.invoice, this);
    }
}
