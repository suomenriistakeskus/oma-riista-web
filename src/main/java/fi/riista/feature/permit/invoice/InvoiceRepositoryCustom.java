package fi.riista.feature.permit.invoice;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.invoice.search.InvoiceSearchFilterDTO;

import java.util.EnumSet;
import java.util.List;

public interface InvoiceRepositoryCustom {

    default List<Invoice> findElectronicInvoices(final HarvestPermit harvestPermit, final InvoiceState invoiceState) {
        return findElectronicInvoices(harvestPermit, EnumSet.allOf(InvoiceType.class), invoiceState);
    }

    List<Invoice> findElectronicInvoices(HarvestPermit harvestPermit,
                                         EnumSet<InvoiceType> invoiceTypes,
                                         InvoiceState invoiceState);

    List<Invoice> findHarvestInvoicesHavingInitiatedOrConfirmedPayments(HarvestPermit harvestPermit);

    List<Invoice> search(InvoiceSearchFilterDTO dto);

    List<Invoice> search(InvoiceSearchFilterDTO dto, int searchResultLimit);

}
