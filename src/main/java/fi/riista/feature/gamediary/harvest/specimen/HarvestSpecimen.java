package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest_;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.SafeHtml;

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
import java.util.stream.Stream;

@Entity
@Access(AccessType.FIELD)
public class HarvestSpecimen extends LifecycleEntity<Long> implements HarvestSpecimenBusinessFields {

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

    // Arvioitu teuraspaino
    @Column
    private Double weightEstimated;

    // Punnittu teuraspaino
    @Column
    private Double weightMeasured;

    @Enumerated(EnumType.STRING)
    @Column
    private GameFitnessClass fitnessClass;

    // Yksi tai molemmat sarvet pudonneet
    @Column
    private Boolean antlersLost;

    @Enumerated(EnumType.STRING)
    @Column
    private GameAntlersType antlersType;

    // Sarvien kärkiväli (cm)
    @Min(0)
    @Max(200)
    @Column
    private Integer antlersWidth;

    @Min(0)
    @Max(30)
    @Column
    private Integer antlerPointsLeft;

    @Min(0)
    @Max(30)
    @Column
    private Integer antlerPointsRight;

    // Sarvien tyven ympärysmitta (cm)
    @Min(0)
    @Max(50)
    @Column
    private Integer antlersGirth;

    // Sarven pituus (cm)
    @Min(0)
    @Max(100)
    @Column
    private Integer antlersLength;

    // Sarvien sisäleveys (cm)
    @Min(0)
    @Max(100)
    @Column
    private Integer antlersInnerWidth;

    // Sarvirungon leveys (cm)
    @Min(0)
    @Max(10)
    @Column
    private Integer antlerShaftWidth;

    @Column
    private Boolean notEdible;

    // Not accompanying mother. Relevant to only calves/juveniles
    @Column
    private Boolean alone;

    // Lisätietoja (esim. sarvet pudonneet, loiset, sairaidet, petojen raatelujäljet ...)
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

    @AssertTrue(message = "{HarvestSpecimen.weightOutOfAcceptableLimits}")
    public boolean isWeightWithinAcceptableLimits() {
        return Stream.of(weight, weightEstimated, weightMeasured).noneMatch(w -> w != null && (w < 0.0 || w > 999.99));
    }

    @AssertTrue
    public boolean isValueForSomeBusinessFieldGiven() {
        return !allBusinessFieldsNull();
    }

    // This method was moved from HarvestSpecimenBusinessFields interface because of a bug in iOS app v2.4.1.1.
    @AssertTrue
    public boolean isAllAntlerFieldsNullWhenNotAdultMale() {
        return isAdultMale() || allAntlerFieldsNull();
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
    public void setId(final Long id) {
        this.id = id;
    }

    public Harvest getHarvest() {
        return harvest;
    }

    public void setHarvest(final Harvest harvest) {
        CriteriaUtils.updateInverseCollection(Harvest_.specimens, this, this.harvest, harvest);
        this.harvest = harvest;
    }

    @Override
    public GameGender getGender() {
        return gender;
    }

    @Override
    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

    @Override
    public GameAge getAge() {
        return age;
    }

    @Override
    public void setAge(final GameAge age) {
        this.age = age;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    @Override
    public Double getWeightEstimated() {
        return weightEstimated;
    }

    @Override
    public void setWeightEstimated(final Double weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    @Override
    public Double getWeightMeasured() {
        return weightMeasured;
    }

    @Override
    public void setWeightMeasured(final Double weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    @Override
    public GameFitnessClass getFitnessClass() {
        return fitnessClass;
    }

    @Override
    public void setFitnessClass(final GameFitnessClass fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    @Override
    public Boolean getAntlersLost() {
        return antlersLost;
    }

    @Override
    public void setAntlersLost(final Boolean antlersLost) {
        this.antlersLost = antlersLost;
    }

    @Override
    public GameAntlersType getAntlersType() {
        return antlersType;
    }

    @Override
    public void setAntlersType(final GameAntlersType antlersType) {
        this.antlersType = antlersType;
    }

    @Override
    public Integer getAntlersWidth() {
        return antlersWidth;
    }

    @Override
    public void setAntlersWidth(final Integer antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    @Override
    public Integer getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    @Override
    public void setAntlerPointsLeft(final Integer antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    @Override
    public Integer getAntlerPointsRight() {
        return antlerPointsRight;
    }

    @Override
    public void setAntlerPointsRight(final Integer antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    @Override
    public Integer getAntlersGirth() {
        return antlersGirth;
    }

    @Override
    public void setAntlersGirth(final Integer antlersGirth) {
        this.antlersGirth = antlersGirth;
    }

    @Override
    public Integer getAntlersLength() {
        return antlersLength;
    }

    @Override
    public void setAntlersLength(final Integer antlersLength) {
        this.antlersLength = antlersLength;
    }

    @Override
    public Integer getAntlersInnerWidth() {
        return antlersInnerWidth;
    }

    @Override
    public void setAntlersInnerWidth(final Integer antlersInnerWidth) {
        this.antlersInnerWidth = antlersInnerWidth;
    }

    @Override
    public Integer getAntlerShaftWidth() {
        return antlerShaftWidth;
    }

    @Override
    public void setAntlerShaftWidth(final Integer antlerShaftWidth) {
        this.antlerShaftWidth = antlerShaftWidth;
    }

    @Override
    public Boolean getNotEdible() {
        return notEdible;
    }

    @Override
    public void setNotEdible(final Boolean notEdible) {
        this.notEdible = notEdible;
    }

    @Override
    public Boolean getAlone() {
        return alone;
    }

    @Override
    public void setAlone(final Boolean alone) {
        this.alone = alone;
    }

    @Override
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
