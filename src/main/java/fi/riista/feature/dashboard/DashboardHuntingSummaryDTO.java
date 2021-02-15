package fi.riista.feature.dashboard;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.BeaverAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingAreaType;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearance;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.SpeciesEstimatedAppearanceWithPiglets;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.TrendOfPopulationGrowth;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class DashboardHuntingSummaryDTO implements HasID<Long> {

    public static DashboardHuntingSummaryDTO from(@Nonnull final MooseHuntingSummary mooseHuntingSummary,
                                                  @Nonnull final String permitNumber,
                                                  final long permitId,
                                                  final LocalisedString clubName,
                                                  final GeoLocation clubLocation) {
        final DashboardHuntingSummaryDTO dto = new DashboardHuntingSummaryDTO();
        dto.setPermitId(permitId);
        dto.setPermitNumber(requireNonNull(permitNumber));
        dto.setClubName(clubName);
        dto.setClubLatitude(ofNullable(clubLocation).map(GeoLocation::getLatitude).orElse(null));
        dto.setClubLongitude(ofNullable(clubLocation).map(GeoLocation::getLongitude).orElse(null));
        dto.setHuntingEndDate(mooseHuntingSummary.getHuntingEndDate());
        dto.setHuntingFinished(mooseHuntingSummary.isHuntingFinished());
        dto.setHuntingAreaType(mooseHuntingSummary.getHuntingAreaType());

        final AreaSizeAndRemainingPopulation ap = mooseHuntingSummary.getAreaSizeAndPopulation();
        dto.setTotalHuntingArea(ap.getTotalHuntingArea());
        dto.setEffectiveHuntingArea(ap.getEffectiveHuntingArea());
        dto.setEffectiveHuntingAreaPercentage(mooseHuntingSummary.getEffectiveHuntingAreaPercentage());
        dto.setRemainingPopulationInTotalArea(ap.getRemainingPopulationInTotalArea());
        dto.setRemainingPopulationInEffectiveArea(ap.getRemainingPopulationInEffectiveArea());

        dto.setNumberOfDrownedMooses(mooseHuntingSummary.getNumberOfDrownedMooses());
        dto.setNumberOfMoosesKilledByBear(mooseHuntingSummary.getNumberOfMoosesKilledByBear());
        dto.setNumberOfMoosesKilledByWolf(mooseHuntingSummary.getNumberOfMoosesKilledByWolf());
        dto.setNumberOfMoosesKilledInTrafficAccident(mooseHuntingSummary.getNumberOfMoosesKilledInTrafficAccident());
        dto.setNumberOfMoosesKilledByPoaching(mooseHuntingSummary.getNumberOfMoosesKilledByPoaching());
        dto.setNumberOfMoosesKilledInRutFight(mooseHuntingSummary.getNumberOfMoosesKilledInRutFight());
        dto.setNumberOfStarvedMooses(mooseHuntingSummary.getNumberOfStarvedMooses());
        dto.setNumberOfMoosesDeceasedByOtherReason(mooseHuntingSummary.getNumberOfMoosesDeceasedByOtherReason());
        dto.setCauseOfDeath(mooseHuntingSummary.getCauseOfDeath());

        dto.setWhiteTailedDeerAppearance(mooseHuntingSummary.getWhiteTailedDeerAppearance());
        dto.setRoeDeerAppearance(mooseHuntingSummary.getRoeDeerAppearance());
        dto.setWildForestReindeerAppearance(mooseHuntingSummary.getWildForestReindeerAppearance());
        dto.setFallowDeerAppearance(mooseHuntingSummary.getFallowDeerAppearance());
        dto.setWildBoarAppearance(mooseHuntingSummary.getWildBoarAppearance());
        dto.setBeaverAppearance(mooseHuntingSummary.getBeaverAppearance());

        dto.setMooseHeatBeginDate(mooseHuntingSummary.getMooseHeatBeginDate());
        dto.setMooseHeatEndDate(mooseHuntingSummary.getMooseHeatEndDate());
        dto.setMooseFawnBeginDate(mooseHuntingSummary.getMooseFawnBeginDate());
        dto.setMooseFawnEndDate(mooseHuntingSummary.getMooseFawnEndDate());

        dto.setDeerFliesAppeared(mooseHuntingSummary.getDeerFliesAppeared());
        dto.setDateOfFirstDeerFlySeen(mooseHuntingSummary.getDateOfFirstDeerFlySeen());
        dto.setDateOfLastDeerFlySeen(mooseHuntingSummary.getDateOfLastDeerFlySeen());
        dto.setNumberOfAdultMoosesHavingFlies(mooseHuntingSummary.getNumberOfAdultMoosesHavingFlies());
        dto.setNumberOfYoungMoosesHavingFlies(mooseHuntingSummary.getNumberOfYoungMoosesHavingFlies());
        dto.setTrendOfDeerFlyPopulationGrowth(mooseHuntingSummary.getTrendOfDeerFlyPopulationGrowth());

        dto.setObservationPolicyAdhered(mooseHuntingSummary.getObservationPolicyAdhered());

        return dto;
    }

    private long permitId;
    private String permitNumber;
    private LocalisedString clubName;
    private Integer clubLatitude;
    private Integer clubLongitude;
    private LocalDate huntingEndDate;
    private boolean huntingFinished;
    private Integer totalHuntingArea;
    private Integer effectiveHuntingArea;
    private Double effectiveHuntingAreaPercentage;
    private Integer remainingPopulationInTotalArea;
    private Integer remainingPopulationInEffectiveArea;
    private MooseHuntingAreaType huntingAreaType;

    private Integer numberOfDrownedMooses;
    private Integer numberOfMoosesKilledByBear;
    private Integer numberOfMoosesKilledByWolf;
    private Integer numberOfMoosesKilledInTrafficAccident;
    private Integer numberOfMoosesKilledByPoaching;
    private Integer numberOfMoosesKilledInRutFight;
    private Integer numberOfStarvedMooses;
    private Integer numberOfMoosesDeceasedByOtherReason;
    private String causeOfDeath;

    private SpeciesEstimatedAppearance whiteTailedDeerAppearance;
    private SpeciesEstimatedAppearance roeDeerAppearance;
    private SpeciesEstimatedAppearance wildForestReindeerAppearance;
    private SpeciesEstimatedAppearance fallowDeerAppearance;
    private SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance;
    private BeaverAppearance beaverAppearance;

    private LocalDate mooseHeatBeginDate;
    private LocalDate mooseHeatEndDate;
    private LocalDate mooseFawnBeginDate;
    private LocalDate mooseFawnEndDate;
    private Boolean deerFliesAppeared;
    private LocalDate dateOfFirstDeerFlySeen;
    private LocalDate dateOfLastDeerFlySeen;
    private Integer numberOfAdultMoosesHavingFlies;
    private Integer numberOfYoungMoosesHavingFlies;
    private TrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth;
    private Boolean observationPolicyAdhered;

    public long getPermitId() {
        return permitId;
    }

    public void setPermitId(final long permitId) {
        this.permitId = permitId;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public LocalisedString getClubName() {
        return clubName;
    }

    public void setClubName(final LocalisedString clubName) {
        this.clubName = clubName;
    }


    public Integer getClubLatitude() {
        return clubLatitude;
    }

    public void setClubLatitude(final Integer clubLatitude) {
        this.clubLatitude = clubLatitude;
    }

    public Integer getClubLongitude() {
        return clubLongitude;
    }

    public void setClubLongitude(final Integer clubLongitude) {
        this.clubLongitude = clubLongitude;
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

    @Override
    public Long getId() {
        return permitId;
    }
}
