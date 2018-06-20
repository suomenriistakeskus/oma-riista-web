package fi.riista.feature.harvestpermit.download;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class HarvestPermitDownloadDecisionDTO {
    private final String filename;
    private final String remoteUri;
    private final UUID localDecisionFileId;

    public HarvestPermitDownloadDecisionDTO(final String filename, final UUID localDecisionFileId) {
        this.filename = requireNonNull(filename);
        this.localDecisionFileId = requireNonNull(localDecisionFileId);
        this.remoteUri = null;
    }

    public HarvestPermitDownloadDecisionDTO(final String filename, final String remoteUri) {
        this.filename = requireNonNull(filename);
        this.remoteUri = requireNonNull(remoteUri);
        this.localDecisionFileId = null;
    }

    public String getFilename() {
        return filename;
    }

    public String getRemoteUri() {
        return remoteUri;
    }

    public UUID getLocalDecisionFileId() {
        return localDecisionFileId;
    }
}
