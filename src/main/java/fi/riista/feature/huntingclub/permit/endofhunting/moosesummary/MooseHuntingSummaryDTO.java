package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class MooseHuntingSummaryDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    private long clubId;

    private long harvestPermitId;

    private int permitAreaSize;

    private LocalDate beginDate;

    private LocalDate endDate;

    private LocalDate huntingEndDate;

    private boolean huntingFinished;

    @Min(0)
    private Integer totalHuntingArea;

    @Min(0)
    private Integer effectiveHuntingArea;

    private Double effectiveHuntingAreaPercentage;

    @Min(0)
    private Integer remainingPopulationInTotalArea;

    @Min(0)
    private Integer remainingPopulationInEffectiveArea;

    private MooseHuntingAreaType huntingAreaType;

    @Min(0)
    private Integer numberOfDrownedMooses;

    @Min(0)
    private Integer numberOfMoosesKilledByBear;

    @Min(0)
    private Integer numberOfMoosesKilledByWolf;

    @Min(0)
    private Integer numberOfMoosesKilledInTrafficAccident;

    @Min(0)
    private Integer numberOfMoosesKilledByPoaching;

    @Min(0)
    private Integer numberOfMoosesKilledInRutFight;

    @Min(0)
    private Integer numberOfStarvedMooses;

    @Min(0)
    private Integer numberOfMoosesDeceasedByOtherReason;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Size(max = 255)
    private String causeOfDeath;

    @Valid
    private SpeciesEstimatedAppearance whiteTailedDeerAppearance;

    @Valid
    private SpeciesEstimatedAppearance roeDeerAppearance;

    @Valid
    private SpeciesEstimatedAppearance wildForestReindeerAppearance;

    @Valid
    private SpeciesEstimatedAppearance fallowDeerAppearance;

    @Valid
    private SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance;

    @Valid
    private BeaverAppearance beaverAppearance;

    private LocalDate mooseHeatBeginDate;

    private LocalDate mooseHeatEndDate;

    private LocalDate mooseFawnBeginDate;

    private LocalDate mooseFawnEndDate;

    private Boolean deerFliesAppeared;

    private LocalDate dateOfFirstDeerFlySeen;

    private LocalDate dateOfLastDeerFlySeen;

    @Min(0)
    private Integer numberOfAdultMoosesHavingFlies;

    @Min(0)
    private Integer numberOfYoungMoosesHavingFlies;

    private TrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth;

    private Boolean observationPolicyAdhered;

    private boolean locked;

    @AssertTrue
    public boolean isAllRequiredFieldsForFinalSubmit() {
        return !huntingFinished ||
                F.allNotNull(observationPolicyAdhered, whiteTailedDeerAppearance,
                        roeDeerAppearance, wildForestReindeerAppearance, fallowDeerAppearance, wildBoarAppearance,
                        beaverAppearance
                ) && F.allNotNull(
                        whiteTailedDeerAppearance.getAppeared(),
                        roeDeerAppearance.getAppeared(),
                        wildForestReindeerAppearance.getAppeared(),
                        fallowDeerAppearance.getAppeared(),
                        wildBoarAppearance.getAppeared(),
                        beaverAppearance.getAppeared()
                );
    }

    @AssertTrue
    public boolean isEffectiveHuntingAreaGivenUnambiguously() {
        return F.anyNull(effectiveHuntingArea, effectiveHuntingAreaPercentage);
    }

    @AssertTrue
    public boolean isHuntingAreaAndRemainingPopulationPresentWhenHuntingFinished() {
        return !huntingFinished ||
                totalHuntingArea != null && remainingPopulationInTotalArea != null ||
                remainingPopulationInEffectiveArea != null &&
                        (effectiveHuntingArea != null ||
                                totalHuntingArea != null && effectiveHuntingAreaPercentage != null);
    }

    @AssertTrue
    public boolean isDeerFlyDataGivenCorrectly() {
        return !Boolean.FALSE.equals(deerFliesAppeared) || F.allNull(
                dateOfFirstDeerFlySeen, dateOfLastDeerFlySeen, numberOfAdultMoosesHavingFlies,
                numberOfAdultMoosesHavingFlies, trendOfDeerFlyPopulationGrowth);
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(final long huntingClubId) {
        this.clubId = huntingClubId;
    }

    public long getHarvestPermitId() {
        return harvestPermitId;
    }

    public void setHarvestPermitId(final long harvestPermitId) {
        this.harvestPermitId = harvestPermitId;
    }

    public int getPermitAreaSize() {
        return permitAreaSize;
    }

    public void setPermitAreaSize(int permitAreaSize) {
        this.permitAreaSize = permitAreaSize;
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

    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    public void setHuntingEndDate(final LocalDate huntingEndDate) {
        this.huntingEndDate = huntingEndDate;
    }

    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    public void setHuntingFinished(final boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    public Integer getTotalHuntingArea() {
        return totalHuntingArea;
    }

    public void setTotalHuntingArea(final Integer totalHuntingArea) {
        this.totalHuntingArea = totalHuntingArea;
    }

    public Integer getEffectiveHuntingArea() {
        return effectiveHuntingArea;
    }

    public void setEffectiveHuntingArea(final Integer effectiveHuntingArea) {
        this.effectiveHuntingArea = effectiveHuntingArea;
    }

    public Double getEffectiveHuntingAreaPercentage() {
        return effectiveHuntingAreaPercentage;
    }

    public void setEffectiveHuntingAreaPercentage(final Double effectiveHuntingAreaPercentage) {
        this.effectiveHuntingAreaPercentage = effectiveHuntingAreaPercentage;
    }

    public Integer getRemainingPopulationInTotalArea() {
        return remainingPopulationInTotalArea;
    }

    public void setRemainingPopulationInTotalArea(final Integer remainingPopulationInTotalArea) {
        this.remainingPopulationInTotalArea = remainingPopulationInTotalArea;
    }

    public Integer getRemainingPopulationInEffectiveArea() {
        return remainingPopulationInEffectiveArea;
    }

    public void setRemainingPopulationInEffectiveArea(final Integer remainingPopulationInEffectiveArea) {
        this.remainingPopulationInEffectiveArea = remainingPopulationInEffectiveArea;
    }

    public MooseHuntingAreaType getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(final MooseHuntingAreaType huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public Integer getNumberOfDrownedMooses() {
        return numberOfDrownedMooses;
    }

    public void setNumberOfDrownedMooses(final Integer numberOfDrownedMooses) {
        this.numberOfDrownedMooses = numberOfDrownedMooses;
    }

    public Integer getNumberOfMoosesKilledByBear() {
        return numberOfMoosesKilledByBear;
    }

    public void setNumberOfMoosesKilledByBear(final Integer numberOfMoosesKilledByBear) {
        this.numberOfMoosesKilledByBear = numberOfMoosesKilledByBear;
    }

    public Integer getNumberOfMoosesKilledByWolf() {
        return numberOfMoosesKilledByWolf;
    }

    public void setNumberOfMoosesKilledByWolf(final Integer numberOfMoosesKilledByWolf) {
        this.numberOfMoosesKilledByWolf = numberOfMoosesKilledByWolf;
    }

    public Integer getNumberOfMoosesKilledInTrafficAccident() {
        return numberOfMoosesKilledInTrafficAccident;
    }

    public void setNumberOfMoosesKilledInTrafficAccident(final Integer numberOfMoosesKilledInTrafficAccident) {
        this.numberOfMoosesKilledInTrafficAccident = numberOfMoosesKilledInTrafficAccident;
    }

    public Integer getNumberOfMoosesKilledByPoaching() {
        return numberOfMoosesKilledByPoaching;
    }

    public void setNumberOfMoosesKilledByPoaching(final Integer numberOfMoosesKilledByPoaching) {
        this.numberOfMoosesKilledByPoaching = numberOfMoosesKilledByPoaching;
    }

    public Integer getNumberOfMoosesKilledInRutFight() {
        return numberOfMoosesKilledInRutFight;
    }

    public void setNumberOfMoosesKilledInRutFight(final Integer numberOfMoosesKilledInRutFight) {
        this.numberOfMoosesKilledInRutFight = numberOfMoosesKilledInRutFight;
    }

    public Integer getNumberOfStarvedMooses() {
        return numberOfStarvedMooses;
    }

    public void setNumberOfStarvedMooses(final Integer numberOfStarvedMooses) {
        this.numberOfStarvedMooses = numberOfStarvedMooses;
    }

    public Integer getNumberOfMoosesDeceasedByOtherReason() {
        return numberOfMoosesDeceasedByOtherReason;
    }

    public void setNumberOfMoosesDeceasedByOtherReason(final Integer numberOfMoosesDeceasedByOtherReason) {
        this.numberOfMoosesDeceasedByOtherReason = numberOfMoosesDeceasedByOtherReason;
    }

    public String getCauseOfDeath() {
        return causeOfDeath;
    }

    public void setCauseOfDeath(final String causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }

    public SpeciesEstimatedAppearance getWhiteTailedDeerAppearance() {
        return whiteTailedDeerAppearance;
    }

    public void setWhiteTailedDeerAppearance(final SpeciesEstimatedAppearance whiteTailedDeerAppearance) {
        this.whiteTailedDeerAppearance = whiteTailedDeerAppearance;
    }

    public SpeciesEstimatedAppearance getRoeDeerAppearance() {
        return roeDeerAppearance;
    }

    public void setRoeDeerAppearance(final SpeciesEstimatedAppearance roeDeerAppearance) {
        this.roeDeerAppearance = roeDeerAppearance;
    }

    public SpeciesEstimatedAppearance getWildForestReindeerAppearance() {
        return wildForestReindeerAppearance;
    }

    public void setWildForestReindeerAppearance(final SpeciesEstimatedAppearance wildForestReindeerAppearance) {
        this.wildForestReindeerAppearance = wildForestReindeerAppearance;
    }

    public SpeciesEstimatedAppearance getFallowDeerAppearance() {
        return fallowDeerAppearance;
    }

    public void setFallowDeerAppearance(final SpeciesEstimatedAppearance fallowDeerAppearance) {
        this.fallowDeerAppearance = fallowDeerAppearance;
    }

    public SpeciesEstimatedAppearanceWithPiglets getWildBoarAppearance() {
        return wildBoarAppearance;
    }

    public void setWildBoarAppearance(final SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance) {
        this.wildBoarAppearance = wildBoarAppearance;
    }

    public BeaverAppearance getBeaverAppearance() {
        return beaverAppearance;
    }

    public void setBeaverAppearance(final BeaverAppearance beaverAppearance) {
        this.beaverAppearance = beaverAppearance;
    }

    public LocalDate getMooseHeatBeginDate() {
        return mooseHeatBeginDate;
    }

    public void setMooseHeatBeginDate(final LocalDate mooseHeatBeginDate) {
        this.mooseHeatBeginDate = mooseHeatBeginDate;
    }

    public LocalDate getMooseHeatEndDate() {
        return mooseHeatEndDate;
    }

    public void setMooseHeatEndDate(final LocalDate mooseHeatEndDate) {
        this.mooseHeatEndDate = mooseHeatEndDate;
    }

    public LocalDate getMooseFawnBeginDate() {
        return mooseFawnBeginDate;
    }

    public void setMooseFawnBeginDate(final LocalDate mooseFawnBeginDate) {
        this.mooseFawnBeginDate = mooseFawnBeginDate;
    }

    public LocalDate getMooseFawnEndDate() {
        return mooseFawnEndDate;
    }

    public void setMooseFawnEndDate(final LocalDate mooseFawnEndDate) {
        this.mooseFawnEndDate = mooseFawnEndDate;
    }

    public Boolean getDeerFliesAppeared() {
        return deerFliesAppeared;
    }

    public void setDeerFliesAppeared(final Boolean deerFliesAppeared) {
        this.deerFliesAppeared = deerFliesAppeared;
    }

    public LocalDate getDateOfFirstDeerFlySeen() {
        return dateOfFirstDeerFlySeen;
    }

    public void setDateOfFirstDeerFlySeen(final LocalDate dateOfFirstDeerFlySeen) {
        this.dateOfFirstDeerFlySeen = dateOfFirstDeerFlySeen;
    }

    public LocalDate getDateOfLastDeerFlySeen() {
        return dateOfLastDeerFlySeen;
    }

    public void setDateOfLastDeerFlySeen(final LocalDate dateOfLastDeerFlySeen) {
        this.dateOfLastDeerFlySeen = dateOfLastDeerFlySeen;
    }

    public Integer getNumberOfAdultMoosesHavingFlies() {
        return numberOfAdultMoosesHavingFlies;
    }

    public void setNumberOfAdultMoosesHavingFlies(final Integer numberOfAdultMoosesHavingFlies) {
        this.numberOfAdultMoosesHavingFlies = numberOfAdultMoosesHavingFlies;
    }

    public Integer getNumberOfYoungMoosesHavingFlies() {
        return numberOfYoungMoosesHavingFlies;
    }

    public void setNumberOfYoungMoosesHavingFlies(final Integer numberOfYoungMoosesHavingFlies) {
        this.numberOfYoungMoosesHavingFlies = numberOfYoungMoosesHavingFlies;
    }

    public TrendOfPopulationGrowth getTrendOfDeerFlyPopulationGrowth() {
        return trendOfDeerFlyPopulationGrowth;
    }

    public void setTrendOfDeerFlyPopulationGrowth(final TrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth) {
        this.trendOfDeerFlyPopulationGrowth = trendOfDeerFlyPopulationGrowth;
    }

    public Boolean getObservationPolicyAdhered() {
        return observationPolicyAdhered;
    }

    public void setObservationPolicyAdhered(final Boolean observationPolicyAdhered) {
        this.observationPolicyAdhered = observationPolicyAdhered;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }
}
