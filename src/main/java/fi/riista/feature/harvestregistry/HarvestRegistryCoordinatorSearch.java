package fi.riista.feature.harvestregistry;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

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
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestRegistryCoordinatorSearch extends LifecycleEntity<Long> {

    private static final String ID_COLUMN_NAME = "harvest_registry_coordinator_search_id";

    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private HarvestRegistryCoordinatorSearchReason searchReason;

    @NotNull
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String rhyCode;

    @Column
    private Integer gameSpeciesCode;

    @FinnishHunterNumber
    @Column
    private String hunterNumber;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    // Accessors

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestRegistryCoordinatorSearchReason getSearchReason() {
        return searchReason;
    }

    public void setSearchReason(final HarvestRegistryCoordinatorSearchReason searchReason) {
        this.searchReason = searchReason;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }
}
