package fi.riista.feature.permit.decision.publish;

import java.net.URI;

public class PermitDecisionAttachmentLinkDescriptionDTO {

    private final URI uri;
    private final String description;

    public PermitDecisionAttachmentLinkDescriptionDTO(final URI uri, final String description) {
        this.uri = uri;
        this.description = description;
    }

    public URI getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }
}
