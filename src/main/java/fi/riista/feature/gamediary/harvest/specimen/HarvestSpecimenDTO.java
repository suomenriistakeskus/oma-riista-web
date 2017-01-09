package fi.riista.feature.gamediary.harvest.specimen;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameDiaryEntrySpecimenDTO;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.F;

import javax.validation.constraints.AssertTrue;

import java.util.stream.Stream;

public class HarvestSpecimenDTO extends GameDiaryEntrySpecimenDTO implements HasMooseFields {

    private GameAge age;

    private Double weight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double weightEstimated;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double weightMeasured;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GameFitnessClass fitnessClass;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GameAntlersType antlersType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlersWidth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlerPointsLeft;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlerPointsRight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean notEdible;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String additionalInfo;

    public HarvestSpecimenDTO() {
    }

    public HarvestSpecimenDTO(final GameGender gender, final GameAge age, final Double weight) {
        super(gender);
        setAge(age);
        setWeight(weight);
    }

    @Override
    public boolean allBusinessFieldsNull() {
        return super.allBusinessFieldsNull() && F.allNull(
                getAge(), getWeight(), getWeightEstimated(), getWeightMeasured(), getFitnessClass(), getAntlersType(),
                getAntlersWidth(), getAntlerPointsLeft(), getAntlerPointsRight(), getNotEdible(), getAdditionalInfo());
    }

    @Override
    public void clearBusinessFields() {
        super.clearBusinessFields();
        setAge(null);
        setWeight(null);
        clearMooseFields();
    }

    @AssertTrue(message = "{HarvestSpecimenDTO.weightOutOfAcceptableLimits}")
    public boolean isWeightWithinAcceptableLimits() {
        return Stream.of(weight, weightEstimated, weightMeasured).noneMatch(w -> w != null && (w < 0.0 || w > 999.99));
    }

    // Accessors -->

    public GameAge getAge() {
        return age;
    }

    public void setAge(final GameAge age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

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
    public Boolean getNotEdible() {
        return notEdible;
    }

    @Override
    public void setNotEdible(final Boolean notEdible) {
        this.notEdible = notEdible;
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
