package fi.riista.feature.permit.invoice.batch;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Pyyntilupapäätösten käsittelymaksuerä muodostuu myyntireskontraan (Fivaldi)
 * vietävistä laskuista/myyntisaamisista.
 */
@Entity
@Access(AccessType.FIELD)
public class PermitDecisionInvoiceBatch extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_metadata_id", unique = true, nullable = false)
    private PersistentFileMetadata fivaldiAccountsReceivableFile;

    // Indicates whether the accounts receivable file of this batch has been downloaded at least once.
    @Column(nullable = false)
    private boolean downloaded;

    // For Hibernate
    PermitDecisionInvoiceBatch() {
    }

    public PermitDecisionInvoiceBatch(final PersistentFileMetadata fivaldiAccountsReceivableFile) {
        this.fivaldiAccountsReceivableFile = requireNonNull(fivaldiAccountsReceivableFile);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_invoice_batch_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public PersistentFileMetadata getFivaldiAccountsReceivableFile() {
        return fivaldiAccountsReceivableFile;
    }

    public void setFivaldiAccountsReceivableFile(final PersistentFileMetadata fileMetadata) {
        this.fivaldiAccountsReceivableFile = fileMetadata;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(final boolean downloaded) {
        this.downloaded = downloaded;
    }
}
