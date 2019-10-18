package fi.riista.feature.permit;

import fi.riista.feature.RuntimeEnvironmentUtil;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevisionReceiver;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

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
        if (harvestPermit.getOriginalPermit() != null) {
            return getAbsolutePermitDashboardUri(harvestPermit.getOriginalPermit().getId());
        }
        return getAbsolutePermitDashboardUri(harvestPermit.getId());
    }

    public URI getAbsolutePermitDashboardUri(final long harvestPermitId) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/")
                .fragment("/permitmanagement/{id}/dashboard")
                .buildAndExpand(harvestPermitId)
                .toUri();
    }

    public URI getAbsoluteAnonymousDecisionUri(final PermitDecisionRevisionReceiver receiver) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/api/v1/anon/decision/receiver/pdf/download/{uuid}")
                .buildAndExpand(receiver.getUuid().toString())
                .toUri();
    }

    public URI getAbsoluteAnonymousApplicationUri(final UUID archiveUuid) {
        Objects.requireNonNull(archiveUuid);

        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/api/v1/anon/application/zip/{uuid}")
                .buildAndExpand(archiveUuid.toString())
                .toUri();
    }

    public URI getAbsoluteClubPermitUri(final long harvestPermitId, final long clubId) {
        return UriComponentsBuilder.fromUri(runtimeEnvironmentUtil.getBackendBaseUri())
                .replacePath("/")
                .fragment("/club/{clubId}/permit/{harvestPermitId}/show")
                .buildAndExpand(clubId, harvestPermitId)
                .toUri();
    }
}
