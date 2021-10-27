package fi.riista.feature.permit.zip;

import static java.util.Objects.requireNonNull;

public class OmaRiistaDecisionAttachmentsZip {
    private final byte[] data;
    private final String filename;

    public OmaRiistaDecisionAttachmentsZip(final byte[] data, final String filename) {
        this.data = requireNonNull(data);
        this.filename = requireNonNull(filename);
    }

    public byte[] getData() {
        return data;
    }

    public String getFilename() {
        return filename;
    }
}
