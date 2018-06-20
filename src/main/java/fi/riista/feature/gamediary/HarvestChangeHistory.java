package fi.riista.feature.gamediary;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import org.joda.time.DateTime;

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

@Entity
@Access(value = AccessType.FIELD)
public class HarvestChangeHistory extends BaseEntity<Long> {

    // Create dummy object for creationTime-based comparison purposes during sorting
    public static HarvestChangeHistory withCreationTime(final DateTime timestamp) {
        HarvestChangeHistory history = new HarvestChangeHistory();
        history.setPointOfTime(timestamp);
        return history;
    }

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Harvest harvest;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @Column
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column
    private HarvestReportState harvestReportState;

    @Column(columnDefinition = "text")
    private String reasonForChange;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_change_history_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public Harvest getHarvest() {
        return harvest;
    }

    public void setHarvest(final Harvest harvest) {
        this.harvest = harvest;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public void setHarvestReportState(final HarvestReportState state) {
        this.harvestReportState = state;
    }

    public String getReasonForChange() {
        return reasonForChange;
    }

    public void setReasonForChange(final String reasonForChange) {
        this.reasonForChange = reasonForChange;
    }
}
