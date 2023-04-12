package fi.riista.feature.permit.application.schedule;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestPermitApplicationSchedule extends BaseEntity<Long> {

    private Long id;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private HarvestPermitCategory category;

    @Column
    private DateTime beginTime;

    @Column
    private DateTime endTime;

    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String instructionsFi;

    @Column(columnDefinition = "TEXT")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String instructionsSv;

    @Column
    private Boolean activeOverride;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_application_schedule_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitCategory getCategory() {
        return category;
    }

    public void setCategory(final HarvestPermitCategory category) {
        this.category = category;
    }

    public DateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(final DateTime beginTime) {
        this.beginTime = beginTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final DateTime endTime) {
        this.endTime = endTime;
    }

    public String getInstructionsFi() {
        return instructionsFi;
    }

    public void setInstructionsFi(final String instructionsFi) {
        this.instructionsFi = instructionsFi;
    }

    public String getInstructionsSv() {
        return instructionsSv;
    }

    public void setInstructionsSv(final String instructionsSv) {
        this.instructionsSv = instructionsSv;
    }

    public Boolean getActiveOverride() {
        return activeOverride;
    }

    public void setActiveOverride(final Boolean activeOverride) {
        this.activeOverride = activeOverride;
    }
}
