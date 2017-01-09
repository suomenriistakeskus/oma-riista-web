package fi.riista.feature.harvestpermit.report.fields;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.util.DtoUtil;

import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;

public class HarvestReportFieldsDTO extends BaseEntityDTO<Long> implements HasBeginAndEndDate {

    public static @Nonnull HarvestReportFieldsDTO createWithSpecies(
            final @Nonnull HarvestReportFields fields,
            final @Nonnull GameSpecies gameSpecies) {
        HarvestReportFieldsDTO dto = new HarvestReportFieldsDTO();
        DtoUtil.copyBaseFields(fields, dto);

        dto.setName(fields.getName());
        dto.setSpecies(GameSpeciesDTO.create(gameSpecies));
        dto.setBeginDate(fields.getBeginDate());
        dto.setEndDate(fields.getEndDate());
        dto.setUsedWithPermit(fields.isUsedWithPermit());
        dto.setFreeHuntingAlso(fields.isFreeHuntingAlso());
        dto.setHarvestsAsList(fields.isHarvestsAsList());

        dto.setHuntingAreaType(fields.getHuntingAreaType());
        dto.setHuntingParty(fields.getHuntingParty());
        dto.setHuntingAreaSize(fields.getHuntingAreaSize());
        dto.setPermitNumber(fields.getPermitNumber());
        dto.setHuntingMethod(fields.getHuntingMethod());
        dto.setWeight(fields.getWeight());
        dto.setAge(fields.getAge());
        dto.setGender(fields.getGender());
        dto.setReportedWithPhoneCall(fields.getReportedWithPhoneCall());

        dto.setAdditionalInfo(fields.getAdditionalInfo());
        dto.setWeightEstimated(fields.getWeightEstimated());
        dto.setWeightMeasured(fields.getWeightMeasured());
        dto.setFitnessClass(fields.getFitnessClass());
        dto.setAntlersType(fields.getAntlersType());
        dto.setAntlersWidth(fields.getAntlersWidth());
        dto.setAntlerPointsLeft(fields.getAntlerPointsLeft());
        dto.setAntlerPointsRight(fields.getAntlerPointsRight());
        dto.setNotEdible(fields.getNotEdible());

        return dto;
    }

    public static @Nonnull HarvestReportFieldsDTO create(final @Nonnull HarvestReportFields fields) {
        return createWithSpecies(fields, fields.getSpecies());
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Valid
    private GameSpeciesDTO species;
    private LocalDate beginDate;
    private LocalDate endDate;
    private boolean usedWithPermit;
    private boolean freeHuntingAlso;
    private boolean harvestsAsList;

    private Required huntingAreaType;
    private Required huntingParty;
    private Required huntingAreaSize;
    private Required permitNumber;
    private Required huntingMethod;
    private Required weight;
    private Required age;
    private Required gender;
    private Required reportedWithPhoneCall;

    private Required additionalInfo;
    private Required weightEstimated;
    private Required weightMeasured;
    private Required fitnessClass;
    private Required antlersType;
    private Required antlersWidth;
    private Required antlerPointsLeft;
    private Required antlerPointsRight;
    private Required notEdible;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecies(GameSpeciesDTO species) {
        this.species = species;
    }

    public GameSpeciesDTO getSpecies() {
        return species;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isUsedWithPermit() {
        return usedWithPermit;
    }

    public void setUsedWithPermit(boolean usedWithPermit) {
        this.usedWithPermit = usedWithPermit;
    }

    public boolean isFreeHuntingAlso() {
        return freeHuntingAlso;
    }

    public void setFreeHuntingAlso(boolean freeHuntingAlso) {
        this.freeHuntingAlso = freeHuntingAlso;
    }

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
    }

    public Required getHuntingAreaType() {
        return huntingAreaType;
    }

    public void setHuntingAreaType(Required huntingAreaType) {
        this.huntingAreaType = huntingAreaType;
    }

    public Required getHuntingParty() {
        return huntingParty;
    }

    public void setHuntingParty(Required huntingParty) {
        this.huntingParty = huntingParty;
    }

    public Required getHuntingAreaSize() {
        return huntingAreaSize;
    }

    public void setHuntingAreaSize(Required huntingAreaSize) {
        this.huntingAreaSize = huntingAreaSize;
    }

    public Required getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(Required permitNumber) {
        this.permitNumber = permitNumber;
    }

    public Required getHuntingMethod() {
        return huntingMethod;
    }

    public void setHuntingMethod(Required huntingMethod) {
        this.huntingMethod = huntingMethod;
    }

    public Required getWeight() {
        return weight;
    }

    public void setWeight(Required weight) {
        this.weight = weight;
    }

    public Required getAge() {
        return age;
    }

    public void setAge(Required age) {
        this.age = age;
    }

    public Required getGender() {
        return gender;
    }

    public void setGender(Required gender) {
        this.gender = gender;
    }

    public Required getReportedWithPhoneCall() {
        return reportedWithPhoneCall;
    }

    public void setReportedWithPhoneCall(Required reportedWithPhoneCall) {
        this.reportedWithPhoneCall = reportedWithPhoneCall;
    }

    public Required getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Required additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Required getWeightEstimated() {
        return weightEstimated;
    }

    public void setWeightEstimated(Required weightEstimated) {
        this.weightEstimated = weightEstimated;
    }

    public Required getWeightMeasured() {
        return weightMeasured;
    }

    public void setWeightMeasured(Required weightMeasured) {
        this.weightMeasured = weightMeasured;
    }

    public Required getFitnessClass() {
        return fitnessClass;
    }

    public void setFitnessClass(Required fitnessClass) {
        this.fitnessClass = fitnessClass;
    }

    public Required getAntlersType() {
        return antlersType;
    }

    public void setAntlersType(Required antlersType) {
        this.antlersType = antlersType;
    }

    public Required getAntlersWidth() {
        return antlersWidth;
    }

    public void setAntlersWidth(Required antlersWidth) {
        this.antlersWidth = antlersWidth;
    }

    public Required getAntlerPointsLeft() {
        return antlerPointsLeft;
    }

    public void setAntlerPointsLeft(Required antlerPointsLeft) {
        this.antlerPointsLeft = antlerPointsLeft;
    }

    public Required getAntlerPointsRight() {
        return antlerPointsRight;
    }

    public void setAntlerPointsRight(Required antlerPointsRight) {
        this.antlerPointsRight = antlerPointsRight;
    }

    public Required getNotEdible() {
        return notEdible;
    }

    public void setNotEdible(Required notEdible) {
        this.notEdible = notEdible;
    }

}
