package fi.riista.feature.harvestregistry;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

import static fi.riista.feature.gamediary.harvest.Harvest.MAX_AMOUNT;
import static fi.riista.feature.gamediary.harvest.Harvest.MIN_AMOUNT;

@Entity
@Access(value = AccessType.FIELD)
public class HarvestRegistryItem extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "harvest_registry_item_id";

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Harvest harvest;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String shooterName;

    @Embedded
    @Valid
    private ShooterAddress shooterAddress;

    @FinnishHunterNumber
    @Column
    private String shooterHunterNumber;

    @NotNull
    @Column(nullable = false)
    private DateTime pointOfTime;

    @Column
    private Boolean timeOfDayValid = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_species_id", nullable = false)
    private GameSpecies species;

    @Min(MIN_AMOUNT)
    @Max(MAX_AMOUNT)
    @Column
    private Integer amount;

    @Column
    @Enumerated(EnumType.STRING)
    private GameGender gender;

    @Column
    @Enumerated(EnumType.STRING)
    private GameAge age;

    @Column
    private Double weight;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @Column(length = 3)
    @Size(max = 3)
    private String municipalityCode;

    @Column(length = 255)
    @Size(max = 255)
    private String municipalityFinnish;

    @Column(length = 255)
    @Size(max = 255)
    private String municipalitySwedish;

    @Column(length = 255)
    @Size(max = 255)
    private String harvestAreaFinnish;

    @Column(length = 255)
    @Size(max = 255)
    private String harvestAreaSwedish;

    @Column(length = 3)
    @Size(max = 3)
    private String rkaCode;


    @Column(length = 255)
    @Size(max = 255)
    private String rkaFinnish;

    @Column(length = 255)
    @Size(max = 255)
    private String rkaSwedish;

    @Column(length = 3)
    @Size(max = 3)
    private String rhyCode;


    @Column(length = 255)
    @Size(max = 255)
    private String rhyFinnish;

    @Column(length = 255)
    @Size(max = 255)
    private String rhySwedish;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
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
        this.harvest = harvest;
    }

    public String getShooterName() {
        return shooterName;
    }

    public void setShooterName(final String shooterName) {
        this.shooterName = shooterName;
    }

    public ShooterAddress getShooterAddress() {
        return shooterAddress;
    }

    public void setShooterAddress(final ShooterAddress shooterAddress) {
        this.shooterAddress = shooterAddress;
    }

    public String getShooterHunterNumber() {
        return shooterHunterNumber;
    }

    public void setShooterHunterNumber(final String shooterHunterNumber) {
        this.shooterHunterNumber = shooterHunterNumber;
    }

    public DateTime getPointOfTime() {
        return pointOfTime;
    }

    public void setPointOfTime(final DateTime pointOfTime) {
        this.pointOfTime = pointOfTime;
    }

    public boolean isTimeOfDayValid() {
        // For items where flag is not set, interpret as valid
        return Optional.ofNullable(timeOfDayValid).orElse(true);
    }

    public void setTimeOfDayValid(final Boolean timeOfDayValid) {
        this.timeOfDayValid = timeOfDayValid;
    }

    public GameSpecies getSpecies() {
        return species;
    }

    public void setSpecies(final GameSpecies species) {
        this.species = species;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }

    public GameGender getGender() {
        return gender;
    }

    public void setGender(final GameGender gender) {
        this.gender = gender;
    }

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

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getMunicipalityFinnish() {
        return municipalityFinnish;
    }

    public void setMunicipalityFinnish(final String municipalityFinnish) {
        this.municipalityFinnish = municipalityFinnish;
    }

    public String getMunicipalitySwedish() {
        return municipalitySwedish;
    }

    public void setMunicipalitySwedish(final String municipalitySwedish) {
        this.municipalitySwedish = municipalitySwedish;
    }

    public LocalisedString getMunicipalityLocalisation() {
        return municipalityFinnish != null && municipalitySwedish != null ?
                LocalisedString.of(municipalityFinnish, municipalitySwedish) :
                null;
    }

    public String getHarvestAreaFinnish() {
        return harvestAreaFinnish;
    }

    public void setHarvestAreaFinnish(final String harvestAreaFinnish) {
        this.harvestAreaFinnish = harvestAreaFinnish;
    }

    public String getHarvestAreaSwedish() {
        return harvestAreaSwedish;
    }

    public void setHarvestAreaSwedish(final String harvestAreaSwedish) {
        this.harvestAreaSwedish = harvestAreaSwedish;
    }

    public LocalisedString getHarvestAreaLocalisation() {
        return harvestAreaFinnish != null && harvestAreaSwedish != null ?
                LocalisedString.of(harvestAreaFinnish, harvestAreaSwedish) :
                null;
    }

    public String getRkaCode() {
        return rkaCode;
    }

    public void setRkaCode(final String rkaCode) {
        this.rkaCode = rkaCode;
    }

    public String getRkaFinnish() {
        return rkaFinnish;
    }

    public void setRkaFinnish(final String rkaFinnish) {
        this.rkaFinnish = rkaFinnish;
    }

    public String getRkaSwedish() {
        return rkaSwedish;
    }

    public void setRkaSwedish(final String rkaSwedish) {
        this.rkaSwedish = rkaSwedish;
    }

    public LocalisedString getRkaLocalisation() {

        return rkaFinnish != null && rkaSwedish != null ?
                LocalisedString.of(rkaFinnish, rkaSwedish) :
                null;
    }

    public String getRhyCode() {
        return rhyCode;
    }

    public void setRhyCode(final String rhyCode) {
        this.rhyCode = rhyCode;
    }

    public String getRhyFinnish() {
        return rhyFinnish;
    }

    public void setRhyFinnish(final String rhyFinnish) {
        this.rhyFinnish = rhyFinnish;
    }

    public String getRhySwedish() {
        return rhySwedish;
    }

    public void setRhySwedish(final String rhySwedish) {
        this.rhySwedish = rhySwedish;
    }

    public LocalisedString getRhyLocalisation() {
        return rhyFinnish != null && rhySwedish != null ?
                LocalisedString.of(rhyFinnish, rhySwedish) :
                null;
    }
}
