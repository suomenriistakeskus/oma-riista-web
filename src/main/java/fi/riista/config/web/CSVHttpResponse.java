package fi.riista.config.web;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class CSVHttpResponse {
    private final String filename;
    private final String[] headerRow;
    private final List<String[]> rows;

    public CSVHttpResponse(String filename, String[] headerRow, List<String[]> rows) {
        this.filename = Objects.requireNonNull(filename);
        this.headerRow = headerRow;
        this.rows = Objects.requireNonNull(rows);
    }

    public String getFilename() {
        return filename;
    }

    public String[] getHeaderRow() {
        return headerRow;
    }

    public List<String[]> getRows() {
        return rows;
    }

    /**
     * Override to change charset used in response encoding.
     *
     * @return null if default charset is to be used, otherwise returns the charset to be in response encoding.
     */
    public Charset getCharset() {
        return null;
    }
}
