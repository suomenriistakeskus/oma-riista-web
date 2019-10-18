package fi.riista.feature.permit.invoice.decision;

import java.util.List;

public interface PermitDecisionInvoiceRepositoryCustom {

    List<PermitDecisionInvoice> getPermitDecisionInvoicesForNextFivaldiBatch();
}
