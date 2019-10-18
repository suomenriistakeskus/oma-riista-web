package fi.riista.feature.permit.invoice.harvest;

import java.util.Map;
import java.util.Set;

public interface PermitHarvestInvoiceRepositoryCustom {

    Map<Long, InvoicePaymentAmountsDTO> getMooselikeHarvestInvoicePaymentAmounts(final Set<Long> permitIds,
                                                                                 final int speciesCode);
}
