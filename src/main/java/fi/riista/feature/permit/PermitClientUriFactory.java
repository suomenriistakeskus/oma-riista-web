package fi.riista.feature.permit;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;

@Component
public class PermitClientUriFactory {

    public static URI getRelativePermitInvoiceReceiptUri(final long permitId, final long invoiceId) {
        return URI.create("/#/permitmanagement/" + permitId + "/payments/receipt/" + invoiceId);
    }

    public static URI getRelativePermitDuePaymentListUri(final long permitId) {
        return URI.create("/#/permitmanagement/" + permitId + "/payments/list-due");
    }

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    public URI getAbsolutePermitDashboardUri(final HarvestPermit harvestPermit) {
        return getAbsolutePermitDashboardUri(harvestPermit.getId());
    }

    public URI getAbsolutePermitDashboardUri(final long harvestPermitId) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/")
                .fragment("permitmanagement/{id}/dashboard")
                .buildAndExpand(harvestPermitId)
                .toUri();
    }

    public URI getAbsoluteAnonymousDecisionUri(final PermitDecisionRevisionReceiver receiver) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/api/v1/anon/decision/receiver/pdf/download/{uuid}")
                .buildAndExpand(receiver.getUuid().toString())
                .toUri();
    }
}
