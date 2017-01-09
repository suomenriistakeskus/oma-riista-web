package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClub_;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static fi.riista.feature.huntingclub.permit.summary.SpeciesEstimatedAppearance.revamp;

@Entity
@Access(AccessType.FIELD)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "club_id", "harvest_permit_id" }) })
public class MooseHuntingSummary extends LifecycleEntity<Long> implements HasBeginAndEndDate, MutableHuntingEndStatus {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HuntingClub club;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermit harvestPermit;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @Column
    private LocalDate huntingEndDate;

    @Column(nullable = false)
    private boolean huntingFinished;

    @AttributeOverrides({
        @AttributeOverride(
                name = "remainingPopulationInTotalArea",
                column = @Column(name = "mooses_remaining_in_total_hunting_area")),
        @AttributeOverride(
                name = "remainingPopulationInEffectiveArea",
                column = @Column(name = "mooses_remaining_in_effective_hunting_area"))
    })
    @Embedded
    private AreaSizeAndRemainingPopulation areaSizeAndPopulation;

    // Mutually exclusive with effectiveHuntingArea
    @Column(precision = 5, scale = 2)
    private Float effectiveHuntingAreaPercentage;

    @Convert(converter = MooseHuntingAreaTypeConverter.class)
    @Column(length = 1)
    private MooseHuntingAreaType huntingAreaType;

    // Hukkuneiden hirvien määrä
    @Min(0)
    @Column(name = "drowned_mooses")
    private Integer numberOfDrownedMooses;

    // "Karhun tappamat"-lukumäärä
    @Min(0)
    @Column(name = "mooses_killed_by_bear")
    private Integer numberOfMoosesKilledByBear;

    // "Suden tappamia"-lukumäärä
    @Min(0)
    @Column(name = "mooses_killed_by_wolf")
    private Integer numberOfMoosesKilledByWolf;

    // "Kolarissa kuolleet"-lukumäärä
    @Min(0)
    @Column(name = "mooses_killed_in_traffic_accident")
    private Integer numberOfMoosesKilledInTrafficAccident;

    // Salakaadettujen hirvien määrä
    @Min(0)
    @Column(name = "mooses_killed_by_poaching")
    private Integer numberOfMoosesKilledByPoaching;

    // Kiimatappelussa kuolleiden hirvien määrä
    @Min(0)
    @Column(name = "mooses_killed_in_rut_fight")
    private Integer numberOfMoosesKilledInRutFight;

    // Nälkiintyneiden hirvien määrä
    @Min(0)
    @Column(name = "starved_mooses")
    private Integer numberOfStarvedMooses;

    // Muusta syystä kuolleet hirvet
    @Min(0)
    @Column(name = "mooses_deceased_by_other_reason")
    private Integer numberOfMoosesDeceasedByOtherReason;

    // Muu syy (hirvien kuolemille)
    @Size(max = 255)
    @Column(length = 255)
    private String causeOfDeath;

    @AttributeOverrides({
        @AttributeOverride(name = "appeared", column = @Column(name = "white_tailed_deer_appeared")),
        @AttributeOverride(
                name = "trendOfPopulationGrowth", column = @Column(name = "white_tailed_deer_population_growth")),
        @AttributeOverride(
                name = "estimatedAmountOfSpecimens",
                column = @Column(name = "white_tailed_deer_estimated_specimen_amount"))
    })
    @Embedded
    private SpeciesEstimatedAppearance whiteTailedDeerAppearance;

    @AttributeOverrides({
        @AttributeOverride(name = "appeared", column = @Column(name = "roe_deer_appeared")),
        @AttributeOverride(name = "trendOfPopulationGrowth", column = @Column(name = "roe_deer_population_growth")),
        @AttributeOverride(
                name = "estimatedAmountOfSpecimens",
                column = @Column(name = "roe_deer_estimated_specimen_amount"))
    })
    @Embedded
    private SpeciesEstimatedAppearance roeDeerAppearance;

    @AttributeOverrides({
        @AttributeOverride(name = "appeared", column = @Column(name = "wild_forest_reindeer_appeared")),
        @AttributeOverride(
                name = "trendOfPopulationGrowth", column = @Column(name = "wild_forest_reindeer_population_growth")),
        @AttributeOverride(
                name = "estimatedAmountOfSpecimens",
                column = @Column(name = "wild_forest_reindeer_estimated_specimen_amount"))
    })
    @Embedded
    private SpeciesEstimatedAppearance wildForestReindeerAppearance;

    @AttributeOverrides({
        @AttributeOverride(name = "appeared", column = @Column(name = "fallow_deer_appeared")),
        @AttributeOverride(name = "trendOfPopulationGrowth", column = @Column(name = "fallow_deer_population_growth")),
        @AttributeOverride(
                name = "estimatedAmountOfSpecimens",
                column = @Column(name = "fallow_deer_estimated_specimen_amount"))
    })
    @Embedded
    private SpeciesEstimatedAppearance fallowDeerAppearance;

    @AttributeOverrides({
            @AttributeOverride(name = "appeared", column = @Column(name = "wild_boar_appeared")),
            @AttributeOverride(name = "trendOfPopulationGrowth", column = @Column(name = "wild_boar_population_growth")),
            @AttributeOverride(
                    name = "estimatedAmountOfSpecimens",
                    column = @Column(name = "wild_boar_estimated_specimen_amount")),
            @AttributeOverride(
                    name = "estimatedAmountOfSowWithPiglets",
                    column = @Column(name = "wild_boar_estimated_amount_of_sow_with_piglets"))
    })
    @Embedded
    private SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance;

    @Column
    private LocalDate mooseHeatBeginDate;

    @Column
    private LocalDate mooseHeatEndDate;

    @Column
    private LocalDate mooseFawnBeginDate;

    @Column
    private LocalDate mooseFawnEndDate;

    @Column
    private LocalDate dateOfFirstDeerFlySeen;

    @Column
    private LocalDate dateOfLastDeerFlySeen;

    @Min(0)
    @Column
    private Integer numberOfAdultMoosesHavingFlies;

    @Min(0)
    @Column
    private Integer numberOfYoungMoosesHavingFlies;

    // Alueellamme esiintyy hirven täikärpästä?
    @Column
    private Boolean deerFliesAppeared;

    // Hirven täikärpästen määrä vähentynyt?
    @Convert(converter = TrendOfPopulationGrowthConverter.class)
    @Column(name = "deer_fly_population_growth", length = 1)
    private TrendOfPopulationGrowth trendOfDeerFlyPopulationGrowth;

    public MooseHuntingSummary() {
    }

    public MooseHuntingSummary(final HuntingClub club, final HarvestPermit permit) {
        setClub(club);
        setHarvestPermit(permit);
    }

    @Override
    public Long getClubId() {
        return F.getId(club);
    }

    @Override
    public int getGameSpeciesCode() {
        return GameSpecies.OFFICIAL_CODE_MOOSE;
    }

    public ClubHuntingSummaryBasicInfo getBasicInfo() {
        final AreaSizeAndRemainingPopulation ap = getAreaSizeAndPopulation();

        return new ClubHuntingSummaryBasicInfo() {
            @Override
            public HuntingClub getClub() {
                return club;
            }

            @Override
            public int getGameSpeciesCode() {
                return MooseHuntingSummary.this.getGameSpeciesCode();
            }

            @Override
            public LocalDate getHuntingEndDate() {
                return huntingEndDate;
            }

            @Override
            public boolean isHuntingFinished() {
                return huntingFinished;
            }

            @Override
            public Integer getTotalHuntingArea() {
                return ap.getTotalHuntingArea();
            }

            // Returns either stored area size or value calculated from percentage of total area.
            @Override
            public Integer getEffectiveHuntingArea() {
                return ap.getEffectiveHuntingArea();
            }

            @Override
            public Float getEffectiveHuntingAreaPercentage() {
                return effectiveHuntingAreaPercentage;
            }

            @Override
            public Integer getRemainingPopulationInTotalArea() {
                return ap.getRemainingPopulationInTotalArea();
            }

            @Override
            public Integer getRemainingPopulationInEffectiveArea() {
                return ap.getRemainingPopulationInEffectiveArea();
            }
        };
    }

    @AssertTrue
    public boolean isEffectiveHuntingAreaGivenExclusively() {
        return F.anyNull(getAreaSizeAndPopulation().getEffectiveHuntingArea(), effectiveHuntingAreaPercentage);
    }

    @AssertTrue
    public boolean isHuntingAreaAndRemainingPopulationPresentWhenHuntingFinished() {
        return !huntingFinished || isHuntingAreaAndRemainingPopulationPresent();
    }

    public boolean isHuntingAreaAndRemainingPopulationPresent() {
        final AreaSizeAndRemainingPopulation ap = getAreaSizeAndPopulation();

        return ap.getTotalHuntingArea() != null && ap.getRemainingPopulationInTotalArea() != null ||
                ap.getRemainingPopulationInEffectiveArea() != null &&
                        (ap.getEffectiveHuntingArea() != null ||
                                ap.getTotalHuntingArea() != null && effectiveHuntingAreaPercentage != null);
    }

    @AssertTrue
    public boolean isDeerFlyDataGivenCorrectly() {
        return !Boolean.FALSE.equals(deerFliesAppeared) ||
                F.allNull(dateOfFirstDeerFlySeen, dateOfLastDeerFlySeen, numberOfAdultMoosesHavingFlies,
                        numberOfAdultMoosesHavingFlies, trendOfDeerFlyPopulationGrowth);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moose_hunting_summary_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HuntingClub getClub() {
        return club;
    }

    public void setClub(final HuntingClub club) {
        CriteriaUtils.updateInverseCollection(HuntingClub_.mooseHuntingSummaries, this, this.club, club);
        this.club = club;
    }

    public HarvestPermit getHarvestPermit() {
        return harvestPermit;
    }

    public void setHarvestPermit(final HarvestPermit harvestPermit) {
        this.harvestPermit = harvestPermit;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public LocalDate getHuntingEndDate() {
        return huntingEndDate;
    }

    @Override
    public void setHuntingEndDate(final LocalDate huntingEndDate) {
        this.huntingEndDate = huntingEndDate;
    }

    @Override
    public boolean isHuntingFinished() {
        return huntingFinished;
    }

    @Override
    public void setHuntingFinished(final boolean huntingFinished) {
        this.huntingFinished = huntingFinished;
    }

    public AreaSizeAndRemainingPopulation getAreaSizeAndPopulation() {
        if (areaSizeAndPopulation == null) {
            areaSizeAndPopulation = new AreaSizeAndRemainingPopulation();
        }
        return areaSizeAndPopulation;
    }

    public void setAreaSizeAndPopulation(final AreaSizeAndRemainingPopulation areaSizeAndPopulation) {
        this.areaSizeAndPopulation = areaSizeAndPopulation;
    }

    public Float getEffectiveHuntingAreaPercentage() {
        return effectiveHuntingAreaPercentage;
    }

    public void setEffectiveHuntingAreaPercentage(final Float effectiveHuntingAreaPercentage) {
        this.effectiveHuntingAreaPercentage = effectiveHuntingAreaPercentage;
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

    public void setNumberOfDrownedMooses(final Integer drownedMooses) {
        this.numberOfDrownedMooses = drownedMooses;
    }

    public Integer getNumberOfMoosesKilledByBear() {
        return numberOfMoosesKilledByBear;
    }

    public void setNumberOfMoosesKilledByBear(final Integer moosesKilledByBear) {
        this.numberOfMoosesKilledByBear = moosesKilledByBear;
    }

    public Integer getNumberOfMoosesKilledByWolf() {
        return numberOfMoosesKilledByWolf;
    }

    public void setNumberOfMoosesKilledByWolf(final Integer moosesKilledByWolf) {
        this.numberOfMoosesKilledByWolf = moosesKilledByWolf;
    }

    public Integer getNumberOfMoosesKilledInTrafficAccident() {
        return numberOfMoosesKilledInTrafficAccident;
    }

    public void setNumberOfMoosesKilledInTrafficAccident(final Integer moosesKilledInTrafficAccident) {
        this.numberOfMoosesKilledInTrafficAccident = moosesKilledInTrafficAccident;
    }

    public Integer getNumberOfMoosesKilledByPoaching() {
        return numberOfMoosesKilledByPoaching;
    }

    public void setNumberOfMoosesKilledByPoaching(final Integer moosesKilledByPoaching) {
        this.numberOfMoosesKilledByPoaching = moosesKilledByPoaching;
    }

    public Integer getNumberOfMoosesKilledInRutFight() {
        return numberOfMoosesKilledInRutFight;
    }

    public void setNumberOfMoosesKilledInRutFight(final Integer moosesKilledInRutFight) {
        this.numberOfMoosesKilledInRutFight = moosesKilledInRutFight;
    }

    public Integer getNumberOfStarvedMooses() {
        return numberOfStarvedMooses;
    }

    public void setNumberOfStarvedMooses(final Integer starvedMooses) {
        this.numberOfStarvedMooses = starvedMooses;
    }

    public Integer getNumberOfMoosesDeceasedByOtherReason() {
        return numberOfMoosesDeceasedByOtherReason;
    }

    public void setNumberOfMoosesDeceasedByOtherReason(final Integer moosesKilledByOtherReason) {
        this.numberOfMoosesDeceasedByOtherReason = moosesKilledByOtherReason;
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
        this.whiteTailedDeerAppearance = revamp(whiteTailedDeerAppearance);
    }

    public SpeciesEstimatedAppearance getRoeDeerAppearance() {
        return roeDeerAppearance;
    }

    public void setRoeDeerAppearance(final SpeciesEstimatedAppearance roeDeerAppearance) {
        this.roeDeerAppearance = revamp(roeDeerAppearance);
    }

    public SpeciesEstimatedAppearance getWildForestReindeerAppearance() {
        return wildForestReindeerAppearance;
    }

    public void setWildForestReindeerAppearance(final SpeciesEstimatedAppearance wildForestReindeerAppearance) {
        this.wildForestReindeerAppearance = revamp(wildForestReindeerAppearance);
    }

    public SpeciesEstimatedAppearance getFallowDeerAppearance() {
        return fallowDeerAppearance;
    }

    public void setFallowDeerAppearance(final SpeciesEstimatedAppearance fallowDeerAppearance) {
        this.fallowDeerAppearance = revamp(fallowDeerAppearance);
    }

    public SpeciesEstimatedAppearanceWithPiglets getWildBoarAppearance() {
        return wildBoarAppearance;
    }

    public void setWildBoarAppearance(SpeciesEstimatedAppearanceWithPiglets wildBoarAppearance) {
        this.wildBoarAppearance = SpeciesEstimatedAppearanceWithPiglets.revamp(wildBoarAppearance);
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

    public Boolean getDeerFliesAppeared() {
        return deerFliesAppeared;
    }

    public void setDeerFliesAppeared(final Boolean deerFliesAppeared) {
        this.deerFliesAppeared = deerFliesAppeared;
    }

    public TrendOfPopulationGrowth getTrendOfDeerFlyPopulationGrowth() {
        return trendOfDeerFlyPopulationGrowth;
    }

    public void setTrendOfDeerFlyPopulationGrowth(final TrendOfPopulationGrowth deerFlyPopulationGrowth) {
        this.trendOfDeerFlyPopulationGrowth = deerFlyPopulationGrowth;
    }

}
