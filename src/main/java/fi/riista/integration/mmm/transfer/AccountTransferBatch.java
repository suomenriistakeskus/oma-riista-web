package fi.riista.integration.mmm.transfer;

import fi.riista.feature.common.entity.LifecycleEntity;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;

/**
 * MMM:n tietyn päivän tiliotteen pohjalta luotu tilisiirtoerä
 */
@Entity
@Access(AccessType.FIELD)
public class AccountTransferBatch extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private LocalDate statementDate;

    // May be null but must be unique
    @Size(max = 255)
    @Column(unique = true)
    private String filename;

    // May be null but must be unique when combined with fileNumber
    @Column
    private LocalDate filenameDate;

    @Column
    private Integer fileNumber;

    // For Hibernate
    AccountTransferBatch() {
    }

    public AccountTransferBatch(@Nonnull final LocalDate statementDate,
                                @Nullable final String filename,
                                @Nullable final LocalDate filenameDate) {

        this(statementDate, filename, filenameDate, 0);
    }

    public AccountTransferBatch(@Nonnull final LocalDate statementDate,
                                @Nullable final String filename,
                                @Nullable final LocalDate filenameDate,
                                @Nonnull final Integer fileNumber) {

        this.statementDate = requireNonNull(statementDate);
        this.filename = filename;
        this.filenameDate = filenameDate;
        this.fileNumber = requireNonNull(fileNumber);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_transfer_batch_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
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

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(final LocalDate statementDate) {
        this.statementDate = statementDate;
    }

    public Integer getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(final Integer fileNumber) {
        this.fileNumber = fileNumber;
    }
}
