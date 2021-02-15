package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class DogEventDisturbance extends LifecycleEntity<Long> implements HasBeginAndEndDate {

    public static final String ID_COLUMN_NAME = "dog_event_disturbance_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true, nullable = false)
    private HarvestPermitApplication harvestPermitApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private GameSpecies gameSpecies;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DogEventType eventType;

    @Column(nullable = false)
    private boolean skipped;

    @Min(1)
    @Max(9999)
    @Column
    private Integer dogsAmount;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String eventDescription;

    // Additional constraints

    @AssertTrue(message="Begin date is missing")
    public boolean isBeginDateValid() {
        return skipped || beginDate != null;
    }

    @AssertTrue(message="End date is missing")
    public boolean isEndDateValid() {
        return skipped || endDate != null;
    }

    @AssertTrue(message="Event description is missing")
    public boolean isEventDescriptionValid() {
        return skipped || !StringUtils.isBlank(eventDescription);
    }

    @AssertTrue(message="Species is missing")
    public boolean isGameSpeciesValid() {
        return skipped || gameSpecies != null;
    }

    @AssertTrue(message="Dogs amount is missing")
    public boolean isDogsAmountValid() {
        return skipped || dogsAmount != null;
    }

    @AssertTrue(message="Invalid fields for skipped event")
    public boolean isSkippedValid() {
        return !skipped || (beginDate == null && endDate == null && eventDescription == null && gameSpecies == null && dogsAmount == null);
    }

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

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public DogEventType getEventType() {
        return eventType;
    }

    public void setEventType(final DogEventType eventType) {
        this.eventType = eventType;
    }

    public Boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(final Boolean skipped) {
        this.skipped = skipped;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(final GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public Integer getDogsAmount() {
        return dogsAmount;
    }

    public void setDogsAmount(final Integer dogsAmount) {
        this.dogsAmount = dogsAmount;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate startDate) {
        this.beginDate = startDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(final String eventDescription) {
        this.eventDescription = eventDescription;
    }

}
