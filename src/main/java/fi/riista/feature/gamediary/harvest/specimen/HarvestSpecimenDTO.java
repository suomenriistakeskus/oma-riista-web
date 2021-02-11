package fi.riista.feature.gamediary.harvest.specimen;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameDiaryEntrySpecimenDTO;
import fi.riista.feature.gamediary.GameGender;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.stream.Stream;

public class HarvestSpecimenDTO extends GameDiaryEntrySpecimenDTO implements HarvestSpecimenBusinessFields {

    private GameAge age;

    private Double weight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double weightEstimated;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double weightMeasured;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GameFitnessClass fitnessClass;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean antlersLost;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GameAntlersType antlersType;

    @Min(0)
    @Max(200)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlersWidth;

    @Min(0)
    @Max(30)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlerPointsLeft;

    @Min(0)
    @Max(30)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlerPointsRight;

    @Min(0)
    @Max(50)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlersGirth;

    @Min(0)
    @Max(100)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlersLength;

    @Min(0)
    @Max(100)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlersInnerWidth;

    @Min(0)
    @Max(10)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer antlerShaftWidth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean notEdible;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean alone;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    public HarvestSpecimenDTO() {
    }

    public HarvestSpecimenDTO(final GameGender gender, final GameAge age, final Double weight) {
        super(gender);
        setAge(age);
        setWeight(weight);
    }

    @AssertTrue(message = "{HarvestSpecimenDTO.weightOutOfAcceptableLimits}")
    public boolean isWeightWithinAcceptableLimits() {
        return Stream.of(weight, weightEstimated, weightMeasured).noneMatch(w -> w != null && (w < 0.0 || w > 999.99));
    }

    // Accessors -->

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
