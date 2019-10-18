package fi.riista.integration.mmm.statement;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class AccountStatement {

    private final LocalDate statementDate;
    private final List<AccountStatementLine> lines;

    private String filename;
    private LocalDate filenameDate;

    public AccountStatement(@Nonnull final LocalDate statementDate, @Nonnull final List<AccountStatementLine> lines) {
        this.statementDate = requireNonNull(statementDate, "statementDate is null");
        this.lines = requireNonNull(lines, "lines is null");
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    // Accessors -->

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public List<AccountStatementLine> getLines() {
        return lines;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public LocalDate getFilenameDate() {
        return filenameDate;
    }

    public void setFilenameDate(final LocalDate filenameDate) {
        this.filenameDate = filenameDate;
    }
}
