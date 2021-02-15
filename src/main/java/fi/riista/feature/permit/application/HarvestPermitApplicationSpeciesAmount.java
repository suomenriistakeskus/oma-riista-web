package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static fi.riista.util.F.mapNullable;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationSpeciesAmount extends LifecycleEntity<Long> implements HasBeginAndEndDate {
    private static final int MAX_AMOUNT_VALUE = 100_000;
    private static final int MIN_AMOUNT_VALUE = 0;

    public static boolean checkValidityYears(final int validityYears, final boolean limitlessPermitAllowed) {
        return validityYears == 0 && limitlessPermitAllowed || validityYears >= 1 && validityYears <= 5;
    }

    private Long id;

    @NotNull
    @JoinColumn(name = "harvest_permit_application_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GameSpecies gameSpecies;

    @Column(name = "amount")
    private Float specimenAmount;

    @Min(1)
    @Max(MAX_AMOUNT_VALUE)
    @Column
    private Integer nestAmount;

    @Min(1)
    @Max(MAX_AMOUNT_VALUE)
    @Column
    private Integer eggAmount;

    @Min(1)
    @Max(MAX_AMOUNT_VALUE)
    @Column
    private Integer constructionAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String mooselikeDescription;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    // Bird application validity years
    // 0 = indefinite
    @Min(0)
    @Max(5)
    @Column
    private Integer validityYears;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String additionalPeriodInfo;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    @Column
    private Integer causedDamageAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String causedDamageDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String evictionMeasureDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String evictionMeasureEffect;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String populationAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String populationDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String forbiddenMethodJustification;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String subSpeciesName;

    @Column
    private Boolean forbiddenMethodsUsed;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_permit_application_species_amount_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitApplicationSpeciesAmount() {
        super();
    }

    public static HarvestPermitApplicationSpeciesAmount createForHarvest(final HarvestPermitApplication harvestPermitApplication,
                                                                         final GameSpecies gameSpecies,
                                                                         final float amount) {
        final HarvestPermitApplicationSpeciesAmount spa =
                new HarvestPermitApplicationSpeciesAmount(harvestPermitApplication, gameSpecies);
        spa.setSpecimenAmount(amount);
        return spa;
    }

    public static HarvestPermitApplicationSpeciesAmount createForNestRemoval(final HarvestPermitApplication harvestPermitApplication,
                                                                             final GameSpecies gameSpecies,
                                                                             final Integer nestAmount,
                                                                             final Integer eggAmount,
                                                                             final Integer constructionAmount) {
        final HarvestPermitApplicationSpeciesAmount spa =
                new HarvestPermitApplicationSpeciesAmount(harvestPermitApplication, gameSpecies);
        spa.setNestAmount(nestAmount);
        spa.setEggAmount(eggAmount);
        spa.setConstructionAmount(constructionAmount);
        return spa;
    }

    public static HarvestPermitApplicationSpeciesAmount createWithSpecimenOrEggs(final @Nonnull HarvestPermitApplication harvestPermitApplication,
                                                                                 final @Nonnull GameSpecies gameSpecies,
                                                                                 final @Nullable Integer specimenAmount,
                                                                                 final @Nullable Integer eggAmount,
                                                                                 final @Nullable String subSpeciesName) {
        requireNonNull(harvestPermitApplication);
        requireNonNull(gameSpecies);

        final HarvestPermitApplicationSpeciesAmount spa =
                new HarvestPermitApplicationSpeciesAmount(harvestPermitApplication, gameSpecies);
        spa.setSpecimenAmount(mapNullable(specimenAmount, Float::valueOf));
        spa.setEggAmount(eggAmount);
        spa.setSubSpeciesName(subSpeciesName);
        return spa;
    }

    private HarvestPermitApplicationSpeciesAmount(final HarvestPermitApplication harvestPermitApplication,
                                                  final GameSpecies gameSpecies) {
        this.harvestPermitApplication = harvestPermitApplication;
        this.gameSpecies = gameSpecies;
    }

    @AssertTrue
    public boolean isAmountPresent() {
        return F.anyNonNull(specimenAmount, nestAmount, eggAmount, constructionAmount);
    }

    @AssertTrue
    public boolean isValidAmount() {
        // Check specimen amount separately, other amount values are handled by Min/Max annotations
        return specimenAmount == null || (specimenAmount >= MIN_AMOUNT_VALUE && specimenAmount < MAX_AMOUNT_VALUE);
    }

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication application) {
        this.harvestPermitApplication = application;
    }

    public GameSpecies getGameSpecies() {
        return gameSpecies;
    }

    public void setGameSpecies(final GameSpecies gameSpecies) {
        this.gameSpecies = gameSpecies;
    }

    public Float getSpecimenAmount() {
        return specimenAmount;
    }

    public void setSpecimenAmount(final Float harvestAmount) {
        this.specimenAmount = harvestAmount;
    }

    public Integer getNestAmount() {
        return nestAmount;
    }

    public void setNestAmount(final Integer nestAmount) {
        this.nestAmount = nestAmount;
    }

    public Integer getEggAmount() {
        return eggAmount;
    }

    public void setEggAmount(final Integer eggAmount) {
        this.eggAmount = eggAmount;
    }

    public Integer getConstructionAmount() {
        return constructionAmount;
    }

    public void setConstructionAmount(final Integer constructionAmount) {
        this.constructionAmount = constructionAmount;
    }

    public String getMooselikeDescription() {
        return mooselikeDescription;
    }

    public void setMooselikeDescription(final String description) {
        this.mooselikeDescription = description;
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

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }

    public String getAdditionalPeriodInfo() {
        return additionalPeriodInfo;
    }

    public void setAdditionalPeriodInfo(final String additionalPeriodInfo) {
        this.additionalPeriodInfo = additionalPeriodInfo;
    }

    public Integer getCausedDamageAmount() {
        return causedDamageAmount;
    }

    public void setCausedDamageAmount(final Integer causedDamageAmount) {
        this.causedDamageAmount = causedDamageAmount;
    }

    public String getCausedDamageDescription() {
        return causedDamageDescription;
    }

    public void setCausedDamageDescription(final String causedDamageDescription) {
        this.causedDamageDescription = causedDamageDescription;
    }

    public String getEvictionMeasureDescription() {
        return evictionMeasureDescription;
    }

    public void setEvictionMeasureDescription(final String evictionMeasureDescription) {
        this.evictionMeasureDescription = evictionMeasureDescription;
    }

    public String getEvictionMeasureEffect() {
        return evictionMeasureEffect;
    }

    public void setEvictionMeasureEffect(final String evictionMeasureEffect) {
        this.evictionMeasureEffect = evictionMeasureEffect;
    }

    public String getPopulationAmount() {
        return populationAmount;
    }

    public void setPopulationAmount(final String populationAmount) {
        this.populationAmount = populationAmount;
    }

    public String getPopulationDescription() {
        return populationDescription;
    }

    public void setPopulationDescription(final String populationDescription) {
        this.populationDescription = populationDescription;
    }

    public String getForbiddenMethodJustification() {
        return forbiddenMethodJustification;
    }

    public void setForbiddenMethodJustification(final String forbiddenMethodJustification) {
        this.forbiddenMethodJustification = forbiddenMethodJustification;
    }

    public String getSubSpeciesName() {
        return subSpeciesName;
    }

    public void setSubSpeciesName(final String subSpeciesName) {
        this.subSpeciesName = subSpeciesName;
    }

    public Boolean isForbiddenMethodsUsed() {
        return forbiddenMethodsUsed;
    }

    public void setForbiddenMethodsUsed(final Boolean forbiddenMethodsUsed) {
        this.forbiddenMethodsUsed = forbiddenMethodsUsed;
    }
}
