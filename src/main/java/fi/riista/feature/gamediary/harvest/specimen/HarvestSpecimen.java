package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.NumberUtils;
import fi.riista.util.jpa.CriteriaUtils;

import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
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

import java.util.Objects;
import java.util.stream.Stream;

@Entity
@Access(AccessType.FIELD)
public class HarvestSpecimen extends LifecycleEntity<Long> implements HasMooseFields {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "harvest_id", nullable = false)
    private Harvest harvest;

    @Column
    @Enumerated(EnumType.STRING)
    private GameGender gender;

    @Column
    @Enumerated(EnumType.STRING)
    private GameAge age;

    @Column
    private Double weight;

    @Column
    private Double weightEstimated;

    @Column
    private Double weightMeasured;

    @Enumerated(EnumType.STRING)
    @Column
    private GameFitnessClass fitnessClass;

    @Enumerated(EnumType.STRING)
    @Column
    private GameAntlersType antlersType;

    @Min(0)
    @Max(999)
    @Column
    private Integer antlersWidth;

    @Min(0)
    @Max(50)
    @Column
    private Integer antlerPointsLeft;

    @Min(0)
    @Max(50)
    @Column
    private Integer antlerPointsRight;

    @Column
    private Boolean notEdible;

    @Column(columnDefinition = "text")
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    // Default constructor for Hibernate
    HarvestSpecimen() {
    }

    public HarvestSpecimen(final Harvest harvest) {
        setHarvest(harvest);
    }

    public HarvestSpecimen(final Harvest harvest, final GameAge age, final GameGender gender, final Double weight) {
        this(harvest);

        setAge(age);
        setGender(gender);
        setWeight(weight);
    }

    /**
     * Checks whether the object given as parameter has equal content. ID and
     * revision fields are not included in comparison.
     */
    public boolean hasEqualContent(@Nonnull final HarvestSpecimen that) {
        Objects.requireNonNull(that);

        return HasMooseFields.super.hasEqualMooseFields(that) &&
                getGender() == that.getGender() &&
                getAge() == that.getAge() &&
                NumberUtils.equal(getWeight(), that.getWeight());
    }

    public void clearContent() {
        clearMooseFields();
        setAge(null);
        setGender(null);
        setWeight(null);
    }

    @AssertTrue(message = "{HarvestSpecimen.weightOutOfAcceptableLimits}")
    public boolean isWeightWithinAcceptableLimits() {
        return Stream.of(weight, weightEstimated, weightMeasured).noneMatch(w -> w != null && (w < 0.0 || w > 999.99));
    }

    public void checkAllMandatoryFieldsPresentWithinClubHunting(final int gameSpeciesCode) {
        final MandatoryHarvestSpecimenFieldMissingWithinClubHuntingException.Builder builder =
                new MandatoryHarvestSpecimenFieldMissingWithinClubHuntingException.Builder();

        builder.validateAge(age).validateGender(gender);

        if (GameSpecies.isMoose(gameSpeciesCode)) {
            builder.validateWeight(getWeightEstimated(), getWeightMeasured())
                    .validateFitnessClass(fitnessClass)
                    .validateNotEdible(notEdible);

            if (age == GameAge.ADULT && gender == GameGender.MALE) {
                builder.validateAntlersType(antlersType)
                        .validateAntlersWidth(antlersWidth)
                        .validateAntlerPointsLeft(antlerPointsLeft)
                        .validateAntlerPointsRight(antlerPointsRight);
            }
        }

        builder.throwOnMissingFields();
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_specimen_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Harvest getHarvest() {
        return harvest;
    }

    public void setHarvest(Harvest harvest) {
        CriteriaUtils.updateInverseCollection(Harvest_.specimens, this, this.harvest, harvest);
        this.harvest = harvest;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(GameGender gender) {
        this.gender = gender;
    }

    public GameAge getAge() {
        return age;
    }

    public void setAge(GameAge age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public Double getWeightEstimated() {
        return weightEstimated;
    }

    @Override
    public void setWeightEstimated(Double weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    @Override
    public Double getWeightMeasured() {
        return weightMeasured;
    }

    @Override
    public void setWeightMeasured(Double weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    @Override
    public GameFitnessClass getFitnessClass() {
        return fitnessClass;
    }

    @Override
    public void setFitnessClass(GameFitnessClass fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    @Override
    public GameAntlersType getAntlersType() {
        return antlersType;
    }

    @Override
    public void setAntlersType(GameAntlersType antlersType) {
        this.antlersType = antlersType;
    }

    @Override
    public Integer getAntlersWidth() {
        return antlersWidth;
    }

    @Override
    public void setAntlersWidth(Integer antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    @Override
    public Integer getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    @Override
    public void setAntlerPointsLeft(Integer antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    @Override
    public Integer getAntlerPointsRight() {
        return antlerPointsRight;
    }

    @Override
    public void setAntlerPointsRight(Integer antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    @Override
    public Boolean getNotEdible() {
        return notEdible;
    }

    @Override
    public void setNotEdible(Boolean notEatable) {
        this.notEdible = notEatable;
    }

    @Override
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
