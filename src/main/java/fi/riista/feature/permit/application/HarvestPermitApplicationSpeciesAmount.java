package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

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
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplicationSpeciesAmount extends LifecycleEntity<Long> implements HasBeginAndEndDate {
    private static final int MAX_SPECIES_AMOUNT = 100_000;
    private static final int MIN_SPECIES_AMOUNT = 0;

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

    @Column(nullable = false)
    private float amount;

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

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String populationAmount;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String populationDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String forbiddenMethodJustification;

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
    public void setId(Long id) {
        this.id = id;
    }

    public HarvestPermitApplicationSpeciesAmount() {
        super();
    }

    public HarvestPermitApplicationSpeciesAmount(final HarvestPermitApplication harvestPermitApplication,
                                                 final GameSpecies gameSpecies,
                                                 final float amount) {
        this.harvestPermitApplication = harvestPermitApplication;
        this.gameSpecies = gameSpecies;
        this.amount = amount;
    }

    @AssertTrue
    public boolean isValidAmount() {
        return amount >= MIN_SPECIES_AMOUNT && amount < MAX_SPECIES_AMOUNT;
    }

    @AssertTrue
    public boolean isJustificationPresentWhenForbiddenMethodsUsed() {
        return forbiddenMethodsUsed == null ||
                (!forbiddenMethodsUsed || StringUtils.hasText(forbiddenMethodJustification));
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

    public float getAmount() {
        return amount;
    }

    public void setAmount(final float amount) {
        this.amount = amount;
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

    public void setForbiddenMethodJustification(String forbiddenMethodJustification) {
        this.forbiddenMethodJustification = forbiddenMethodJustification;
    }

    public Boolean isForbiddenMethodsUsed() {
        return forbiddenMethodsUsed;
    }

    public void setForbiddenMethodsUsed(Boolean forbiddenMethodsUsed) {
        this.forbiddenMethodsUsed = forbiddenMethodsUsed;
    }
}
