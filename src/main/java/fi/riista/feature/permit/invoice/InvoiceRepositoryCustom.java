package fi.riista.feature.permit.invoice;

import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;

import java.util.List;

public interface InvoiceRepositoryCustom {

    Invoice getInvoice(PermitDecision permitDecision, long invoiceId);

    List<Invoice> findElectronicInvoices(PermitDecision permitDecision, InvoiceState invoiceState);

    List<Invoice> search(InvoiceSearchFilterDTO dto);

    List<Invoice> search(InvoiceSearchFilterDTO dto, int searchResultLimit);

}
