package fi.riista.feature.permit.application.conflict;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationConflictPalsta extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication firstApplication;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication secondApplication;

    @NotNull
    @Column(nullable = false)
    private Integer palstaId;

    @NotNull
    @Column(nullable = false)
    private Long palstaTunnus;

    @Size(max = 255)
    @Column
    private String palstaNimi;

    @Column(nullable = false)
    private boolean metsahallitus;

    @Column
    private Double conflictAreaSize;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_application_conflict_palsta_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermitApplication getFirstApplication() {
        return firstApplication;
    }

    public void setFirstApplication(final HarvestPermitApplication firstApplication) {
        this.firstApplication = firstApplication;
    }

    public HarvestPermitApplication getSecondApplication() {
        return secondApplication;
    }

    public void setSecondApplication(final HarvestPermitApplication secondApplication) {
        this.secondApplication = secondApplication;
    }

    public Integer getPalstaId() {
        return palstaId;
    }

    public void setPalstaId(final Integer palstaId) {
        this.palstaId = palstaId;
    }

    public Long getPalstaTunnus() {
        return palstaTunnus;
    }

    public void setPalstaTunnus(final Long palstaTunnus) {
        this.palstaTunnus = palstaTunnus;
    }

    public String getPalstaNimi() {
        return palstaNimi;
    }

    public void setPalstaNimi(final String palstaNimi) {
        this.palstaNimi = palstaNimi;
    }

    public boolean isMetsahallitus() {
        return metsahallitus;
    }

    public void setMetsahallitus(final boolean metsahallitus) {
        this.metsahallitus = metsahallitus;
    }

    public Double getConflictAreaSize() {
        return conflictAreaSize;
    }

    public void setConflictAreaSize(final Double conflictAreaSize) {
        this.conflictAreaSize = conflictAreaSize;
    }
}
