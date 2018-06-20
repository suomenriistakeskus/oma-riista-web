package fi.riista.feature.permit.invoice;

import java.util.List;

public interface PermitDecisionInvoiceRepositoryCustom {

    List<PermitDecisionInvoice> getPermitDecisionInvoicesForNextFivaldiBatch();
}
