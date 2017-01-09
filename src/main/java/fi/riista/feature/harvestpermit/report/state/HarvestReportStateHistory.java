package fi.riista.feature.harvestpermit.report.state;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.report.HarvestReport;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Access(AccessType.FIELD)
public class HarvestReportStateHistory extends LifecycleEntity<Long> {

    // Create dummy object for creationTime-based comparison purposes during sorting
    public static HarvestReportStateHistory withCreationTime(Date timestamp) {
        HarvestReportStateHistory history = new HarvestReportStateHistory();
        history.getLifecycleFields().setCreationTime(timestamp);
        return history;
    }

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestReport harvestReport;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private HarvestReport.State state;

    @Column(columnDefinition = "text")
    private String message;

    protected HarvestReportStateHistory() {
    }

    public HarvestReportStateHistory(HarvestReport harvestReport, HarvestReport.State state) {
        this.harvestReport = harvestReport;
        this.state = state;
    }

    public HarvestReportStateHistory(HarvestReport harvestReport, HarvestReport.State state, String message) {
        this.harvestReport = harvestReport;
        this.state = state;
        this.message = message;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_report_state_history_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestReport getHarvestReport() {
        return harvestReport;
    }

    public void setHarvestReport(HarvestReport harvestReport) {
        this.harvestReport = harvestReport;
    }

    public HarvestReport.State getState() {
        return state;
    }

    public void setState(HarvestReport.State state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
