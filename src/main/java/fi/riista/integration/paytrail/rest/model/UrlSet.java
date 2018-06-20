package fi.riista.integration.paytrail.rest.model;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "success",
        "failure",
        "pending",
        "notification"
})
public class UrlSet {

    @NotNull
    @XmlElement(name = "success", required = true)
    private URI success;

    @NotNull
    @XmlElement(name = "failure", required = true)
    private URI failure;

    @XmlElement(name = "pending")
    private URI pending;

    @NotNull
    @XmlElement(name = "notification", required = true)
    private URI notification;

    public URI getSuccess() {
        return success;
    }

    public void setSuccess(final URI success) {
        this.success = success;
    }

    public URI getFailure() {
        return failure;
    }

    public void setFailure(final URI failure) {
        this.failure = failure;
    }

    public URI getPending() {
        return pending;
    }

    public void setPending(final URI pending) {
        this.pending = pending;
    }

    public URI getNotification() {
        return notification;
    }

    public void setNotification(final URI notification) {
        this.notification = notification;
    }
}
