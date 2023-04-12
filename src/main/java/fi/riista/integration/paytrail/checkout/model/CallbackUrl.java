package fi.riista.integration.paytrail.checkout.model;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class CallbackUrl {

    @NotNull
    private URI success;

    @NotNull
    private URI cancel;

    public URI getSuccess() {
        return success;
    }

    public void setSuccess(final URI success) {
        this.success = success;
    }

    public URI getCancel() {
        return cancel;
    }

    public void setCancel(final URI cancel) {
        this.cancel = cancel;
    }
}
