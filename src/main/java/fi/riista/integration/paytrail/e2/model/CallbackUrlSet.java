package fi.riista.integration.paytrail.e2.model;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class CallbackUrlSet {
    @NotNull
    private URI successUri;

    @NotNull
    private URI cancelUri;

    private URI notifyUri;

    public URI getSuccessUri() {
        return successUri;
    }

    public void setSuccessUri(final URI successUri) {
        this.successUri = successUri;
    }

    public URI getCancelUri() {
        return cancelUri;
    }

    public void setCancelUri(final URI cancelUri) {
        this.cancelUri = cancelUri;
    }

    public URI getNotifyUri() {
        return notifyUri;
    }

    public void setNotifyUri(final URI notifyUri) {
        this.notifyUri = notifyUri;
    }
}
