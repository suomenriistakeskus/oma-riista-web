package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.gamediary.harvest.HarvestReportingType;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class RequiredHarvestSpecimenFieldsDTO {

    @Nonnull
    public static RequiredHarvestSpecimenFieldsDTO create(final int gameSpeciesCode,
                                                          final int huntingYear,
                                                          final @Nonnull HarvestReportingType reportingType,
                                                          final @Nonnull HarvestSpecVersion specVersion) {
        requireNonNull(reportingType);

        final boolean withPermit = reportingType.equals(HarvestReportingType.PERMIT);

        final RequiredHarvestFields.Specimen fieldRequirements = RequiredHarvestFields
                .getSpecimenFields(huntingYear, gameSpeciesCode, null, reportingType, false, specVersion, withPermit);

        return create(fieldRequirements);
    }

    @Nonnull
    public static RequiredHarvestSpecimenFieldsDTO create(final @Nonnull RequiredHarvestFields.Specimen specimen) {
        requireNonNull(specimen);

        final RequiredHarvestSpecimenFieldsDTO dto = new RequiredHarvestSpecimenFieldsDTO();

        dto.setAge(specimen.getAge());
        dto.setGender(specimen.getGender());
        dto.setWeight(specimen.getWeight());
        dto.setNotEdible(specimen.getNotEdible());
        dto.setFitnessClass(specimen.getFitnessClass());
        dto.setWeightEstimated(specimen.getWeightEstimated());
        dto.setWeightMeasured(specimen.getWeightMeasured());
        dto.setAntlersLost(specimen.getAntlersLost());
        dto.setAntlersType(specimen.getAntlersType());
        dto.setAntlersWidth(specimen.getAntlersWidth());
        dto.setAntlerPointsLeft(specimen.getAntlerPoints());
        dto.setAntlerPointsRight(specimen.getAntlerPoints());
        dto.setAntlersGirth(specimen.getAntlersGirth());
        dto.setAntlersLength(specimen.getAntlersLength());
        dto.setAntlersInnerWidth(specimen.getAntlersInnerWidth());
        dto.setAntlerShaftWidth(specimen.getAntlerShaftWidth());
        dto.setAlone(specimen.getAlone());
        dto.setAdditionalInfo(specimen.getAdditionalInfo());

        return dto;
    }

    private RequiredHarvestSpecimenField age;
    private RequiredHarvestSpecimenField gender;
    private RequiredHarvestSpecimenField weight;
    private RequiredHarvestSpecimenField weightEstimated;
    private RequiredHarvestSpecimenField weightMeasured;
    private RequiredHarvestSpecimenField notEdible;
    private RequiredHarvestSpecimenField fitnessClass;
    private RequiredHarvestSpecimenField antlersLost;
    private RequiredHarvestSpecimenField antlersType;
    private RequiredHarvestSpecimenField antlersWidth;
    private RequiredHarvestSpecimenField antlerPointsLeft;
    private RequiredHarvestSpecimenField antlerPointsRight;
    private RequiredHarvestSpecimenField antlersGirth;
    private RequiredHarvestSpecimenField antlersLength;
    private RequiredHarvestSpecimenField antlersInnerWidth;
    private RequiredHarvestSpecimenField antlerShaftWidth;
    private RequiredHarvestSpecimenField alone;
    private RequiredHarvestSpecimenField additionalInfo;

    public RequiredHarvestSpecimenField getAge() {
        return age;
    }

    public void setAge(final RequiredHarvestSpecimenField age) {
        this.age = age;
    }

    public RequiredHarvestSpecimenField getGender() {
        return gender;
    }

    public void setGender(final RequiredHarvestSpecimenField gender) {
        this.gender = gender;
    }

    public RequiredHarvestSpecimenField getWeight() {
        return weight;
    }

    public void setWeight(final RequiredHarvestSpecimenField weight) {
        this.weight = weight;
    }

    public RequiredHarvestSpecimenField getWeightEstimated() {
        return weightEstimated;
    }

    public void setWeightEstimated(final RequiredHarvestSpecimenField weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    public RequiredHarvestSpecimenField getWeightMeasured() {
        return weightMeasured;
    }

    public void setWeightMeasured(final RequiredHarvestSpecimenField weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    public RequiredHarvestSpecimenField getNotEdible() {
        return notEdible;
    }

    public void setNotEdible(final RequiredHarvestSpecimenField notEdible) {
        this.notEdible = notEdible;
    }

    public RequiredHarvestSpecimenField getFitnessClass() {
        return fitnessClass;
    }

    public void setFitnessClass(final RequiredHarvestSpecimenField fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    public RequiredHarvestSpecimenField getAntlersLost() {
        return antlersLost;
    }

    public void setAntlersLost(final RequiredHarvestSpecimenField antlersLost) {
        this.antlersLost = antlersLost;
    }

    public RequiredHarvestSpecimenField getAntlersType() {
        return antlersType;
    }

    public void setAntlersType(final RequiredHarvestSpecimenField antlersType) {
        this.antlersType = antlersType;
    }

    public RequiredHarvestSpecimenField getAntlersWidth() {
        return antlersWidth;
    }

    public void setAntlersWidth(final RequiredHarvestSpecimenField antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    public RequiredHarvestSpecimenField getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    public void setAntlerPointsLeft(final RequiredHarvestSpecimenField antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    public RequiredHarvestSpecimenField getAntlerPointsRight() {
        return antlerPointsRight;
    }

    public void setAntlerPointsRight(final RequiredHarvestSpecimenField antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    public RequiredHarvestSpecimenField getAntlersGirth() {
        return antlersGirth;
    }

    public void setAntlersGirth(final RequiredHarvestSpecimenField antlersGirth) {
        this.antlersGirth = antlersGirth;
    }

    public RequiredHarvestSpecimenField getAntlersLength() {
        return antlersLength;
    }

    public void setAntlersLength(final RequiredHarvestSpecimenField antlersLength) {
        this.antlersLength = antlersLength;
    }

    public RequiredHarvestSpecimenField getAntlersInnerWidth() {
        return antlersInnerWidth;
    }

    public void setAntlersInnerWidth(final RequiredHarvestSpecimenField antlersInnerWidth) {
        this.antlersInnerWidth = antlersInnerWidth;
    }

    public RequiredHarvestSpecimenField getAntlerShaftWidth() {
        return antlerShaftWidth;
    }

    public void setAntlerShaftWidth(final RequiredHarvestSpecimenField antlerShaftWidth) {
        this.antlerShaftWidth = antlerShaftWidth;
    }

    public RequiredHarvestSpecimenField getAlone() {
        return alone;
    }

    public void setAlone(final RequiredHarvestSpecimenField alone) {
        this.alone = alone;
    }

    public RequiredHarvestSpecimenField getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final RequiredHarvestSpecimenField additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
