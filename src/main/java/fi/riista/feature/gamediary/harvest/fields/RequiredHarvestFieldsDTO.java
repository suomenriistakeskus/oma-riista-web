package fi.riista.feature.gamediary.harvest.fields;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.harvest.HarvestReportingType;

import javax.annotation.Nonnull;

// Fields generated by RequiredHarvestFields for given reportingType, gameSpeciesCode and huntingYear
public class RequiredHarvestFieldsDTO {

    @Nonnull
    public static RequiredHarvestFieldsDTO create(final int gameSpeciesCode,
                                                  final int huntingYear,
                                                  final @Nonnull HarvestReportingType reportingType) {
        final RequiredHarvestFields.Specimen specimenRequirements =
                RequiredHarvestFields.getSpecimenFields(huntingYear, gameSpeciesCode, null, reportingType);

        final RequiredHarvestFields.Report reportFieldRequirements =
                RequiredHarvestFields.getFormFields(huntingYear, gameSpeciesCode, reportingType);

        return RequiredHarvestFieldsDTO.create(reportFieldRequirements, specimenRequirements);
    }

    @Nonnull
    public static RequiredHarvestFieldsDTO create(final @Nonnull RequiredHarvestFields.Report report,
                                                  final @Nonnull RequiredHarvestFields.Specimen specimen) {
        final RequiredHarvestFieldsDTO dto = new RequiredHarvestFieldsDTO();

        dto.setPermitNumber(report.getPermitNumber());
        dto.setHarvestArea(report.getHarvestArea());
        dto.setHuntingMethod(report.getHuntingMethod());
        dto.setFeedingPlace(report.getFeedingPlace());
        dto.setTaigaBeanGoose(report.getTaigaBeanGoose());
        dto.setLukeStatus(report.getLukeStatus());
        dto.setHuntingAreaType(report.getHuntingAreaType());
        dto.setHuntingParty(report.getHuntingParty());
        dto.setHuntingAreaSize(report.getHuntingAreaSize());
        dto.setReportedWithPhoneCall(report.getReportedWithPhoneCall());
        dto.setWeight(specimen.getWeight());
        dto.setAge(specimen.getAge());
        dto.setGender(specimen.getGender());
        dto.setNotEdible(specimen.getNotEdible());
        dto.setFitnessClass(specimen.getFitnessClass());
        dto.setAdditionalInfo(specimen.getAdditionalInfo());
        dto.setWeightEstimated(specimen.getWeightEstimated());
        dto.setWeightMeasured(specimen.getWeightMeasured());
        dto.setAntlersType(specimen.getAntlersType());
        dto.setAntlersWidth(specimen.getAntlersWidth());
        dto.setAntlerPointsLeft(specimen.getAntlerPoints());
        dto.setAntlerPointsRight(specimen.getAntlerPoints());

        return dto;
    }

    private Required permitNumber;
    private Required harvestArea;
    private Required huntingAreaType;
    private Required huntingParty;
    private Required huntingAreaSize;
    private Required huntingMethod;
    private Required weight;
    private Required age;
    private Required gender;
    private Required reportedWithPhoneCall;
    private Required feedingPlace;
    private Required taigaBeanGoose;
    private Required lukeStatus;
    private Required additionalInfo;
    private Required weightEstimated;
    private Required weightMeasured;
    private Required notEdible;
    private Required fitnessClass;
    private Required antlersType;
    private Required antlersWidth;
    private Required antlerPointsLeft;
    private Required antlerPointsRight;

    public Required getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final Required permitNumber) {
        this.permitNumber = permitNumber;
    }

    public Required getHarvestArea() {
        return harvestArea;
    }

    public void setHarvestArea(final Required harvestArea) {
        this.harvestArea = harvestArea;
    }

    public Required getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(final Required huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public Required getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(final Required huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Required getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(final Required huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public Required getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(final Required huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Required getWeight() {
        return weight;
    }

    public void setWeight(final Required weight) {
        this.weight = weight;
    }

    public Required getAge() {
        return age;
    }

    public void setAge(final Required age) {
        this.age = age;
    }

    public Required getGender() {
        return gender;
    }

    public void setGender(final Required gender) {
        this.gender = gender;
    }

    public Required getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(final Required reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public Required getFeedingPlace() {
        return feedingPlace;
    }

    public void setFeedingPlace(final Required feedingPlace) {
        this.feedingPlace = feedingPlace;
    }

    public Required getTaigaBeanGoose() {
        return taigaBeanGoose;
    }

    public void setTaigaBeanGoose(final Required taigaBeanGoose) {
        this.taigaBeanGoose = taigaBeanGoose;
    }

    public Required getLukeStatus() {
        return lukeStatus;
    }

    public void setLukeStatus(final Required lukeStatus) {
        this.lukeStatus = lukeStatus;
    }

    public Required getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final Required additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Required getWeightEstimated() {
        return weightEstimated;
    }

    public void setWeightEstimated(final Required weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    public Required getWeightMeasured() {
        return weightMeasured;
    }

    public void setWeightMeasured(final Required weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    public Required getFitnessClass() {
        return fitnessClass;
    }

    public void setFitnessClass(final Required fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    public Required getAntlersType() {
        return antlersType;
    }

    public void setAntlersType(final Required antlersType) {
        this.antlersType = antlersType;
    }

    public Required getAntlersWidth() {
        return antlersWidth;
    }

    public void setAntlersWidth(final Required antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    public Required getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    public void setAntlerPointsLeft(final Required antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    public Required getAntlerPointsRight() {
        return antlerPointsRight;
    }

    public void setAntlerPointsRight(final Required antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    public Required getNotEdible() {
        return notEdible;
    }

    public void setNotEdible(final Required notEdible) {
        this.notEdible = notEdible;
    }
}
