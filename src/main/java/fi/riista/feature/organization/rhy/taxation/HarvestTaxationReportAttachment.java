package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.jpa.CriteriaUtils;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestTaxationReportAttachment extends LifecycleEntity<Long> {


    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestTaxationReport harvestTaxationReport;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attachment_metadata_id", unique = true, nullable = false)
    private PersistentFileMetadata fileMetadata;


    public HarvestTaxationReportAttachment() {
    }

    public HarvestTaxationReportAttachment(final HarvestTaxationReport harvestTaxationReport, final PersistentFileMetadata fileMetadata) {
        this.harvestTaxationReport = harvestTaxationReport;
        this.fileMetadata = fileMetadata;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "harvest_taxation_report_attachment_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestTaxationReport getHarvestTaxationReport() {
        return harvestTaxationReport;
    }

    public void setHarvestTaxationReport(final HarvestTaxationReport harvestTaxationReport) {
        CriteriaUtils.updateInverseCollection(HarvestTaxationReport_.attachments, this, this.harvestTaxationReport,harvestTaxationReport);
        this.harvestTaxationReport = harvestTaxationReport;
    }

    public PersistentFileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(final PersistentFileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }
}
