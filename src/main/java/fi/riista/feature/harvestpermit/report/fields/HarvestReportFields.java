package fi.riista.feature.harvestpermit.report.fields;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;

import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.LocalDate;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(AccessType.FIELD)
public class HarvestReportFields extends LifecycleEntity<Long> implements HasBeginAndEndDate {

    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    @Column(nullable = false)
    private boolean usedWithPermit;

    /**
     * Some species can be hunted "freely", but you can get permit to hunt it using otherwise illegal technique.
     * If true, this is used when person chooses that this harvest was hunted with permit.
     */
    @Column(nullable = false)
    private boolean freeHuntingAlso;

    @Column(nullable = false)
    private boolean harvestsAsList;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    /**
     * Pyyntialueen tyyppi (seura/erillinen
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required huntingAreaType = Required.YES;

    /**
     * Metsästysseuran/-seurueen nimi
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required huntingParty = Required.YES;

    /**
     * Alueen pinta-ala (ha)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required huntingAreaSize = Required.YES;

    /**
     * Lupanumero
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required permitNumber = Required.YES;

    /**
     * Saalistustapa/Pyyntitapa, Halli: ammuttu / Elävänä pyytävällä loukulla pyydetty / Ammuttu, mutta menetetty
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required huntingMethod = Required.YES;

    /**
     * Paino kg
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required weight = Required.YES;

    /**
     * Ikä
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required age = Required.YES;

    /**
     * Sukupuoli
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required gender = Required.YES;

    /**
     * Onko ilmoitettu myös saalispuhelimeen?
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required reportedWithPhoneCall = Required.YES;

    /**
     * Lisätietoja (esim. sarvet pudonneet, loiset, sairaidet, petojen raatelujäljet ...)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required additionalInfo = Required.YES;

    /**
     * Arvioitu teuraspaino
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required weightEstimated = Required.YES;

    /**
     * Punnittu teuraspaino
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required weightMeasured = Required.YES;

    /**
     * Kuntoluokka
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required fitnessClass = Required.YES;

    /**
     * Sarvityyppi
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required antlersType = Required.YES;

    /**
     * Sarvien kärkiväli (cm)
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required antlersWidth = Required.YES;

    /**
     * Sarvipiikit vasen
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required antlerPointsLeft = Required.YES;

    /**
     * Sarvipiikit oikea
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required antlerPointsRight = Required.YES;

    /**
     * Ihmisravinnoksi kelpaamaton
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Required notEdible = Required.YES;

    protected HarvestReportFields() {
    }

    public HarvestReportFields(String name, GameSpecies species, boolean usedWithPermit) {
        this.name = name;
        this.species = species;
        this.usedWithPermit = usedWithPermit;
    }

    public void noPermissionNumber() {
        setPermitNumber(Required.NO);
    }

    public void noHuntingAreaTypeOrSize() {
        setHuntingAreaSize(Required.NO);
        setHuntingAreaType(Required.NO);
    }

    public void noHuntingParty() {
        setHuntingParty(Required.NO);
    }

    public void noReportedWithPhoneCall() {
        setReportedWithPhoneCall(Required.NO);
    }

    public void noHuntingMethod() {
        setHuntingMethod(Required.NO);
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_report_fields_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(GameSpecies species) {
        this.species = species;
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

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
